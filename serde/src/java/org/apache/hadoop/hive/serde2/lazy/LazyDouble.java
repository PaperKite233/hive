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
package org.apache.hadoop.hive.serde2.lazy;

import java.nio.charset.CharacterCodingException;

import org.apache.hadoop.io.Text;

/**
 * LazyObject for storing a value of Double.
 * 
 */
public class LazyDouble extends LazyPrimitive<Double> {

  public LazyDouble() {
    super(Double.class);
  }
  
  Text text = new Text();
  
  @Override
  public Double getPrimitiveObject() {
    // TODO: replace this by directly parsing the bytes buffer for better performance.
    if (bytes == null) return null;
    try {
      return Double.valueOf(Text.decode(bytes, start, length));
    } catch (NumberFormatException e) {
      return null;
    } catch (CharacterCodingException e) {
      return null;
    }
  }

}
