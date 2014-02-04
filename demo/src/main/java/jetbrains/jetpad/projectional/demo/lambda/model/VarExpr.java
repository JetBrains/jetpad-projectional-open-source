package jetbrains.jetpad.projectional.demo.lambda.model;

import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;

public class VarExpr extends Expr {
  public final Property<String> name = new ValueProperty<String>();
}
