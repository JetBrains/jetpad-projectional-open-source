package jetbrains.jetpad.projectional.demo.lambda.model;

import jetbrains.jetpad.model.children.ChildProperty;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;

public class LambdaExpr extends Expr {
  public final Property<String> varName = new ValueProperty<String>();
  public final Property<Expr> body = new ChildProperty<LambdaExpr, Expr>(this);
}
