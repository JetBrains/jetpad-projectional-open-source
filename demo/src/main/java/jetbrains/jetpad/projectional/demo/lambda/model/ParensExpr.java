package jetbrains.jetpad.projectional.demo.lambda.model;

import jetbrains.jetpad.model.children.ChildProperty;
import jetbrains.jetpad.model.property.Property;

public class ParensExpr extends Expr {
  public final Property<Expr> expr = new ChildProperty<LambdaNode, Expr>(this);

  @Override
  public String toString() {
    return "(parens " + expr.get() + ")";
  }
}
