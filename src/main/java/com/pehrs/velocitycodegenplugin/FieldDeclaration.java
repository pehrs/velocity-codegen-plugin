package com.pehrs.velocitycodegenplugin;

import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.impl.java.stubs.JavaStubElementTypes;
import com.intellij.psi.impl.source.PsiFieldImpl;
import org.jetbrains.annotations.Nullable;

public class FieldDeclaration {

  public final String type;
  public final String name;

  public final boolean isStatic;

  public final boolean isCollection;
  public final boolean isSet;
  public final boolean isList;

  public final boolean isArray;

  public final boolean isPublic;
  public final boolean isPrivate;
  public final boolean isProtected;

  public FieldDeclaration(String type, String name, boolean isStatic, boolean isCollection,
      boolean isSet, boolean isList, boolean isArray, boolean isPublic, boolean isPrivate,
      boolean isProtected) {
    this.type = type;
    this.name = name;
    this.isStatic = isStatic;
    this.isCollection = isCollection;
    this.isSet = isSet;
    this.isList = isList;
    this.isArray = isArray;
    this.isPublic = isPublic;
    this.isPrivate = isPrivate;
    this.isProtected = isProtected;
  }

  public String getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public boolean isStatic() {
    return isStatic;
  }

  public boolean isCollection() {
    return isCollection;
  }

  public boolean isSet() {
    return isSet;
  }

  public boolean isList() {
    return isList;
  }

  public boolean isArray() {
    return isArray;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public boolean isPrivate() {
    return isPrivate;
  }

  public boolean isProtected() {
    return isProtected;
  }

  public static FieldDeclaration fromPsiField(PsiField psiField) {
    String name = psiField.getName();
    String type = psiField.getType().getCanonicalText();

    if (psiField instanceof PsiFieldImpl psiFieldImpl) {
      @Nullable PsiModifierList mods =
          psiFieldImpl.getStubOrPsiChild(JavaStubElementTypes.MODIFIER_LIST);

      boolean isStatic = mods.hasModifierProperty("static");
      boolean isPublic = mods.hasModifierProperty("public");
      boolean isPrivate = mods.hasModifierProperty("private");
      boolean isProtected = mods.hasModifierProperty("protected");

      boolean isArray = psiFieldImpl.getType().getArrayDimensions() > 0;

      boolean isList = type.startsWith("java.util.List");
      boolean isSet = type.startsWith("java.util.Set");
      boolean isCollection = isList || isSet;

      return new FieldDeclaration(type, name, isStatic, isCollection, isSet, isList, isArray, isPublic, isPrivate, isProtected);
    } else {
      throw new IllegalStateException("Not supported PsiField: " + psiField.getClass().getName());
    }
  }

}
