package jetbrains.jetpad.projectional.demo.lambda.model;

import jetbrains.jetpad.model.children.ChildProperty;
import jetbrains.jetpad.model.property.Property;

public class AppExpr extends Expr {
  public final Property<Expr> fun = new ChildProperty<LambdaNode, Expr>(this);
  public final Property<Expr> arg = new ChildProperty<LambdaNode, Expr>(this);
}
