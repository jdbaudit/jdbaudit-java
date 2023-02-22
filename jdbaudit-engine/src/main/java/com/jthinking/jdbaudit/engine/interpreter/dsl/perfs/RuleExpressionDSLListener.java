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

import com.jthinking.jdbaudit.engine.interpreter.dsl.ast.RuleExpressionDSLBaseListener;
import com.jthinking.jdbaudit.engine.interpreter.dsl.ast.RuleExpressionDSLParser;

import java.util.Stack;

public class RuleExpressionDSLListener extends RuleExpressionDSLBaseListener {
 
    private final Stack<ExprNode> stacks = new Stack<>();
 
    @Override
    public void exitIdentifier(RuleExpressionDSLParser.IdentifierContext ctx) {
        stacks.push(new VariableExprNode(ExprType.ID, ctx.getText()));
    }
 
    @Override
    public void exitAndEpr(RuleExpressionDSLParser.AndEprContext ctx) {
        ExprNode right = stacks.pop();
        ExprNode left = stacks.pop();
        stacks.push(new BinaryExprNode(ExprType.AND, left, right));
    }
 
    @Override
    public void exitOrEpr(RuleExpressionDSLParser.OrEprContext ctx) {
        ExprNode right = stacks.pop();
        ExprNode left = stacks.pop();
        stacks.push(new BinaryExprNode(ExprType.OR, left, right));
    }

    @Override
    public void exitNotEpr(RuleExpressionDSLParser.NotEprContext ctx) {
        ExprNode node = stacks.pop();
        stacks.push(new UnaryExprNode(ExprType.NOT, node));
    }

    @Override
    public void exitLine(RuleExpressionDSLParser.LineContext ctx) {
        super.exitLine(ctx);
    }
 
    @Override
    public void exitParenExpr(RuleExpressionDSLParser.ParenExprContext ctx) {

    }

    public ExprNode getResult() {
        return stacks.peek();
    }
 
}