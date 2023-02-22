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
package com.jthinking.jdbaudit.engine.interpreter.dsl.perfs;

import com.jthinking.jdbaudit.engine.interpreter.dsl.ast.RuleExpressionDSLLexer;
import com.jthinking.jdbaudit.engine.interpreter.dsl.ast.RuleExpressionDSLParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExprEvaluatorHelper {

    private static final ConcurrentHashMap<String, ExprNode> exprAstClassMap = new ConcurrentHashMap<>();

    public static boolean exec(String expr, Map<String, Boolean> params) {
        ExprNode root = exprAstClassMap.get(expr);
        if (root == null) {
            synchronized (expr.intern()) {
                RuleExpressionDSLLexer lexer = new RuleExpressionDSLLexer(CharStreams.fromString(expr));
                CommonTokenStream tokens = new CommonTokenStream(lexer);
                RuleExpressionDSLParser parser = new RuleExpressionDSLParser(tokens);
                RuleExpressionDSLListener listener = new RuleExpressionDSLListener();
                parser.addParseListener(listener);
                parser.line();
                root = listener.getResult();
                exprAstClassMap.put(expr, root);
            }
        }
        return ExprEvaluatorHelper.evaluate(root, params);
    }

    public static boolean evaluate(ExprNode cur, Map<String, Boolean> params) {
        if (cur instanceof VariableExprNode) {
            VariableExprNode vEx = (VariableExprNode) cur;
            if (vEx.type == ExprType.ID) {
                return params.get(vEx.name);
            } else {
                throw new RuntimeException("unsupported operation " + vEx.type);
            }
        } else if (cur instanceof BinaryExprNode) {
            BinaryExprNode bEx = (BinaryExprNode) cur;
            if (bEx.type == ExprType.AND) {
                boolean leftRes = evaluate(bEx.left, params);
                if (!leftRes) {
                    return false;
                }
                return evaluate(bEx.right, params);
            } else if (bEx.type == ExprType.OR) {
                boolean leftRes = evaluate(bEx.left, params);
                if (leftRes) {
                    return true;
                }
                return evaluate(bEx.right, params);
            } else {
                throw new RuntimeException("unsupported operation " + bEx.type);
            }
        } else if (cur instanceof UnaryExprNode) {
            UnaryExprNode uEx = (UnaryExprNode) cur;
            if (uEx.type == ExprType.NOT) {
                boolean res = evaluate(uEx.expr, params);
                return !res;
            } else {
                throw new RuntimeException("unsupported operation " + uEx.type);
            }
        } else {
            throw new RuntimeException("unsupported ExprNode " + cur.getClass().getName());
        }
    }

}