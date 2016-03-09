package jetbrains.jetpad.hybrid.testapp.mapper;

import jetbrains.jetpad.hybrid.parser.ValueToken;
import jetbrains.jetpad.hybrid.testapp.model.*;

public class ValueExprCloner implements ValueToken.ValueCloner<Expr> {
  @Override
  public Expr clone(Expr val) {
    if (val instanceof ValueExpr) {
      ValueExpr result = new ValueExpr();
      result.val.set(((ValueExpr) val).val.get());
      return result;
    }

    if (val instanceof ComplexValueExpr) {
      return new ComplexValueExpr();
    }

    if (val instanceof PosValueExpr) {
      return new PosValueExpr();
    }

    if (val instanceof AsyncValueExpr) {
      return new AsyncValueExpr();
    }

    if (val instanceof StringExpr) {
      return new StringExpr((StringExpr) val);
    }

    throw new IllegalArgumentException(val.toString());
  }
}
