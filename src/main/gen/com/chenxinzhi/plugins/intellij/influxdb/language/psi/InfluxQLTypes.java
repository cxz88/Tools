// This is a generated file. Not intended for manual editing.
package com.chenxinzhi.plugins.intellij.influxdb.language.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.chenxinzhi.plugins.intellij.influxdb.language.psi.impl.*;

public interface InfluxQLTypes {

  IElementType ALIAS = new InfluxQLElementType("ALIAS");
  IElementType FIELD_CLAUSE = new InfluxQLElementType("FIELD_CLAUSE");
  IElementType FIELD_LIST = new InfluxQLElementType("FIELD_LIST");
  IElementType FROM_CLAUSE = new InfluxQLElementType("FROM_CLAUSE");
  IElementType FUNCTION_CALL = new InfluxQLElementType("FUNCTION_CALL");
  IElementType GROUP_BY_CLAUSE = new InfluxQLElementType("GROUP_BY_CLAUSE");
  IElementType IDENTIFIER_MY = new InfluxQLElementType("IDENTIFIER_MY");
  IElementType LIMIT_CLAUSE = new InfluxQLElementType("LIMIT_CLAUSE");
  IElementType LITERAL = new InfluxQLElementType("LITERAL");
  IElementType MEASUREMENT = new InfluxQLElementType("MEASUREMENT");
  IElementType OFFSET_CLAUSE = new InfluxQLElementType("OFFSET_CLAUSE");
  IElementType ORDER_BY_CLAUSE = new InfluxQLElementType("ORDER_BY_CLAUSE");
  IElementType OTHER_STATEMENT = new InfluxQLElementType("OTHER_STATEMENT");
  IElementType SELECT_STATEMENT = new InfluxQLElementType("SELECT_STATEMENT");
  IElementType SHOW_STATEMENT = new InfluxQLElementType("SHOW_STATEMENT");
  IElementType SLIMIT_CLAUSE = new InfluxQLElementType("SLIMIT_CLAUSE");
  IElementType SOFFSET_CLAUSE = new InfluxQLElementType("SOFFSET_CLAUSE");
  IElementType TAG_CLAUSE = new InfluxQLElementType("TAG_CLAUSE");
  IElementType TIME_CLAUSE = new InfluxQLElementType("TIME_CLAUSE");
  IElementType WHERE_CLAUSE = new InfluxQLElementType("WHERE_CLAUSE");

  IElementType ALL = new InfluxQLTokenType("ALL");
  IElementType AND = new InfluxQLTokenType("AND");
  IElementType ANY_KEYWORD = new InfluxQLTokenType("ANY_KEYWORD");
  IElementType AS = new InfluxQLTokenType("AS");
  IElementType ASC = new InfluxQLTokenType("ASC");
  IElementType BEGIN_STMT = new InfluxQLTokenType("BEGIN_STMT");
  IElementType BOOLEAN_LITERAL = new InfluxQLTokenType("BOOLEAN_LITERAL");
  IElementType BY = new InfluxQLTokenType("BY");
  IElementType COMMA = new InfluxQLTokenType("COMMA");
  IElementType COMMENT = new InfluxQLTokenType("comment");
  IElementType CONTINUOUS = new InfluxQLTokenType("CONTINUOUS");
  IElementType CREATE = new InfluxQLTokenType("CREATE");
  IElementType DATABASE = new InfluxQLTokenType("DATABASE");
  IElementType DATABASES = new InfluxQLTokenType("DATABASES");
  IElementType DELETE = new InfluxQLTokenType("DELETE");
  IElementType DESC = new InfluxQLTokenType("DESC");
  IElementType DISTINCT = new InfluxQLTokenType("DISTINCT");
  IElementType DIV = new InfluxQLTokenType("DIV");
  IElementType DOT = new InfluxQLTokenType("DOT");
  IElementType DOUBLE_QUOTES = new InfluxQLTokenType("\"");
  IElementType DROP = new InfluxQLTokenType("DROP");
  IElementType DURATION_LITERAL = new InfluxQLTokenType("DURATION_LITERAL");
  IElementType END_STMT = new InfluxQLTokenType("END_STMT");
  IElementType EQ = new InfluxQLTokenType("EQ");
  IElementType FIELD = new InfluxQLTokenType("FIELD");
  IElementType FOR = new InfluxQLTokenType("FOR");
  IElementType FROM = new InfluxQLTokenType("FROM");
  IElementType GRANT = new InfluxQLTokenType("GRANT");
  IElementType GROUP = new InfluxQLTokenType("GROUP");
  IElementType GT = new InfluxQLTokenType("GT");
  IElementType GTE = new InfluxQLTokenType("GTE");
  IElementType IDENTIFIER = new InfluxQLTokenType("IDENTIFIER");
  IElementType INTEGER_LITERAL = new InfluxQLTokenType("INTEGER_LITERAL");
  IElementType INTO = new InfluxQLTokenType("INTO");
  IElementType KEYS = new InfluxQLTokenType("KEYS");
  IElementType LIMIT = new InfluxQLTokenType("LIMIT");
  IElementType LPAREN = new InfluxQLTokenType("LPAREN");
  IElementType LT = new InfluxQLTokenType("LT");
  IElementType LTE = new InfluxQLTokenType("LTE");
  IElementType MEASUREMENTS = new InfluxQLTokenType("MEASUREMENTS");
  IElementType MINUS = new InfluxQLTokenType("MINUS");
  IElementType MUL = new InfluxQLTokenType("MUL");
  IElementType NEQ = new InfluxQLTokenType("NEQ");
  IElementType NUMBER_LITERAL = new InfluxQLTokenType("NUMBER_LITERAL");
  IElementType OFFSET = new InfluxQLTokenType("OFFSET");
  IElementType ON = new InfluxQLTokenType("ON");
  IElementType OR = new InfluxQLTokenType("OR");
  IElementType ORDER = new InfluxQLTokenType("ORDER");
  IElementType PLUS = new InfluxQLTokenType("PLUS");
  IElementType POLICY = new InfluxQLTokenType("POLICY");
  IElementType QUERIES = new InfluxQLTokenType("QUERIES");
  IElementType QUERY = new InfluxQLTokenType("QUERY");
  IElementType REGEQ = new InfluxQLTokenType("REGEQ");
  IElementType REGNEQ = new InfluxQLTokenType("REGNEQ");
  IElementType RESAMPLE = new InfluxQLTokenType("RESAMPLE");
  IElementType RETENTION = new InfluxQLTokenType("RETENTION");
  IElementType REVOKE = new InfluxQLTokenType("REVOKE");
  IElementType RPAREN = new InfluxQLTokenType("RPAREN");
  IElementType SELECT = new InfluxQLTokenType("SELECT");
  IElementType SEMICOLON = new InfluxQLTokenType("SEMICOLON");
  IElementType SERIES = new InfluxQLTokenType("SERIES");
  IElementType SHOW = new InfluxQLTokenType("SHOW");
  IElementType SINGLE_QUOTES = new InfluxQLTokenType("'");
  IElementType SLIMIT = new InfluxQLTokenType("SLIMIT");
  IElementType SOFFSET = new InfluxQLTokenType("SOFFSET");
  IElementType STRING_LITERAL = new InfluxQLTokenType("STRING_LITERAL");
  IElementType TAG = new InfluxQLTokenType("TAG");
  IElementType TO = new InfluxQLTokenType("TO");
  IElementType USER = new InfluxQLTokenType("USER");
  IElementType USERS = new InfluxQLTokenType("USERS");
  IElementType WHERE = new InfluxQLTokenType("WHERE");
  IElementType WITH = new InfluxQLTokenType("WITH");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ALIAS) {
        return new InfluxQLAliasImpl(node);
      }
      else if (type == FIELD_CLAUSE) {
        return new InfluxQLFieldClauseImpl(node);
      }
      else if (type == FIELD_LIST) {
        return new InfluxQLFieldListImpl(node);
      }
      else if (type == FROM_CLAUSE) {
        return new InfluxQLFromClauseImpl(node);
      }
      else if (type == FUNCTION_CALL) {
        return new InfluxQLFunctionCallImpl(node);
      }
      else if (type == GROUP_BY_CLAUSE) {
        return new InfluxQLGroupByClauseImpl(node);
      }
      else if (type == IDENTIFIER_MY) {
        return new InfluxQLIdentifierMyImpl(node);
      }
      else if (type == LIMIT_CLAUSE) {
        return new InfluxQLLimitClauseImpl(node);
      }
      else if (type == LITERAL) {
        return new InfluxQLLiteralImpl(node);
      }
      else if (type == MEASUREMENT) {
        return new InfluxQLMeasurementImpl(node);
      }
      else if (type == OFFSET_CLAUSE) {
        return new InfluxQLOffsetClauseImpl(node);
      }
      else if (type == ORDER_BY_CLAUSE) {
        return new InfluxQLOrderByClauseImpl(node);
      }
      else if (type == OTHER_STATEMENT) {
        return new InfluxQLOtherStatementImpl(node);
      }
      else if (type == SELECT_STATEMENT) {
        return new InfluxQLSelectStatementImpl(node);
      }
      else if (type == SHOW_STATEMENT) {
        return new InfluxQLShowStatementImpl(node);
      }
      else if (type == SLIMIT_CLAUSE) {
        return new InfluxQLSlimitClauseImpl(node);
      }
      else if (type == SOFFSET_CLAUSE) {
        return new InfluxQLSoffsetClauseImpl(node);
      }
      else if (type == TAG_CLAUSE) {
        return new InfluxQLTagClauseImpl(node);
      }
      else if (type == TIME_CLAUSE) {
        return new InfluxQLTimeClauseImpl(node);
      }
      else if (type == WHERE_CLAUSE) {
        return new InfluxQLWhereClauseImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
