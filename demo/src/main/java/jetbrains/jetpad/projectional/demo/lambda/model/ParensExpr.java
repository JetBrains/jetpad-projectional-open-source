package jetbrains.jetpad.projectional.demo.lambda.model;

import jetbrains.jetpad.model.children.ChildProperty;
import jetbrains.jetpad.model.property.Property;

public class ParensExpr extends Expr {
  public final Property<Expr> expr = new ChildProperty<LambdaNode, Expr>(this);

  @Override
  public Expr copy() {
    ParensExpr result = new ParensExpr();
    result.expr.set(copy(expr.get()));
    return result;
  }

  @Override
  public String toString() {
    return "(parens " + expr.get() + ")";
  }
}
