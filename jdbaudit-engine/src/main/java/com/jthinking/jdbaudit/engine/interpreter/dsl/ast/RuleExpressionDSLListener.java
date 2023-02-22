// Generated from com\jthinking\jdbaudit\engine\interpreter\dsl\ast\RuleExpressionDSL.g4 by ANTLR 4.9.3
package com.jthinking.jdbaudit.engine.interpreter.dsl.ast;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link RuleExpressionDSLParser}.
 */
public interface RuleExpressionDSLListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link RuleExpressionDSLParser#line}.
	 * @param ctx the parse tree
	 */
	void enterLine(RuleExpressionDSLParser.LineContext ctx);
	/**
	 * Exit a parse tree produced by {@link RuleExpressionDSLParser#line}.
	 * @param ctx the parse tree
	 */
	void exitLine(RuleExpressionDSLParser.LineContext ctx);
	/**
	 * Enter a parse tree produced by the {@code identifier}
	 * labeled alternative in {@link RuleExpressionDSLParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(RuleExpressionDSLParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by the {@code identifier}
	 * labeled alternative in {@link RuleExpressionDSLParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(RuleExpressionDSLParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by the {@code andEpr}
	 * labeled alternative in {@link RuleExpressionDSLParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAndEpr(RuleExpressionDSLParser.AndEprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code andEpr}
	 * labeled alternative in {@link RuleExpressionDSLParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAndEpr(RuleExpressionDSLParser.AndEprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code notEpr}
	 * labeled alternative in {@link RuleExpressionDSLParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterNotEpr(RuleExpressionDSLParser.NotEprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code notEpr}
	 * labeled alternative in {@link RuleExpressionDSLParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitNotEpr(RuleExpressionDSLParser.NotEprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code orEpr}
	 * labeled alternative in {@link RuleExpressionDSLParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterOrEpr(RuleExpressionDSLParser.OrEprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code orEpr}
	 * labeled alternative in {@link RuleExpressionDSLParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitOrEpr(RuleExpressionDSLParser.OrEprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code parenExpr}
	 * labeled alternative in {@link RuleExpressionDSLParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterParenExpr(RuleExpressionDSLParser.ParenExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code parenExpr}
	 * labeled alternative in {@link RuleExpressionDSLParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitParenExpr(RuleExpressionDSLParser.ParenExprContext ctx);
}