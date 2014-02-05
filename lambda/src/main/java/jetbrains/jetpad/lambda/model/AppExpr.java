package jetbrains.jetpad.lambda.model;

import jetbrains.jetpad.model.children.ChildProperty;
import jetbrains.jetpad.model.property.Property;

public class AppExpr extends Expr {
  public final Property<Expr> fun = new ChildProperty<LambdaNode, Expr>(this);
  public final Property<Expr> arg = new ChildProperty<LambdaNode, Expr>(this);

  @Override
  public Expr copy() {
    AppExpr result = new AppExpr();
    result.fun.set(copy(fun.get()));
    result.arg.set(copy(arg.get()));
    return result;
  }

  @Override
  public String toString() {
    return "(app " + fun.get() + " " + arg.get() + ")";
  }
}
