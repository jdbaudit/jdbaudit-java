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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternMatcher {

    private final static Pattern ENUM_PATTERN = Pattern.compile("(?<reverse>~?)<ENUM\\((?<items>\\[(\\s*'[^']*'\\s*)(\\s*,\\s*'[^']*'\\s*)*])\\)>");

    private final static Pattern RANGE_PATTERN = Pattern.compile("(?<reverse>~?)<RANGE\\(\\s*(?<start>\\d+)\\s*,\\s*(?<end>\\d+)\\s*\\)>");

    private final static Pattern HAS_PATTERN = Pattern.compile("(?<reverse>~?)<HAS\\(\\s*'(?<text>[^']*)'\\s*\\)>");

    private final static Pattern EMPTY_PATTERN = Pattern.compile("(?<reverse>~?)<EMPTY>");

    private final static Pattern EXIST_PATTERN = Pattern.compile("(?<reverse>~?)<EXIST>");

    /**
     * 银行卡号检测模式名
     */
    private final static Pattern BANK_NUM = Pattern.compile("(?<reverse>~?)<BANKNUM>");

    /**
     * 身份证号检测模式名
     */
    private final static Pattern CID = Pattern.compile("(?<reverse>~?)<CID>");

    /**
     * 恶意脚本检测模式名
     */
    private final static Pattern SEC_SCAN = Pattern.compile("(?<reverse>~?)<SECSCAN>");

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    
    public static boolean match(String columnData, String ruleValue) {
        Matcher existMatcher = EXIST_PATTERN.matcher(ruleValue);
        if (existMatcher.find()) {
            String reverse = existMatcher.group("reverse");
            if ("~".equals(reverse)) {
                return columnData == null;
            } else {
                return columnData != null;
            }
        }

        Matcher emptyMatcher = EMPTY_PATTERN.matcher(ruleValue);
        if (emptyMatcher.find()) {
            String reverse = emptyMatcher.group("reverse");
            if ("~".equals(reverse)) {
                return columnData != null && !columnData.equals("");
            } else {
                return columnData != null && columnData.equals("");
            }
        }

        Matcher rangeMatcher = RANGE_PATTERN.matcher(ruleValue);
        if (rangeMatcher.find()) {
            String reverse = rangeMatcher.group("reverse");
            int start = Integer.parseInt(rangeMatcher.group("start"));
            int end = Integer.parseInt(rangeMatcher.group("end"));
            if ("~".equals(reverse)) {
                if (columnData != null) {
                    try {
                        int value = Integer.parseInt(columnData);
                        if (value < start || value > end) {
                            return true;
                        }
                    } catch (Exception e) {
                        // TODO
                    }
                }
                return false;
            } else {
                if (columnData != null) {
                    try {
                        int value = Integer.parseInt(columnData);
                        if (value >= start && value <= end) {
                            return true;
                        }
                    } catch (Exception e) {
                        // TODO
                    }
                }
                return false;
            }
        }

        Matcher enumMatcher = ENUM_PATTERN.matcher(ruleValue);
        if (enumMatcher.find()) {
            String reverse = enumMatcher.group("reverse");
            String items = enumMatcher.group("items");
            List<String> objects;
            try {
                objects = OBJECT_MAPPER.readValue(items, new TypeReference<List<String>>() {});
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            boolean matched = columnData != null && objects.contains(columnData);
            if ("~".equals(reverse)) {
                return !matched;
            } else {
                return matched;
            }
        }

        Matcher hasMatcher = HAS_PATTERN.matcher(ruleValue);
        if (hasMatcher.find()) {
            String reverse = hasMatcher.group("reverse");
            String text = hasMatcher.group("text");
            boolean matched = columnData != null && columnData.contains(text);
            if ("~".equals(reverse)) {
                return !matched;
            } else {
                return matched;
            }
        }

        Matcher bankNumMatcher = BANK_NUM.matcher(ruleValue);
        if (bankNumMatcher.find()) {
            String reverse = bankNumMatcher.group("reverse");
            boolean matched = hasBankCardNumber(columnData);
            if ("~".equals(reverse)) {
                return !matched;
            } else {
                return matched;
            }
        }

        Matcher cIDMatcher = CID.matcher(ruleValue);
        if (cIDMatcher.find()) {
            String reverse = cIDMatcher.group("reverse");
            boolean matched = hasChineseIDNumber(columnData);
            if ("~".equals(reverse)) {
                return !matched;
            } else {
                return matched;
            }
        }

        return false;
    }

    /**
     * 身份证号校验
     * @param text
     * @return
     */
    public static boolean hasChineseIDNumber(String text) {
        if (text == null) {
            return false;
        }
        text = text.replaceAll("\\s+", "");
        Pattern compile = Pattern.compile("(^|\\D)(?<number>(11|12|13|14|15|21|22|23|31|32|33|34|35|36|37|41|42|43|44|45|46|50|51|52|53|54|61|62|63|64|65|71|81|82)\\d{15}[\\dX])($|\\D)");
        Matcher matcher = compile.matcher(text);
        int[] modulus = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        String[] checkNumberDic = {"1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"};
        if (matcher.find()) {
            String numberStr = matcher.group("number");
            char[] numberChars = numberStr.toCharArray();

            int number = 0;
            for (int i = 0; i < numberChars.length - 1; i++) {
                int aChar = Integer.parseInt(String.valueOf(numberChars[i]));
                number += aChar * modulus[i];
            }
            String checkNumber = checkNumberDic[number % 11];
            return numberStr.endsWith(checkNumber);
        }
        return false;
    }

    /**
     * 银行卡号校验
     * @param text
     * @return
     */
    public static boolean hasBankCardNumber(String text) {
        if (text == null) {
            return false;
        }
        text = text.replaceAll("\\s+", "");
        Pattern compile = Pattern.compile("(^|\\D)(?<number>(62|34|37|51|52|53|54|55|4)\\d{11,18})($|\\D)");
        Matcher matcher = compile.matcher(text);
        if (matcher.find()) {
            String number = matcher.group("number");
            char[] numberChars = number.toCharArray();
            int checkNumber;
            int number1 = 0;
            int number2 = 0;
            for (int i = 0; i < numberChars.length - 1; i++) {
                int aChar = Integer.parseInt(String.valueOf(numberChars[i]));
                if (i % 2 == 0) {
                    char[] chars = String.valueOf(aChar * 2).toCharArray();
                    for (char c : chars) {
                        int bChar = Integer.parseInt(String.valueOf(c));
                        number1 += bChar;
                    }
                } else {
                    number2 += aChar;
                }
            }
            String s = String.valueOf(number1 + number2);
            char[] array = s.toCharArray();
            int cInt = Integer.parseInt(String.valueOf(array[array.length - 1]));
            if (cInt == 0) {
                checkNumber = 0;
            } else {
                checkNumber = 10 - cInt;
            }
            return number.endsWith(String.valueOf(checkNumber));
        }
        return false;
    }
    
}
