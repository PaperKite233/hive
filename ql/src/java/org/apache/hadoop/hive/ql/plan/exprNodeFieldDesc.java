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

package org.apache.hadoop.hive.ql.plan;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hive.ql.typeinfo.TypeInfo;
import org.apache.hadoop.hive.ql.exec.Utilities;
import org.apache.hadoop.hive.ql.parse.RowResolver;


public class exprNodeFieldDesc extends exprNodeDesc implements Serializable {
  private static final long serialVersionUID = 1L;
  exprNodeDesc desc;
  String fieldName;
  
  // Used to support a.b where a is a list of struct that contains a field called b.
  // a.b will return an array that contains field b of all elements of array a. 
  Boolean isList;
  
  public exprNodeFieldDesc() {}
  public exprNodeFieldDesc(TypeInfo typeInfo, exprNodeDesc desc, String fieldName, Boolean isList) {
    super(typeInfo);
    this.desc = desc;
    this.fieldName = fieldName;
    this.isList = isList;
  }
  
  public exprNodeDesc getDesc() {
    return this.desc;
  }
  public void setDesc(exprNodeDesc desc) {
    this.desc = desc;
  }
  public String getFieldName() {
    return this.fieldName;
  }
  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }
  public Boolean getIsList() {
    return isList;
  }
  public void setIsList(Boolean isList) {
    this.isList = isList;
  }
  
  @Override
  public String toString() {
    return this.desc.toString() + "." + this.fieldName;
  }
  
  @explain(displayName="expr")
  @Override
  public String getExprString() {
    return this.desc.getExprString() + "." + this.fieldName;
  }

  public List<String> getCols() {
    List<String> colList = new ArrayList<String>();
    if (desc != null) 
    	colList = Utilities.mergeUniqElems(colList, desc.getCols());    
    return colList;
  }
}
