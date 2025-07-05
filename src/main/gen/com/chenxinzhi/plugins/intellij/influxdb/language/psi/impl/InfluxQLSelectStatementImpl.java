// This is a generated file. Not intended for manual editing.
package com.chenxinzhi.plugins.intellij.influxdb.language.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.chenxinzhi.plugins.intellij.influxdb.language.psi.InfluxQLTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.chenxinzhi.plugins.intellij.influxdb.language.psi.*;

public class InfluxQLSelectStatementImpl extends ASTWrapperPsiElement implements InfluxQLSelectStatement {

  public InfluxQLSelectStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull InfluxQLVisitor visitor) {
    visitor.visitSelectStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof InfluxQLVisitor) accept((InfluxQLVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public InfluxQLFieldList getFieldList() {
    return findNotNullChildByClass(InfluxQLFieldList.class);
  }

  @Override
  @NotNull
  public InfluxQLFromClause getFromClause() {
    return findNotNullChildByClass(InfluxQLFromClause.class);
  }

  @Override
  @Nullable
  public InfluxQLGroupByClause getGroupByClause() {
    return findChildByClass(InfluxQLGroupByClause.class);
  }

  @Override
  @Nullable
  public InfluxQLLimitClause getLimitClause() {
    return findChildByClass(InfluxQLLimitClause.class);
  }

  @Override
  @Nullable
  public InfluxQLOffsetClause getOffsetClause() {
    return findChildByClass(InfluxQLOffsetClause.class);
  }

  @Override
  @Nullable
  public InfluxQLOrderByClause getOrderByClause() {
    return findChildByClass(InfluxQLOrderByClause.class);
  }

  @Override
  @Nullable
  public InfluxQLSlimitClause getSlimitClause() {
    return findChildByClass(InfluxQLSlimitClause.class);
  }

  @Override
  @Nullable
  public InfluxQLSoffsetClause getSoffsetClause() {
    return findChildByClass(InfluxQLSoffsetClause.class);
  }

  @Override
  @Nullable
  public InfluxQLWhereClause getWhereClause() {
    return findChildByClass(InfluxQLWhereClause.class);
  }

}
