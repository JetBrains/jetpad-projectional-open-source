package jetbrains.jetpad.hybrid.testapp.mapper;

import jetbrains.jetpad.hybrid.parser.ValueToken;
import jetbrains.jetpad.hybrid.testapp.model.*;

public class ValueExprTextGen implements ValueToken.ValueTextGen<Expr> {
  @Override
  public String toText(Expr val) {
    if (val instanceof StringExpr) {
      return val.toString();
    }

    throw new UnsupportedOperationException();
  }
}
