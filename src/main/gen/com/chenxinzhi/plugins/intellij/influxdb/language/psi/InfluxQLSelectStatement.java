// This is a generated file. Not intended for manual editing.
package com.chenxinzhi.plugins.intellij.influxdb.language.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface InfluxQLSelectStatement extends PsiElement {

  @NotNull
  InfluxQLFieldList getFieldList();

  @NotNull
  InfluxQLFromClause getFromClause();

  @Nullable
  InfluxQLGroupByClause getGroupByClause();

  @Nullable
  InfluxQLLimitClause getLimitClause();

  @Nullable
  InfluxQLOffsetClause getOffsetClause();

  @Nullable
  InfluxQLOrderByClause getOrderByClause();

  @Nullable
  InfluxQLSlimitClause getSlimitClause();

  @Nullable
  InfluxQLSoffsetClause getSoffsetClause();

  @Nullable
  InfluxQLWhereClause getWhereClause();

}
