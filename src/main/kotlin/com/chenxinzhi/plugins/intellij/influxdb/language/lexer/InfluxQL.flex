package com.chenxinzhi.plugins.intellij.influxdb.language.psi;

import com.intellij.psi.tree.IElementType;

import static com.chenxinzhi.plugins.intellij.influxdb.language.psi.InfluxQLTypes.*;

%%
%public
%class _InfluxQLLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

%{
  private char yycharat(int pos) { return 0; }  public _InfluxQLLexer() {
    this((java.io.Reader)null);
  }
%}

CRLF=\R
WHITE_SPACE=[\ \t\f]+
LINE_COMMENT=--[^\r\n]*
IDENTIFIER=([a-zA-Z_][a-zA-Z0-9_]*)|(\"[^\"]+\")|('[^']+')
INTEGER_LITERAL=[0-9]+
FLOAT_LITERAL=([0-9]+\.[0-9]*)|(\.[0-9]+)|([0-9]+[eE][+-]?[0-9]+)
DURATION_LITERAL=[0-9]+(u|µ|ms|s|m|h|d|w)
BOOLEAN_LITERAL=("true"|"false")

%state STRING

%%

<YYINITIAL> {
  {WHITE_SPACE}              { return com.intellij.psi.TokenType.WHITE_SPACE; }
  {CRLF}                     { return com.intellij.psi.TokenType.WHITE_SPACE; }
  {LINE_COMMENT}             { return InfluxQLTypes.COMMENT; }

  /* Keywords */
  "SELECT"                   { return SELECT; }
  "FROM"                     { return FROM; }
  "WHERE"                    { return WHERE; }
  "GROUP"                    { return GROUP; }
  "BY"                       { return BY; }
  "ORDER"                    { return ORDER; }
  "LIMIT"                    { return LIMIT; }
  "SLIMIT"                   { return SLIMIT; }
  "OFFSET"                   { return OFFSET; }
  "SOFFSET"                  { return SOFFSET; }
  "INTO"                     { return INTO; }
  "CREATE"                   { return CREATE; }
  "DATABASE"                 { return DATABASE; }
  "DROP"                     { return DROP; }
  "DELETE"                   { return DELETE; }
  "SHOW"                     { return SHOW; }
  "DATABASES"                { return DATABASES; }
  "MEASUREMENTS"             { return MEASUREMENTS; }
  "SERIES"                   { return SERIES; }
  "FIELD"                    { return FIELD; }
  "KEYS"                     { return KEYS; }
  "TAG"                      { return TAG; }
  "CONTINUOUS"               { return CONTINUOUS; }
  "QUERIES"                  { return QUERIES; }
  "QUERY"                    { return QUERY; }
  "RETENTION"                { return RETENTION; }
  "POLICY"                   { return POLICY; }
  "ON"                       { return ON; }
  "RESAMPLE"                 { return RESAMPLE; }
  "FOR"                      { return FOR; }
  "BEGIN"                    { return BEGIN_STMT; }
  "END"                      { return END_STMT; }
  "ALL"                      { return ALL; }
  "ANY"                      { return ANY_KEYWORD; }
  "AS"                       { return AS; }
  "ASC"                      { return ASC; }
  "DESC"                     { return DESC; }
  "DISTINCT"                 { return DISTINCT; }
  "GRANT"                    { return GRANT; }
  "REVOKE"                   { return REVOKE; }
  "USER"                     { return USER; }
  "USERS"                    { return USERS; }
  "WITH"                     { return WITH; }
  "TO"                       { return TO; }

  /* Operators */
  "="                        { return EQ; }
  "!="                       { return NEQ; }
  "<>"                       { return NEQ; }
  "<"                        { return LT; }
  "<="                       { return LTE; }
  ">"                        { return GT; }
  ">="                       { return GTE; }
  "+"                        { return PLUS; }
  "-"                        { return MINUS; }
  "*"                        { return MUL; }
  "/"                        { return DIV; }
  "AND"                      { return AND; }
  "OR"                       { return OR; }
  "=~"                       { return REGEQ; }
  "!~"                       { return REGNEQ; }
  "\""                       { return DOUBLE_QUOTES; }
  "'"                        { return SINGLE_QUOTES; }


  /* Delimiters */
  "("                        { return LPAREN; }
  ")"                        { return RPAREN; }
  ","                        { return COMMA; }
  "."                        { return DOT; }
  ";"                        { return SEMICOLON; }



  /* Literals */
  {BOOLEAN_LITERAL}          { return BOOLEAN_LITERAL; }
  {DURATION_LITERAL}         { return DURATION_LITERAL; }
  {FLOAT_LITERAL}            { return NUMBER_LITERAL; }
  {INTEGER_LITERAL}          { return NUMBER_LITERAL; }

  {IDENTIFIER}               { return IDENTIFIER; }


}

[^] { return com.intellij.psi.TokenType.BAD_CHARACTER; }