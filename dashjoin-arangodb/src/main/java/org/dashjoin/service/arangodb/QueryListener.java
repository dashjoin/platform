// Generated from Query.g4 by ANTLR 4.8
package org.dashjoin.service.arangodb;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link QueryParser}.
 */
public interface QueryListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link QueryParser#query}.
	 * @param ctx the parse tree
	 */
	void enterQuery(QueryParser.QueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#query}.
	 * @param ctx the parse tree
	 */
	void exitQuery(QueryParser.QueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link QueryParser#sort}.
	 * @param ctx the parse tree
	 */
	void enterSort(QueryParser.SortContext ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#sort}.
	 * @param ctx the parse tree
	 */
	void exitSort(QueryParser.SortContext ctx);
	/**
	 * Enter a parse tree produced by {@link QueryParser#limit}.
	 * @param ctx the parse tree
	 */
	void enterLimit(QueryParser.LimitContext ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#limit}.
	 * @param ctx the parse tree
	 */
	void exitLimit(QueryParser.LimitContext ctx);
	/**
	 * Enter a parse tree produced by {@link QueryParser#obj}.
	 * @param ctx the parse tree
	 */
	void enterObj(QueryParser.ObjContext ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#obj}.
	 * @param ctx the parse tree
	 */
	void exitObj(QueryParser.ObjContext ctx);
	/**
	 * Enter a parse tree produced by {@link QueryParser#pair}.
	 * @param ctx the parse tree
	 */
	void enterPair(QueryParser.PairContext ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#pair}.
	 * @param ctx the parse tree
	 */
	void exitPair(QueryParser.PairContext ctx);
	/**
	 * Enter a parse tree produced by {@link QueryParser#filter}.
	 * @param ctx the parse tree
	 */
	void enterFilter(QueryParser.FilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#filter}.
	 * @param ctx the parse tree
	 */
	void exitFilter(QueryParser.FilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link QueryParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(QueryParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(QueryParser.ValueContext ctx);
}