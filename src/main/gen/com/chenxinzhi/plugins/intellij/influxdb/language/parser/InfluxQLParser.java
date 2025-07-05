// This is a generated file. Not intended for manual editing.
package com.chenxinzhi.plugins.intellij.influxdb.language.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.chenxinzhi.plugins.intellij.influxdb.language.psi.InfluxQLTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class InfluxQLParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return influxQLFile(b, l + 1);
  }

  /* ********************************************************** */
  // SELECT | FROM | WHERE | GROUP | BY | ORDER | LIMIT | SLIMIT | OFFSET | SOFFSET | INTO | CREATE | DATABASE | DROP | DELETE | SHOW | DATABASES | MEASUREMENTS | SERIES | FIELD | KEYS | TAG | CONTINUOUS | QUERIES | QUERY | RETENTION | POLICY | ON | RESAMPLE | FOR | BEGIN_STMT | END_STMT | ALL | ANY_KEYWORD | AS | ASC | DESC | DISTINCT | GRANT | REVOKE | USER | USERS | WITH | TO
  static boolean KEYWORDS(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "KEYWORDS")) return false;
    boolean r;
    r = consumeToken(b, SELECT);
    if (!r) r = consumeToken(b, FROM);
    if (!r) r = consumeToken(b, WHERE);
    if (!r) r = consumeToken(b, GROUP);
    if (!r) r = consumeToken(b, BY);
    if (!r) r = consumeToken(b, ORDER);
    if (!r) r = consumeToken(b, LIMIT);
    if (!r) r = consumeToken(b, SLIMIT);
    if (!r) r = consumeToken(b, OFFSET);
    if (!r) r = consumeToken(b, SOFFSET);
    if (!r) r = consumeToken(b, INTO);
    if (!r) r = consumeToken(b, CREATE);
    if (!r) r = consumeToken(b, DATABASE);
    if (!r) r = consumeToken(b, DROP);
    if (!r) r = consumeToken(b, DELETE);
    if (!r) r = consumeToken(b, SHOW);
    if (!r) r = consumeToken(b, DATABASES);
    if (!r) r = consumeToken(b, MEASUREMENTS);
    if (!r) r = consumeToken(b, SERIES);
    if (!r) r = consumeToken(b, FIELD);
    if (!r) r = consumeToken(b, KEYS);
    if (!r) r = consumeToken(b, TAG);
    if (!r) r = consumeToken(b, CONTINUOUS);
    if (!r) r = consumeToken(b, QUERIES);
    if (!r) r = consumeToken(b, QUERY);
    if (!r) r = consumeToken(b, RETENTION);
    if (!r) r = consumeToken(b, POLICY);
    if (!r) r = consumeToken(b, ON);
    if (!r) r = consumeToken(b, RESAMPLE);
    if (!r) r = consumeToken(b, FOR);
    if (!r) r = consumeToken(b, BEGIN_STMT);
    if (!r) r = consumeToken(b, END_STMT);
    if (!r) r = consumeToken(b, ALL);
    if (!r) r = consumeToken(b, ANY_KEYWORD);
    if (!r) r = consumeToken(b, AS);
    if (!r) r = consumeToken(b, ASC);
    if (!r) r = consumeToken(b, DESC);
    if (!r) r = consumeToken(b, DISTINCT);
    if (!r) r = consumeToken(b, GRANT);
    if (!r) r = consumeToken(b, REVOKE);
    if (!r) r = consumeToken(b, USER);
    if (!r) r = consumeToken(b, USERS);
    if (!r) r = consumeToken(b, WITH);
    if (!r) r = consumeToken(b, TO);
    return r;
  }

  /* ********************************************************** */
  // EQ | NEQ | LT | LTE | GT | GTE | PLUS | MINUS | MUL | DIV | AND | OR | REGEQ | REGNEQ
  static boolean OPERATORS(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OPERATORS")) return false;
    boolean r;
    r = consumeToken(b, EQ);
    if (!r) r = consumeToken(b, NEQ);
    if (!r) r = consumeToken(b, LT);
    if (!r) r = consumeToken(b, LTE);
    if (!r) r = consumeToken(b, GT);
    if (!r) r = consumeToken(b, GTE);
    if (!r) r = consumeToken(b, PLUS);
    if (!r) r = consumeToken(b, MINUS);
    if (!r) r = consumeToken(b, MUL);
    if (!r) r = consumeToken(b, DIV);
    if (!r) r = consumeToken(b, AND);
    if (!r) r = consumeToken(b, OR);
    if (!r) r = consumeToken(b, REGEQ);
    if (!r) r = consumeToken(b, REGNEQ);
    return r;
  }

  /* ********************************************************** */
  // LPAREN | RPAREN | COMMA | DOT | SEMICOLON
  static boolean PUNCTUATION(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PUNCTUATION")) return false;
    boolean r;
    r = consumeToken(b, LPAREN);
    if (!r) r = consumeToken(b, RPAREN);
    if (!r) r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, DOT);
    if (!r) r = consumeToken(b, SEMICOLON);
    return r;
  }

  /* ********************************************************** */
  // identifierMy
  public static boolean alias(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "alias")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifierMy(b, l + 1);
    exit_section_(b, m, ALIAS, r);
    return r;
  }

  /* ********************************************************** */
  // (IDENTIFIER | STRING_LITERAL | NUMBER_LITERAL | KEYWORDS | OPERATORS | PUNCTUATION)*
  static boolean any_tokens(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "any_tokens")) return false;
    while (true) {
      int c = current_position_(b);
      if (!any_tokens_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "any_tokens", c)) break;
    }
    return true;
  }

  // IDENTIFIER | STRING_LITERAL | NUMBER_LITERAL | KEYWORDS | OPERATORS | PUNCTUATION
  private static boolean any_tokens_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "any_tokens_0")) return false;
    boolean r;
    r = consumeToken(b, IDENTIFIER);
    if (!r) r = consumeToken(b, STRING_LITERAL);
    if (!r) r = consumeToken(b, NUMBER_LITERAL);
    if (!r) r = KEYWORDS(b, l + 1);
    if (!r) r = OPERATORS(b, l + 1);
    if (!r) r = PUNCTUATION(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // unary_expression ( (EQ|NEQ|LT|LTE|GT|GTE|REGEQ|REGNEQ|PLUS|MINUS|MUL|DIV|AND|OR) unary_expression )*
  static boolean binary_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "binary_expression")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = unary_expression(b, l + 1);
    r = r && binary_expression_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( (EQ|NEQ|LT|LTE|GT|GTE|REGEQ|REGNEQ|PLUS|MINUS|MUL|DIV|AND|OR) unary_expression )*
  private static boolean binary_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "binary_expression_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!binary_expression_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "binary_expression_1", c)) break;
    }
    return true;
  }

  // (EQ|NEQ|LT|LTE|GT|GTE|REGEQ|REGNEQ|PLUS|MINUS|MUL|DIV|AND|OR) unary_expression
  private static boolean binary_expression_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "binary_expression_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = binary_expression_1_0_0(b, l + 1);
    r = r && unary_expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // EQ|NEQ|LT|LTE|GT|GTE|REGEQ|REGNEQ|PLUS|MINUS|MUL|DIV|AND|OR
  private static boolean binary_expression_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "binary_expression_1_0_0")) return false;
    boolean r;
    r = consumeToken(b, EQ);
    if (!r) r = consumeToken(b, NEQ);
    if (!r) r = consumeToken(b, LT);
    if (!r) r = consumeToken(b, LTE);
    if (!r) r = consumeToken(b, GT);
    if (!r) r = consumeToken(b, GTE);
    if (!r) r = consumeToken(b, REGEQ);
    if (!r) r = consumeToken(b, REGNEQ);
    if (!r) r = consumeToken(b, PLUS);
    if (!r) r = consumeToken(b, MINUS);
    if (!r) r = consumeToken(b, MUL);
    if (!r) r = consumeToken(b, DIV);
    if (!r) r = consumeToken(b, AND);
    if (!r) r = consumeToken(b, OR);
    return r;
  }

  /* ********************************************************** */
  // binary_expression
  static boolean expression(PsiBuilder b, int l) {
    return binary_expression(b, l + 1);
  }

  /* ********************************************************** */
  // expression ('as' alias)?
  public static boolean field_clause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "field_clause")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FIELD_CLAUSE, "<field clause>");
    r = expression(b, l + 1);
    r = r && field_clause_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ('as' alias)?
  private static boolean field_clause_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "field_clause_1")) return false;
    field_clause_1_0(b, l + 1);
    return true;
  }

  // 'as' alias
  private static boolean field_clause_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "field_clause_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "as");
    r = r && alias(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // MUL | field_clause (',' field_clause)*
  public static boolean field_list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "field_list")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FIELD_LIST, "<field list>");
    r = consumeToken(b, MUL);
    if (!r) r = field_list_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // field_clause (',' field_clause)*
  private static boolean field_list_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "field_list_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = field_clause(b, l + 1);
    r = r && field_list_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' field_clause)*
  private static boolean field_list_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "field_list_1_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!field_list_1_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "field_list_1_1", c)) break;
    }
    return true;
  }

  // ',' field_clause
  private static boolean field_list_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "field_list_1_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ",");
    r = r && field_clause(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // measurement (',' measurement)*
  public static boolean from_clause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = measurement(b, l + 1);
    r = r && from_clause_1(b, l + 1);
    exit_section_(b, m, FROM_CLAUSE, r);
    return r;
  }

  // (',' measurement)*
  private static boolean from_clause_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!from_clause_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "from_clause_1", c)) break;
    }
    return true;
  }

  // ',' measurement
  private static boolean from_clause_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "from_clause_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ",");
    r = r && measurement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // identifierMy '(' (DISTINCT? (expression (',' expression)*) | MUL)? ')'
  public static boolean function_call(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifierMy(b, l + 1);
    r = r && consumeToken(b, "(");
    r = r && function_call_2(b, l + 1);
    r = r && consumeToken(b, ")");
    exit_section_(b, m, FUNCTION_CALL, r);
    return r;
  }

  // (DISTINCT? (expression (',' expression)*) | MUL)?
  private static boolean function_call_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_2")) return false;
    function_call_2_0(b, l + 1);
    return true;
  }

  // DISTINCT? (expression (',' expression)*) | MUL
  private static boolean function_call_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = function_call_2_0_0(b, l + 1);
    if (!r) r = consumeToken(b, MUL);
    exit_section_(b, m, null, r);
    return r;
  }

  // DISTINCT? (expression (',' expression)*)
  private static boolean function_call_2_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_2_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = function_call_2_0_0_0(b, l + 1);
    r = r && function_call_2_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // DISTINCT?
  private static boolean function_call_2_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_2_0_0_0")) return false;
    consumeToken(b, DISTINCT);
    return true;
  }

  // expression (',' expression)*
  private static boolean function_call_2_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_2_0_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression(b, l + 1);
    r = r && function_call_2_0_0_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' expression)*
  private static boolean function_call_2_0_0_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_2_0_0_1_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!function_call_2_0_0_1_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "function_call_2_0_0_1_1", c)) break;
    }
    return true;
  }

  // ',' expression
  private static boolean function_call_2_0_0_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_2_0_0_1_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ",");
    r = r && expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (time_clause | tag_clause) (',' (time_clause | tag_clause))*
  public static boolean group_by_clause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_by_clause")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, GROUP_BY_CLAUSE, "<group by clause>");
    r = group_by_clause_0(b, l + 1);
    r = r && group_by_clause_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // time_clause | tag_clause
  private static boolean group_by_clause_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_by_clause_0")) return false;
    boolean r;
    r = time_clause(b, l + 1);
    if (!r) r = tag_clause(b, l + 1);
    return r;
  }

  // (',' (time_clause | tag_clause))*
  private static boolean group_by_clause_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_by_clause_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!group_by_clause_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "group_by_clause_1", c)) break;
    }
    return true;
  }

  // ',' (time_clause | tag_clause)
  private static boolean group_by_clause_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_by_clause_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ",");
    r = r && group_by_clause_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // time_clause | tag_clause
  private static boolean group_by_clause_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_by_clause_1_0_1")) return false;
    boolean r;
    r = time_clause(b, l + 1);
    if (!r) r = tag_clause(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean identifierMy(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifierMy")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, IDENTIFIER_MY, r);
    return r;
  }

  /* ********************************************************** */
  // statement*
  static boolean influxQLFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "influxQLFile")) return false;
    while (true) {
      int c = current_position_(b);
      if (!statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "influxQLFile", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // INTEGER_LITERAL
  public static boolean limit_clause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "limit_clause")) return false;
    if (!nextTokenIs(b, INTEGER_LITERAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, INTEGER_LITERAL);
    exit_section_(b, m, LIMIT_CLAUSE, r);
    return r;
  }

  /* ********************************************************** */
  // NUMBER_LITERAL | STRING_LITERAL | BOOLEAN_LITERAL | DURATION_LITERAL
  public static boolean literal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literal")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LITERAL, "<literal>");
    r = consumeToken(b, NUMBER_LITERAL);
    if (!r) r = consumeToken(b, STRING_LITERAL);
    if (!r) r = consumeToken(b, BOOLEAN_LITERAL);
    if (!r) r = consumeToken(b, DURATION_LITERAL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // identifierMy
  public static boolean measurement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "measurement")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifierMy(b, l + 1);
    exit_section_(b, m, MEASUREMENT, r);
    return r;
  }

  /* ********************************************************** */
  // INTEGER_LITERAL
  public static boolean offset_clause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "offset_clause")) return false;
    if (!nextTokenIs(b, INTEGER_LITERAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, INTEGER_LITERAL);
    exit_section_(b, m, OFFSET_CLAUSE, r);
    return r;
  }

  /* ********************************************************** */
  // 'time' (ASC | DESC)?
  public static boolean order_by_clause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "order_by_clause")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ORDER_BY_CLAUSE, "<order by clause>");
    r = consumeToken(b, "time");
    r = r && order_by_clause_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (ASC | DESC)?
  private static boolean order_by_clause_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "order_by_clause_1")) return false;
    order_by_clause_1_0(b, l + 1);
    return true;
  }

  // ASC | DESC
  private static boolean order_by_clause_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "order_by_clause_1_0")) return false;
    boolean r;
    r = consumeToken(b, ASC);
    if (!r) r = consumeToken(b, DESC);
    return r;
  }

  /* ********************************************************** */
  // (CREATE | DROP | DELETE | GRANT | REVOKE) any_tokens
  public static boolean other_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "other_statement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OTHER_STATEMENT, "<other statement>");
    r = other_statement_0(b, l + 1);
    r = r && any_tokens(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // CREATE | DROP | DELETE | GRANT | REVOKE
  private static boolean other_statement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "other_statement_0")) return false;
    boolean r;
    r = consumeToken(b, CREATE);
    if (!r) r = consumeToken(b, DROP);
    if (!r) r = consumeToken(b, DELETE);
    if (!r) r = consumeToken(b, GRANT);
    if (!r) r = consumeToken(b, REVOKE);
    return r;
  }

  /* ********************************************************** */
  // literal | identifierMy | function_call | '(' expression ')'
  static boolean primary_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "primary_expression")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = literal(b, l + 1);
    if (!r) r = identifierMy(b, l + 1);
    if (!r) r = function_call(b, l + 1);
    if (!r) r = primary_expression_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '(' expression ')'
  private static boolean primary_expression_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "primary_expression_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "(");
    r = r && expression(b, l + 1);
    r = r && consumeToken(b, ")");
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // SELECT field_list FROM from_clause (WHERE where_clause)? (GROUP BY group_by_clause)? (ORDER BY order_by_clause)? (LIMIT limit_clause)? (SLIMIT slimit_clause)? (OFFSET offset_clause)? (SOFFSET soffset_clause)?
  public static boolean select_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_statement")) return false;
    if (!nextTokenIs(b, SELECT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SELECT);
    r = r && field_list(b, l + 1);
    r = r && consumeToken(b, FROM);
    r = r && from_clause(b, l + 1);
    r = r && select_statement_4(b, l + 1);
    r = r && select_statement_5(b, l + 1);
    r = r && select_statement_6(b, l + 1);
    r = r && select_statement_7(b, l + 1);
    r = r && select_statement_8(b, l + 1);
    r = r && select_statement_9(b, l + 1);
    r = r && select_statement_10(b, l + 1);
    exit_section_(b, m, SELECT_STATEMENT, r);
    return r;
  }

  // (WHERE where_clause)?
  private static boolean select_statement_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_statement_4")) return false;
    select_statement_4_0(b, l + 1);
    return true;
  }

  // WHERE where_clause
  private static boolean select_statement_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_statement_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, WHERE);
    r = r && where_clause(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (GROUP BY group_by_clause)?
  private static boolean select_statement_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_statement_5")) return false;
    select_statement_5_0(b, l + 1);
    return true;
  }

  // GROUP BY group_by_clause
  private static boolean select_statement_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_statement_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, GROUP, BY);
    r = r && group_by_clause(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (ORDER BY order_by_clause)?
  private static boolean select_statement_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_statement_6")) return false;
    select_statement_6_0(b, l + 1);
    return true;
  }

  // ORDER BY order_by_clause
  private static boolean select_statement_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_statement_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, ORDER, BY);
    r = r && order_by_clause(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (LIMIT limit_clause)?
  private static boolean select_statement_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_statement_7")) return false;
    select_statement_7_0(b, l + 1);
    return true;
  }

  // LIMIT limit_clause
  private static boolean select_statement_7_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_statement_7_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LIMIT);
    r = r && limit_clause(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (SLIMIT slimit_clause)?
  private static boolean select_statement_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_statement_8")) return false;
    select_statement_8_0(b, l + 1);
    return true;
  }

  // SLIMIT slimit_clause
  private static boolean select_statement_8_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_statement_8_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SLIMIT);
    r = r && slimit_clause(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (OFFSET offset_clause)?
  private static boolean select_statement_9(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_statement_9")) return false;
    select_statement_9_0(b, l + 1);
    return true;
  }

  // OFFSET offset_clause
  private static boolean select_statement_9_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_statement_9_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OFFSET);
    r = r && offset_clause(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (SOFFSET soffset_clause)?
  private static boolean select_statement_10(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_statement_10")) return false;
    select_statement_10_0(b, l + 1);
    return true;
  }

  // SOFFSET soffset_clause
  private static boolean select_statement_10_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "select_statement_10_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SOFFSET);
    r = r && soffset_clause(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // SHOW (DATABASES | MEASUREMENTS (ON identifierMy)? | SERIES (FROM measurement)? | FIELD KEYS (FROM measurement)? | TAG KEYS (FROM measurement)?)
  public static boolean show_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "show_statement")) return false;
    if (!nextTokenIs(b, SHOW)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SHOW);
    r = r && show_statement_1(b, l + 1);
    exit_section_(b, m, SHOW_STATEMENT, r);
    return r;
  }

  // DATABASES | MEASUREMENTS (ON identifierMy)? | SERIES (FROM measurement)? | FIELD KEYS (FROM measurement)? | TAG KEYS (FROM measurement)?
  private static boolean show_statement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "show_statement_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DATABASES);
    if (!r) r = show_statement_1_1(b, l + 1);
    if (!r) r = show_statement_1_2(b, l + 1);
    if (!r) r = show_statement_1_3(b, l + 1);
    if (!r) r = show_statement_1_4(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // MEASUREMENTS (ON identifierMy)?
  private static boolean show_statement_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "show_statement_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, MEASUREMENTS);
    r = r && show_statement_1_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (ON identifierMy)?
  private static boolean show_statement_1_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "show_statement_1_1_1")) return false;
    show_statement_1_1_1_0(b, l + 1);
    return true;
  }

  // ON identifierMy
  private static boolean show_statement_1_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "show_statement_1_1_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ON);
    r = r && identifierMy(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // SERIES (FROM measurement)?
  private static boolean show_statement_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "show_statement_1_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SERIES);
    r = r && show_statement_1_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (FROM measurement)?
  private static boolean show_statement_1_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "show_statement_1_2_1")) return false;
    show_statement_1_2_1_0(b, l + 1);
    return true;
  }

  // FROM measurement
  private static boolean show_statement_1_2_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "show_statement_1_2_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FROM);
    r = r && measurement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // FIELD KEYS (FROM measurement)?
  private static boolean show_statement_1_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "show_statement_1_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, FIELD, KEYS);
    r = r && show_statement_1_3_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (FROM measurement)?
  private static boolean show_statement_1_3_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "show_statement_1_3_2")) return false;
    show_statement_1_3_2_0(b, l + 1);
    return true;
  }

  // FROM measurement
  private static boolean show_statement_1_3_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "show_statement_1_3_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FROM);
    r = r && measurement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // TAG KEYS (FROM measurement)?
  private static boolean show_statement_1_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "show_statement_1_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, TAG, KEYS);
    r = r && show_statement_1_4_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (FROM measurement)?
  private static boolean show_statement_1_4_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "show_statement_1_4_2")) return false;
    show_statement_1_4_2_0(b, l + 1);
    return true;
  }

  // FROM measurement
  private static boolean show_statement_1_4_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "show_statement_1_4_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FROM);
    r = r && measurement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // INTEGER_LITERAL
  public static boolean slimit_clause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "slimit_clause")) return false;
    if (!nextTokenIs(b, INTEGER_LITERAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, INTEGER_LITERAL);
    exit_section_(b, m, SLIMIT_CLAUSE, r);
    return r;
  }

  /* ********************************************************** */
  // INTEGER_LITERAL
  public static boolean soffset_clause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "soffset_clause")) return false;
    if (!nextTokenIs(b, INTEGER_LITERAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, INTEGER_LITERAL);
    exit_section_(b, m, SOFFSET_CLAUSE, r);
    return r;
  }

  /* ********************************************************** */
  // (select_statement | show_statement | other_statement) (';')?
  static boolean statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = statement_0(b, l + 1);
    p = r; // pin = 1
    r = r && statement_1(b, l + 1);
    exit_section_(b, l, m, r, p, InfluxQLParser::statement_recovery);
    return r || p;
  }

  // select_statement | show_statement | other_statement
  private static boolean statement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement_0")) return false;
    boolean r;
    r = select_statement(b, l + 1);
    if (!r) r = show_statement(b, l + 1);
    if (!r) r = other_statement(b, l + 1);
    return r;
  }

  // (';')?
  private static boolean statement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement_1")) return false;
    statement_1_0(b, l + 1);
    return true;
  }

  // (';')
  private static boolean statement_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ";");
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !('SELECT' | 'SHOW' | 'CREATE' | 'DROP' | 'DELETE' | 'GRANT' | 'REVOKE')
  static boolean statement_recovery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement_recovery")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !statement_recovery_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // 'SELECT' | 'SHOW' | 'CREATE' | 'DROP' | 'DELETE' | 'GRANT' | 'REVOKE'
  private static boolean statement_recovery_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement_recovery_0")) return false;
    boolean r;
    r = consumeToken(b, "SELECT");
    if (!r) r = consumeToken(b, "SHOW");
    if (!r) r = consumeToken(b, "CREATE");
    if (!r) r = consumeToken(b, "DROP");
    if (!r) r = consumeToken(b, "DELETE");
    if (!r) r = consumeToken(b, "GRANT");
    if (!r) r = consumeToken(b, "REVOKE");
    return r;
  }

  /* ********************************************************** */
  // identifierMy
  public static boolean tag_clause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tag_clause")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifierMy(b, l + 1);
    exit_section_(b, m, TAG_CLAUSE, r);
    return r;
  }

  /* ********************************************************** */
  // 'time' '(' DURATION_LITERAL ')'
  public static boolean time_clause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "time_clause")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TIME_CLAUSE, "<time clause>");
    r = consumeToken(b, "time");
    r = r && consumeToken(b, "(");
    r = r && consumeToken(b, DURATION_LITERAL);
    r = r && consumeToken(b, ")");
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ('-'|'+')? primary_expression
  static boolean unary_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_expression")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = unary_expression_0(b, l + 1);
    r = r && primary_expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('-'|'+')?
  private static boolean unary_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_expression_0")) return false;
    unary_expression_0_0(b, l + 1);
    return true;
  }

  // '-'|'+'
  private static boolean unary_expression_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_expression_0_0")) return false;
    boolean r;
    r = consumeToken(b, "-");
    if (!r) r = consumeToken(b, "+");
    return r;
  }

  /* ********************************************************** */
  // expression
  public static boolean where_clause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "where_clause")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, WHERE_CLAUSE, "<where clause>");
    r = expression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

}
