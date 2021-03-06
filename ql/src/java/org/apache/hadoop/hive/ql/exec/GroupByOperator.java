/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hive.ql.exec;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.IllegalAccessException;

import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.plan.aggregationDesc;
import org.apache.hadoop.hive.ql.plan.exprNodeDesc;
import org.apache.hadoop.hive.ql.plan.groupByDesc;
import org.apache.hadoop.hive.serde2.objectinspector.InspectableObject;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.ql.parse.OpParseContext;
import org.apache.hadoop.hive.ql.typeinfo.TypeInfo;
import org.apache.hadoop.hive.ql.typeinfo.PrimitiveTypeInfo;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.mapred.Reporter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * GroupBy operator implementation.
 */
public class GroupByOperator extends Operator <groupByDesc> implements Serializable {

  static final private Log LOG = LogFactory.getLog(JoinOperator.class.getName());

  private static final long serialVersionUID = 1L;
  private static final int  NUMROWSESTIMATESIZE = 1000;

  transient protected ExprNodeEvaluator[] keyFields;
  transient protected ExprNodeEvaluator[][] aggregationParameterFields;
  // In the future, we may allow both count(DISTINCT a) and sum(DISTINCT a) in the same SQL clause,
  // so aggregationIsDistinct is a boolean array instead of a single number. 
  transient protected boolean[] aggregationIsDistinct;

  transient Class<? extends UDAFEvaluator>[] aggregationClasses; 
  transient protected Method[] aggregationsAggregateMethods;
  transient protected Method[] aggregationsEvaluateMethods;

  transient protected ArrayList<ObjectInspector> objectInspectors;
  transient protected ObjectInspector outputObjectInspector;

  // Used by sort-based GroupBy: Mode = COMPLETE, PARTIAL1, PARTIAL2
  transient protected ArrayList<Object> currentKeys;
  transient protected UDAFEvaluator[] aggregations;
  transient protected Object[][] aggregationsParametersLastInvoke;

  // Used by hash-based GroupBy: Mode = HASH
  transient protected HashMap<ArrayList<Object>, UDAFEvaluator[]> hashAggregations;
  
  transient boolean firstRow;
  transient long    totalMemory;
  transient boolean hashAggr;

  /**
   * This is used to store the position and field names for variable length fields.
   **/
  class varLenFields {
    int           aggrPos;
    List<Field>   fields;
    varLenFields(int aggrPos, List<Field> fields) {
      this.aggrPos = aggrPos;
      this.fields  = fields;
    }

    int getAggrPos() {
      return aggrPos;
    }

    List<Field> getFields() {
      return fields;
    }
  };

  // for these positions, some variable primitive type (String) is used, so size cannot be estimated. sample it at runtime.
  transient List<Integer> keyPositionsSize;

  // for these positions, some variable primitive type (String) is used for the aggregation classes
  transient List<varLenFields> aggrPositions;

  transient int           fixedRowSize;
  transient long          maxHashTblMemory;
  transient int           totalVariableSize;
  transient int           numEntriesVarSize;
  transient int           numEntriesHashTable;

  public void initialize(Configuration hconf, Reporter reporter) throws HiveException {
    super.initialize(hconf, reporter);
    totalMemory = Runtime.getRuntime().totalMemory();

    // init keyFields
    keyFields = new ExprNodeEvaluator[conf.getKeys().size()];
    for (int i = 0; i < keyFields.length; i++) {
      keyFields[i] = ExprNodeEvaluatorFactory.get(conf.getKeys().get(i));
    }
  
    // init aggregationParameterFields
    aggregationParameterFields = new ExprNodeEvaluator[conf.getAggregators().size()][];
    for (int i = 0; i < aggregationParameterFields.length; i++) {
      ArrayList<exprNodeDesc> parameters = conf.getAggregators().get(i).getParameters();
      aggregationParameterFields[i] = new ExprNodeEvaluator[parameters.size()];
      for (int j = 0; j < parameters.size(); j++) {
        aggregationParameterFields[i][j] = ExprNodeEvaluatorFactory.get(parameters.get(j));
      }
    }
    // init aggregationIsDistinct
    aggregationIsDistinct = new boolean[conf.getAggregators().size()];
    for(int i=0; i<aggregationIsDistinct.length; i++) {
      aggregationIsDistinct[i] = conf.getAggregators().get(i).getDistinct();
    }

    // init aggregationClasses  
    aggregationClasses = (Class<? extends UDAFEvaluator>[]) new Class[conf.getAggregators().size()];
    for (int i = 0; i < conf.getAggregators().size(); i++) {
      aggregationDesc agg = conf.getAggregators().get(i);
      aggregationClasses[i] = agg.getAggregationClass();
    }

    // init aggregations, aggregationsAggregateMethods,
    // aggregationsEvaluateMethods
    aggregationsAggregateMethods = new Method[aggregationClasses.length];
    aggregationsEvaluateMethods = new Method[aggregationClasses.length];

    for(int i=0; i<aggregationClasses.length; i++) {
      String evaluateMethodName = conf.getEvalMethods().get(i);
      String aggregateMethodName = conf.getAggMethods().get(i);

      // aggregationsAggregateMethods
      for( Method m : aggregationClasses[i].getMethods() ){
        if( m.getName().equals( aggregateMethodName ) 
            && m.getParameterTypes().length == aggregationParameterFields[i].length) {              
          aggregationsAggregateMethods[i] = m;
          break;
        }
      }
      if (null == aggregationsAggregateMethods[i]) {
        throw new HiveException("Cannot find " + aggregateMethodName + " method of UDAF class "
                                 + aggregationClasses[i].getName() + " that accepts "
                                 + aggregationParameterFields[i].length + " parameters!");
      }
      // aggregationsEvaluateMethods
      try {
        aggregationsEvaluateMethods[i] = aggregationClasses[i].getMethod(evaluateMethodName);
      } catch (Exception e) {
        throw new HiveException("Unable to get the method named " + evaluateMethodName + " from " 
            + aggregationClasses[i] + ": " + e.getMessage());
      }

      if (null == aggregationsEvaluateMethods[i]) {
        throw new HiveException("Cannot find " + evaluateMethodName + " method of UDAF class "
                                 + aggregationClasses[i].getName() + "!");
      }
      assert(aggregationsEvaluateMethods[i] != null);
    }

    aggregationsParametersLastInvoke = new Object[conf.getAggregators().size()][];
    if (conf.getMode() != groupByDesc.Mode.HASH) {
      aggregations = newAggregations();
      hashAggr = false;
    } else {
      hashAggregations = new HashMap<ArrayList<Object>, UDAFEvaluator[]>();
      hashAggr = true;
      keyPositionsSize = new ArrayList<Integer>();
      aggrPositions = new ArrayList<varLenFields>();
    }

    // init objectInspectors
    int totalFields = keyFields.length + aggregationClasses.length;
    objectInspectors = new ArrayList<ObjectInspector>(totalFields);
    for(int i=0; i<keyFields.length; i++) {
      objectInspectors.add(null);
    }
    for(int i=0; i<aggregationClasses.length; i++) {
      objectInspectors.add(ObjectInspectorFactory.getStandardPrimitiveObjectInspector(
          aggregationsEvaluateMethods[i].getReturnType()));
    }
    
    firstRow = true;
    // estimate the number of hash table entries based on the size of each entry. Since the size of a entry
    // is not known, estimate that based on the number of entries
    if (conf.getMode() == groupByDesc.Mode.HASH)
      computeMaxEntriesHashAggr(hconf);
  }

  /**
   * Estimate the number of entries in map-side hash table. 
   * The user can specify the total amount of memory to be used by the map-side hash. By default, all available
   * memory is used. The size of each row is estimated, rather crudely, and the number of entries are figure out
   * based on that. 
   * @return number of entries that can fit in hash table - useful for map-side aggregation only
   **/
  private void computeMaxEntriesHashAggr(Configuration hconf) {
    maxHashTblMemory = (long)(HiveConf.getFloatVar(hconf, HiveConf.ConfVars.HIVEMAPAGGRHASHMEMORY) * Runtime.getRuntime().maxMemory());
    estimateRowSize();
  }

  private static final int javaObjectOverHead    = 16;
  private static final int javaHashEntryOverHead = 64;
  private static final int javaSizePrimitiveType = 16;
  private static final int javaSizeUnknownType   = 256;

  /**
   * The size of the element at position 'pos' is returned, if possible. 
   * If the datatype is of variable length, STRING, a list of such key positions is maintained, and the size for such positions is
   * then actually calculated at runtime.
   * @param pos the position of the key
   * @param c   the type of the key
   * @return the size of this datatype
   **/
  private int getSize(int pos, Class<?> c) {
    if (c.isPrimitive() ||
        c.isInstance(new Boolean(true)) ||
        c.isInstance(new Byte((byte)0)) ||
        c.isInstance(new Short((short)0)) ||
        c.isInstance(new Integer(0)) ||
        c.isInstance(new Long(0)) ||
        c.isInstance(new Float(0)) ||
        c.isInstance(new Double(0)))
      return javaSizePrimitiveType;

    if (c.isInstance(new String())) {
      keyPositionsSize.add(new Integer(pos));
      return javaObjectOverHead;
    }
      
    return javaSizeUnknownType;
  }

  /**
   * The size of the element at position 'pos' is returned, if possible. 
   * If the field is of variable length, STRING, a list of such field names for the field position is maintained, and the size 
   * for such positions is then actually calculated at runtime.
   * @param pos the position of the key
   * @param c   the type of the key
   * @param f   the field to be added
   * @return the size of this datatype
   **/
  private int getSize(int pos, Class<?> c, Field f) {
    if (c.isPrimitive() ||
        c.isInstance(new Boolean(true)) ||
        c.isInstance(new Byte((byte)0)) ||
        c.isInstance(new Short((short)0)) ||
        c.isInstance(new Integer(0)) ||
        c.isInstance(new Long(0)) ||
        c.isInstance(new Float(0)) ||
        c.isInstance(new Double(0)))
      return javaSizePrimitiveType;

    if (c.isInstance(new String())) {
      int idx = 0;
      varLenFields v = null;
      for (idx = 0; idx < aggrPositions.size(); idx++) {
        v = aggrPositions.get(idx);
        if (v.getAggrPos() == pos)
          break;
      }

      if (idx == aggrPositions.size()) {
        v = new varLenFields(pos, new ArrayList<Field>());
        aggrPositions.add(v);
      }

      v.getFields().add(f);
      return javaObjectOverHead;
    }
      
    return javaSizeUnknownType;
  }

  /**
   * @param pos position of the key
   * @param typeinfo type of the input
   * @return the size of this datatype
   **/
  private int getSize(int pos, TypeInfo typeInfo) {
    if (typeInfo instanceof PrimitiveTypeInfo) 
      return getSize(pos, typeInfo.getPrimitiveClass());
    return javaSizeUnknownType;
  }

  /**
   * @return the size of each row
   **/
  private void estimateRowSize() {
    // estimate the size of each entry - 
    // a datatype with unknown size (String/Struct etc. - is assumed to be 256 bytes for now).
    // 64 bytes is the overhead for a reference
    fixedRowSize = javaHashEntryOverHead;

    ArrayList<exprNodeDesc> keys = conf.getKeys();

    // Go over all the keys and get the size of the fields of fixed length. Keep track of the variable length keys
    for (int pos = 0; pos < keys.size(); pos++)
      fixedRowSize += getSize(pos, keys.get(pos).getTypeInfo());

    // Go over all the aggregation classes and and get the size of the fields of fixed length. Keep track of the variable length
    // fields in these aggregation classes.
    for(int i=0; i < aggregationClasses.length; i++) {

      fixedRowSize += javaObjectOverHead;
      Class<? extends UDAFEvaluator> agg = aggregationClasses[i];
      Field[] fArr = agg.getDeclaredFields();
      for (Field f : fArr) {
        fixedRowSize += getSize(i, f.getType(), f);
      }
    }
  }

  protected UDAFEvaluator[] newAggregations() throws HiveException {      
    UDAFEvaluator[] aggs = new UDAFEvaluator[aggregationClasses.length];
    for(int i=0; i<aggregationClasses.length; i++) {
      try {
        aggs[i] = aggregationClasses[i].newInstance();
      } catch (Exception e) {
        e.printStackTrace();
        throw new HiveException("Unable to create an instance of class " + aggregationClasses[i] + ": " + e.getMessage());
      }
      aggs[i].init();
    }
    return aggs;
  }

  InspectableObject tempInspectableObject = new InspectableObject();
  
  protected void updateAggregations(UDAFEvaluator[] aggs, Object row, ObjectInspector rowInspector, boolean hashAggr, boolean newEntry,
                                    Object[][] lastInvoke) throws HiveException {
    for(int ai=0; ai<aggs.length; ai++) {

      // Calculate the parameters 
      Object[] o = new Object[aggregationParameterFields[ai].length];
      for(int pi=0; pi<aggregationParameterFields[ai].length; pi++) {
        aggregationParameterFields[ai][pi].evaluate(row, rowInspector, tempInspectableObject);
        o[pi] = tempInspectableObject.o; 
      }

      // Update the aggregations.
      if (aggregationIsDistinct[ai]) {
        if (hashAggr) {
          if (newEntry) {
            FunctionRegistry.invoke(aggregationsAggregateMethods[ai], aggs[ai], o);
          }
        }
        else {
          boolean differentParameters = false;
          if ((lastInvoke == null) || (lastInvoke[ai] == null))
            differentParameters = true;
          else {
            for(int pi=0; pi<o.length; pi++) {
              if (!o[pi].equals(lastInvoke[ai][pi])) {
                differentParameters = true;
                break;
              }
            }  
          }

          if (differentParameters) {
            FunctionRegistry.invoke(aggregationsAggregateMethods[ai], aggs[ai], o);
            lastInvoke[ai] = o;
          }
        }
      }
      else {
        FunctionRegistry.invoke(aggregationsAggregateMethods[ai], aggs[ai], o);
      }
    }
  }
  
  public void process(Object row, ObjectInspector rowInspector) throws HiveException {
    
    try {
      // Compute the keys
      ArrayList<Object> newKeys = new ArrayList<Object>(keyFields.length);
      for (int i = 0; i < keyFields.length; i++) {
        keyFields[i].evaluate(row, rowInspector, tempInspectableObject);
        newKeys.add(tempInspectableObject.o);
        if (firstRow) {
          objectInspectors.set(i, tempInspectableObject.oi);
        }
      }
      if (firstRow) {
        firstRow = false;
        ArrayList<String> fieldNames = new ArrayList<String>(objectInspectors.size());
        for(int i=0; i<objectInspectors.size(); i++) {
          fieldNames.add(Integer.valueOf(i).toString());
        }
        outputObjectInspector = 
          ObjectInspectorFactory.getStandardStructObjectInspector(fieldNames, objectInspectors);
      }

      if (hashAggr)
        processHashAggr(row, rowInspector, newKeys);
      else
        processAggr(row, rowInspector, newKeys);
    } catch (HiveException e) {
      throw e;
    } catch (Exception e) {
      throw new HiveException(e);
    }
  }

  private void processHashAggr(Object row, ObjectInspector rowInspector, ArrayList<Object> newKeys) throws HiveException {
    // Prepare aggs for updating
    UDAFEvaluator[] aggs = null;
    boolean newEntry = false;

    // hash-based aggregations
    aggs = hashAggregations.get(newKeys);
    if (aggs == null) {
      aggs = newAggregations();
      hashAggregations.put(newKeys, aggs);
      newEntry = true;
    }

    // Update the aggs
    updateAggregations(aggs, row, rowInspector, true, newEntry, null);
    
    // based on used-specified pramaters, check if the hash table needs to be flushed
    if (shouldBeFlushed(newKeys)) {
      flush();
    }
  }

  private void processAggr(Object row, ObjectInspector rowInspector, ArrayList<Object> newKeys) throws HiveException {
    // Prepare aggs for updating
    UDAFEvaluator[] aggs = null;
    Object[][] lastInvoke = null;
    boolean keysAreEqual = newKeys.equals(currentKeys);
    
    // forward the current keys if needed for sort-based aggregation
    if (currentKeys != null && !keysAreEqual)
      forward(currentKeys, aggregations);
    
    // Need to update the keys?
    if (currentKeys == null || !keysAreEqual) {
      currentKeys = newKeys;
      
      // init aggregations
      for(UDAFEvaluator aggregation: aggregations)
        aggregation.init();
      
      // clear parameters in last-invoke
      for(int i=0; i<aggregationsParametersLastInvoke.length; i++)
        aggregationsParametersLastInvoke[i] = null;
    }
    
    aggs = aggregations;
    
    lastInvoke = aggregationsParametersLastInvoke;
    // Update the aggs
    updateAggregations(aggs, row, rowInspector, false, false, lastInvoke);
  }

  /**
   * Based on user-parameters, should the hash table be flushed.
   * @param newKeys keys for the row under consideration
   **/
  private boolean shouldBeFlushed(ArrayList<Object> newKeys) {
    int numEntries = hashAggregations.size();

    // The fixed size for the aggregation class is already known. Get the variable portion of the size every NUMROWSESTIMATESIZE rows.
    if ((numEntriesHashTable == 0) || ((numEntries % NUMROWSESTIMATESIZE) == 0)) {
      for (Integer pos : keyPositionsSize) {
        Object key = newKeys.get(pos.intValue());
        totalVariableSize += ((String)key).length();
      }

      UDAFEvaluator[] aggs = null;
      if (aggrPositions.size() > 0)
        aggs = hashAggregations.get(newKeys);

      for (varLenFields v : aggrPositions) {
        int     aggrPos          = v.getAggrPos();
        List<Field> fieldsVarLen = v.getFields();
        UDAFEvaluator    agg              = aggs[aggrPos];

        try 
        {
          for (Field f : fieldsVarLen)
            totalVariableSize += ((String)f.get(agg)).length();
        } catch (IllegalAccessException e) {
          assert false;
        }
      }

      numEntriesVarSize++;

      // Update the number of entries that can fit in the hash table
      numEntriesHashTable = (int)(maxHashTblMemory / (fixedRowSize + ((int)totalVariableSize/numEntriesVarSize)));
      LOG.trace("Hash Aggr: #hash table = " + numEntries + " #max in hash table = " + numEntriesHashTable);
    }

    // flush if necessary
    if (numEntries >= numEntriesHashTable)
      return true;
    return false;
  }

  private void flush() throws HiveException {
    
    // Currently, the algorithm flushes 10% of the entries - this can be
    // changed in the future

    int oldSize = hashAggregations.size();
    Iterator iter = hashAggregations.entrySet().iterator();
    int numDel = 0;
    while (iter.hasNext()) {
      Map.Entry<ArrayList<Object>, UDAFEvaluator[]> m = (Map.Entry)iter.next();
      forward(m.getKey(), m.getValue());
      iter.remove();
      numDel++;
      if (numDel * 10 >= oldSize)
        return;
    }
  }

  /**
   * Forward a record of keys and aggregation results.
   * 
   * @param keys
   *          The keys in the record
   * @throws HiveException
   */
  protected void forward(ArrayList<Object> keys, UDAFEvaluator[] aggs) throws HiveException {
    int totalFields = keys.size() + aggs.length;
    List<Object> a = new ArrayList<Object>(totalFields);
    for(int i=0; i<keys.size(); i++) {
      a.add(keys.get(i));
    }
    for(int i=0; i<aggs.length; i++) {
      try {
        a.add(aggregationsEvaluateMethods[i].invoke(aggs[i]));
      } catch (Exception e) {
        throw new HiveException("Unable to execute UDAF function " + aggregationsEvaluateMethods[i] + " " 
            + " on object " + "(" + aggs[i] + ") " + ": " + e.getMessage());
      }
    }
    forward(a, outputObjectInspector);
  }
  
  /**
   * We need to forward all the aggregations to children.
   * 
   */
  public void close(boolean abort) throws HiveException {
    if (!abort) {
      try {
        if (aggregations != null) {
          // sort-based aggregations
          if (currentKeys != null) {
            forward(currentKeys, aggregations);
          }
        } else if (hashAggregations != null) {
          // hash-based aggregations
          for (ArrayList<Object> key: hashAggregations.keySet()) {
            forward(key, hashAggregations.get(key));
          }
        } else {
          // The GroupByOperator is not initialized, which means there is no data
          // (since we initialize the operators when we see the first record).
          // Just do nothing here.
        }
      } catch (Exception e) {
        e.printStackTrace();
        throw new HiveException(e);
      }
    }
    super.close(abort);
  }

  // Group by contains the columns needed - no need to aggregate from children
  public List<String> genColLists(HashMap<Operator<? extends Serializable>, OpParseContext> opParseCtx) {
    List<String> colLists = new ArrayList<String>();
    ArrayList<exprNodeDesc> keys = conf.getKeys();
    for (exprNodeDesc key : keys)
      colLists = Utilities.mergeUniqElems(colLists, key.getCols());
    
    ArrayList<aggregationDesc> aggrs = conf.getAggregators();
    for (aggregationDesc aggr : aggrs) { 
      ArrayList<exprNodeDesc> params = aggr.getParameters();
      for (exprNodeDesc param : params) 
        colLists = Utilities.mergeUniqElems(colLists, param.getCols());
    }

    return colLists;
  }

  /**
   * @return the name of the operator
   */
  @Override
  public String getName() {
    return new String("GBY");
  }
}
