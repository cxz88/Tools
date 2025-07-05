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

public class InfluxQLFunctionCallImpl extends ASTWrapperPsiElement implements InfluxQLFunctionCall {

  public InfluxQLFunctionCallImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull InfluxQLVisitor visitor) {
    visitor.visitFunctionCall(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof InfluxQLVisitor) accept((InfluxQLVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<InfluxQLFunctionCall> getFunctionCallList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, InfluxQLFunctionCall.class);
  }

  @Override
  @NotNull
  public List<InfluxQLIdentifierMy> getIdentifierMyList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, InfluxQLIdentifierMy.class);
  }

  @Override
  @NotNull
  public List<InfluxQLLiteral> getLiteralList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, InfluxQLLiteral.class);
  }

}
