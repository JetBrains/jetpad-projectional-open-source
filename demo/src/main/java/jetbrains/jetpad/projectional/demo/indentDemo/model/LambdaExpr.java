package jetbrains.jetpad.projectional.demo.indentDemo.model;

import jetbrains.jetpad.model.children.ChildProperty;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;

public class LambdaExpr extends Expr {
  public final Property<String> varName = new ValueProperty<String>();
  public final Property<Expr> body = new ChildProperty<LambdaExpr, Expr>(this);

  @Override
  public Expr copy() {
    LambdaExpr result = new LambdaExpr();
    result.varName.set(varName.get());
    result.body.set(copy(body.get()));
    return result;
  }

  @Override
  public String toString() {
    return "(lambda " + varName.get() + " -> " + body.get() + ")";
  }
}
