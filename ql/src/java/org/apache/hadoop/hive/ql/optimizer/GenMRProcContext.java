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

package org.apache.hadoop.hive.ql.optimizer;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Stack;
import java.io.Serializable;
import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.hadoop.hive.ql.exec.Operator;
import org.apache.hadoop.hive.ql.exec.ReduceSinkOperator;
import org.apache.hadoop.hive.ql.exec.FileSinkOperator;
import org.apache.hadoop.hive.ql.exec.TableScanOperator;
import org.apache.hadoop.hive.ql.exec.JoinOperator;
import org.apache.hadoop.hive.ql.exec.Task;
import org.apache.hadoop.hive.ql.exec.TaskFactory;
import org.apache.hadoop.hive.ql.exec.OperatorFactory;
import org.apache.hadoop.hive.ql.plan.mapredWork;
import org.apache.hadoop.hive.ql.plan.reduceSinkDesc;
import org.apache.hadoop.hive.ql.plan.tableDesc;
import org.apache.hadoop.hive.ql.plan.partitionDesc;
import org.apache.hadoop.hive.ql.plan.fileSinkDesc;
import org.apache.hadoop.hive.ql.plan.PlanUtils;
import org.apache.hadoop.hive.ql.lib.NodeProcessorCtx;
import org.apache.hadoop.hive.ql.metadata.*;
import org.apache.hadoop.hive.ql.parse.ParseContext;
import org.apache.hadoop.hive.ql.exec.Utilities;
import org.apache.hadoop.fs.Path;

/**
 * Processor Context for creating map reduce task. Walk the tree in a DFS manner and process the nodes. Some state is 
 * maintained about the current nodes visited so far.
 */
public class GenMRProcContext implements NodeProcessorCtx {

  /** 
   * GenMapRedCtx is used to keep track of the current state. 
   */
  public static class GenMapRedCtx {
    Task<? extends Serializable>         currTask;
    Operator<? extends Serializable>     currTopOp;
    String                               currAliasId;
    
    /**
     * @param currTask    the current task
     * @param currTopOp   the current top operator being traversed
     * @param currAliasId the current alias for the to operator
     */
    public GenMapRedCtx (Task<? extends Serializable>         currTask,
                         Operator<? extends Serializable>     currTopOp,
                         String                               currAliasId) {
      this.currTask    = currTask;
      this.currTopOp   = currTopOp;
      this.currAliasId = currAliasId;
    }

    /**
     * @return current task
     */
    public Task<? extends Serializable> getCurrTask() {
      return currTask;
    }

    /**
     * @return current top operator
     */
    public Operator<? extends Serializable> getCurrTopOp() {
      return currTopOp;
    }

    /**
     * @return current alias
     */
    public String getCurrAliasId() {
      return currAliasId;
    }
  }
  
  private HashMap<Operator<? extends Serializable>, Task<? extends Serializable>> opTaskMap;
  private List<Operator<? extends Serializable>> seenOps;

  private ParseContext                          parseCtx;
  private Task<? extends Serializable>          mvTask;
  private List<Task<? extends Serializable>>    rootTasks;
  private String scratchDir;
  private int randomid;
  private int pathid;

  private Map<Operator<? extends Serializable>, GenMapRedCtx> mapCurrCtx; 
  private Task<? extends Serializable>         currTask;
  private Operator<? extends Serializable>     currTopOp;
  private String                               currAliasId;
  private List<Operator<? extends Serializable>> rootOps;

  /**
   * @param opTaskMap  reducer to task mapping
   * @param seenOps    operator already visited
   * @param parseCtx   current parse context
   * @param rootTasks  root tasks for the plan
   * @param mvTask     the final move task
   * @param scratchDir directory for temp destinations   
   * @param randomId   identifier used for temp destinations   
   * @param pathId     identifier used for temp destinations   
   * @param mapCurrCtx operator to task mappings
   */
  public GenMRProcContext (
    HashMap<Operator<? extends Serializable>, Task<? extends Serializable>> opTaskMap,
    List<Operator<? extends Serializable>> seenOps,
    ParseContext                           parseCtx,
    Task<? extends Serializable>           mvTask,
    List<Task<? extends Serializable>>     rootTasks,
    String scratchDir, int randomid, int pathid,
    Map<Operator<? extends Serializable>, GenMapRedCtx> mapCurrCtx) 
  {

    this.opTaskMap  = opTaskMap;
    this.seenOps    = seenOps;
    this.mvTask     = mvTask;
    this.parseCtx   = parseCtx;
    this.rootTasks  = rootTasks;
    this.scratchDir = scratchDir;
    this.randomid   = randomid;
    this.pathid     = pathid;
    this.mapCurrCtx = mapCurrCtx;
    currTask        = null;
    currTopOp       = null;
    currAliasId     = null;
    rootOps         = new ArrayList<Operator<? extends Serializable>>();
    rootOps.addAll(parseCtx.getTopOps().values());
  }

  /**
   * @return  reducer to task mapping
   */
  public HashMap<Operator<? extends Serializable>, Task<? extends Serializable>> getOpTaskMap() {
    return opTaskMap;
  }

  /**
   * @param opTaskMap  reducer to task mapping
   */
  public void setOpTaskMap(HashMap<Operator<? extends Serializable>, Task<? extends Serializable>> opTaskMap) {
    this.opTaskMap = opTaskMap;
  }

  /**
   * @return  operators already visited
   */
  public List<Operator<? extends Serializable>> getSeenOps() {
    return seenOps;
  }

  /**
   * @param seenOps    operators already visited
   */
  public void setSeenOps(List<Operator<? extends Serializable>> seenOps) {
    this.seenOps = seenOps;
  }

  /**
   * @return  top operators for tasks
   */
  public List<Operator<? extends Serializable>> getRootOps() {
    return rootOps;
  }

  /**
   * @param rootOps    top operators for tasks
   */
  public void setRootOps(List<Operator<? extends Serializable>> rootOps) {
    this.rootOps = rootOps;
  }

  /**
   * @return   current parse context
   */
  public ParseContext getParseCtx() {
    return parseCtx;
  }

  /**
   * @param parseCtx   current parse context
   */
  public void setParseCtx(ParseContext parseCtx) {
    this.parseCtx = parseCtx;
  }

  /**
   * @return     the final move task
   */
  public Task<? extends Serializable> getMvTask() {
    return mvTask;
  }

  /**
   * @param mvTask     the final move task
   */
  public void setMvTask(Task<? extends Serializable> mvTask) {
    this.mvTask = mvTask;
  }

  /**
   * @return  root tasks for the plan
   */
  public List<Task<? extends Serializable>>  getRootTasks() {
    return rootTasks;
  }

  /**
   * @param rootTasks  root tasks for the plan
   */
  public void setRootTasks(List<Task<? extends Serializable>>  rootTasks) {
    this.rootTasks = rootTasks;
  }

  /**
   * @return directory for temp destinations   
   */
  public String getScratchDir() {
    return scratchDir;
  }

  /**
   * @param scratchDir directory for temp destinations   
   */
  public void setScratchDir(String scratchDir) {
    this.scratchDir = scratchDir;
  }

  /**
   * @return   identifier used for temp destinations   
   */
  public int getRandomId() {
    return randomid;
  }

  /**
   * @param randomId   identifier used for temp destinations   
   */
  public void setRandomId(int randomid) {
    this.randomid = randomid;
  }

  /**
   * @return   identifier used for temp destinations   
   */
  public int getPathId() {
    return pathid;
  }

  /**
   * @param pathid   identifier used for temp destinations   
   */
  public void setPathId(int pathid) {
    this.pathid = pathid;
  }

  /**
   * @return operator to task mappings
   */
  public Map<Operator<? extends Serializable>, GenMapRedCtx> getMapCurrCtx() {
    return mapCurrCtx;
  }

  /**
   * @param mapCurrCtx operator to task mappings
   */
  public void setMapCurrCtx(Map<Operator<? extends Serializable>, GenMapRedCtx> mapCurrCtx) {
    this.mapCurrCtx = mapCurrCtx;
  }

  /**
   * @return current task
   */
  public Task<? extends Serializable>  getCurrTask() {
    return currTask;
  }

  /**
   * @param currTask current task
   */
  public void setCurrTask(Task<? extends Serializable>  currTask) {
    this.currTask = currTask;
  }

  /**
   * @return current top operator
   */
  public Operator<? extends Serializable> getCurrTopOp() {
    return currTopOp;
  }   
   
  /**
   * @param currTopOp current top operator
   */
  public void setCurrTopOp(Operator<? extends Serializable> currTopOp) {
    this.currTopOp = currTopOp;
  }      

  /**
   * @return current top alias
   */
  public String  getCurrAliasId() {
    return currAliasId;
  }

  /**
   * @param currAliasId current top alias
   */
  public void setCurrAliasId(String currAliasId) {
    this.currAliasId = currAliasId;
  }
}
