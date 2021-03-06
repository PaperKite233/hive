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

package org.apache.hadoop.hive.ql.parse;

import java.util.Stack;
import java.util.Vector;
import org.apache.hadoop.hive.ql.exec.*;
import org.apache.hadoop.hive.ql.lib.DefaultGraphWalker;
import org.apache.hadoop.hive.ql.lib.Dispatcher;
import org.apache.hadoop.hive.ql.lib.Node;

/**
 * Walks the operator tree in pre order fashion
 */
public class GenMapRedWalker extends DefaultGraphWalker {

  /**
   * constructor of the walker - the dispatcher is passed
   * @param disp the dispatcher to be called for each node visited
   */
  public GenMapRedWalker(Dispatcher disp) {
    super(disp);
  }
  
  /**
   * Walk the given operator
   * @param nd operator being walked
   */
  @Override
  public void walk(Node nd) throws SemanticException {
    Vector<Node> children = nd.getChildren();
    
    // maintain the stack of operators encountered
    opStack.push(nd);
    dispatch(nd, opStack);

    // kids of reduce sink operator need not be traversed again
    if ((children == null) ||
        ((nd instanceof ReduceSinkOperator) && (getDispatchedList().containsAll(children)))) {
      opStack.pop();
      return;
    }

    // move all the children to the front of queue
    for (Node ch : children)
      walk(ch);

    // done with this operator
    opStack.pop();
  }
}
