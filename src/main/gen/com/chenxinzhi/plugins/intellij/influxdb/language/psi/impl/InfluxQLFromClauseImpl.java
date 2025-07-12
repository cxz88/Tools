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

public class InfluxQLFromClauseImpl extends ASTWrapperPsiElement implements InfluxQLFromClause {

  public InfluxQLFromClauseImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull InfluxQLVisitor visitor) {
    visitor.visitFromClause(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof InfluxQLVisitor) accept((InfluxQLVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<InfluxQLMeasurement> getMeasurementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, InfluxQLMeasurement.class);
  }

}
