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

@explain(displayName="Select Operator")
public class selectDesc implements Serializable {
  private static final long serialVersionUID = 1L;
  private java.util.ArrayList<org.apache.hadoop.hive.ql.plan.exprNodeDesc> colList;
  private boolean selectStar;
  public selectDesc() { }
  public selectDesc(final java.util.ArrayList<org.apache.hadoop.hive.ql.plan.exprNodeDesc> colList) {
    this(colList, false);
  }
  
  public selectDesc(
    final java.util.ArrayList<org.apache.hadoop.hive.ql.plan.exprNodeDesc> colList, final boolean selectStar) {
    this.colList = colList;
    this.selectStar = selectStar;
  }
  
  @explain(displayName="expressions")
  public java.util.ArrayList<org.apache.hadoop.hive.ql.plan.exprNodeDesc> getColList() {
    return this.colList;
  }
  public void setColList(final java.util.ArrayList<org.apache.hadoop.hive.ql.plan.exprNodeDesc> colList) {
    this.colList=colList;
  }
  
  /**
   * @return the selectStar
   */
  public boolean isSelectStar() {
    return selectStar;
  }
  /**
   * @param selectStar the selectStar to set
   */
  public void setSelectStar(boolean selectStar) {
    this.selectStar = selectStar;
  }
}
