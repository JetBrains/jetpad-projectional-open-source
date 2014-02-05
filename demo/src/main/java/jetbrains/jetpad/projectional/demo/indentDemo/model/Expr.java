package jetbrains.jetpad.projectional.demo.indentDemo.model;

public abstract class Expr extends LambdaNode {
  public abstract Expr copy();

  protected Expr copy(Expr e) {
    if (e == null) return null;
    return e.copy();
  }
}
