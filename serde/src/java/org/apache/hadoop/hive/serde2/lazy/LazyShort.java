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

/**
 * LazyObject for storing a value of Short.
 * 
 * <p>
 * Part of the code is adapted from Apache Harmony Project.
 * 
 * As with the specification, this implementation relied on code laid out in <a
 * href="http://www.hackersdelight.org/">Henry S. Warren, Jr.'s Hacker's
 * Delight, (Addison Wesley, 2002)</a> as well as <a
 * href="http://aggregate.org/MAGIC/">The Aggregate's Magic Algorithms</a>.
 * </p>
 * 
 */
public class LazyShort extends LazyPrimitive<Short> {

  public LazyShort() {
    super(Short.class);
  }
  
  @Override
  public Short getPrimitiveObject() {
    try {
      // Slower method: convert to String and then convert to Integer
      // return Short.valueOf(LazyUtils.convertToString(bytes, start, length));
      return Short.valueOf(parseShort(bytes, start, length));
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * Parses the string argument as if it was a short value and returns the
   * result. Throws NumberFormatException if the string does not represent an
   * short quantity.
   * 
   * @param bytes
   * @param start
   * @param length
   *            a UTF-8 encoded string representation of a short quantity.
   * @return short the value represented by the argument
   * @exception NumberFormatException
   *                if the argument could not be parsed as a short quantity.
   */
  public static short parseShort(byte[] bytes, int start, int length) 
      throws NumberFormatException {
    return parseShort(bytes, start, length, 10);
  }

  /**
   * Parses the string argument as if it was a short value and returns the
   * result. Throws NumberFormatException if the string does not represent a
   * single short quantity. The second argument specifies the radix to use
   * when parsing the value.
   * 
   * @param bytes
   * @param start
   * @param length
   *            a UTF-8 encoded string representation of a short quantity.
   * @param radix
   *            the radix to use when parsing.
   * @return short the value represented by the argument
   * @exception NumberFormatException
   *                if the argument could not be parsed as a short quantity.
   */
  public static short parseShort(byte[] bytes, int start, int length, int radix)
      throws NumberFormatException {
    int intValue = LazyInteger.parseInt(bytes, start, length, radix);
    short result = (short) intValue;
    if (result == intValue) {
      return result;
    }
    throw new NumberFormatException();
  }
  
}
