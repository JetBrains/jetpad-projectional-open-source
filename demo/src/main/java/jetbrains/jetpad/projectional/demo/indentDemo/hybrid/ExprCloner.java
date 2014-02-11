package jetbrains.jetpad.projectional.demo.indentDemo.hybrid;

import jetbrains.jetpad.hybrid.parser.ValueToken;
import jetbrains.jetpad.projectional.demo.indentDemo.model.Expr;

class ExprCloner implements ValueToken.ValueCloner<Expr> {
  @Override
  public Expr clone(Expr val) {
    return val.copy();
  }
}
