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
package com.jthinking.jdbaudit.engine.interpreter.util;

public class RuleUtils {

    /**
     * wildCard pattern :
     * 1. 5.*
     * 2. 5
     * 3. *
     * 4. 5.*,8.*
     * @param versionWildCard wildCard pattern
     * @param version 实际版本
     * @return 匹配是否成功
     */
    public static boolean wildCardMatch(String versionWildCard, String version) {
        if (versionWildCard == null || version == null) {
            return false;
        }
        String[] wildCards = versionWildCard.split(",");
        for (String wildCard : wildCards) {
            wildCard = wildCard.trim();
            if (wildCard.equals("*")) {
                return true;
            } else if (wildCard.endsWith("*")) {
                wildCard = wildCard.substring(0, wildCard.length() - 1);
                if (version.startsWith(wildCard)) {
                    return true;
                }
            } else {
                if (version.equals(wildCard)) {
                    return true;
                }
            }
        }
        return false;
    }

}
