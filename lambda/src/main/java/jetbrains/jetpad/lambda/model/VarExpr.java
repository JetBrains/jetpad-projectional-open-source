package jetbrains.jetpad.lambda.model;

import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;

public class VarExpr extends Expr {
  public final Property<String> name = new ValueProperty<String>();

  @Override
  public Expr copy() {
    VarExpr result = new VarExpr();
    result.name.set(name.get());
    return result;
  }

  @Override
  public String toString() {
    return name.get();
  }
}
