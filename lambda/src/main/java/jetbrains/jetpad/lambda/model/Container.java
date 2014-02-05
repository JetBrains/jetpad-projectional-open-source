package jetbrains.jetpad.lambda.model;

import jetbrains.jetpad.model.children.ChildProperty;
import jetbrains.jetpad.model.property.Property;

public class Container extends LambdaNode {
  public final Property<Expr> expr = new ChildProperty<LambdaNode, Expr>(this);
}
