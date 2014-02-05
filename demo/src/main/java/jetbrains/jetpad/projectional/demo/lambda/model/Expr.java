package jetbrains.jetpad.projectional.demo.lambda.model;

public abstract class Expr extends LambdaNode {
  public abstract Expr copy();

  protected Expr copy(Expr e) {
    if (e == null) return null;
    return e.copy();
  }
}
