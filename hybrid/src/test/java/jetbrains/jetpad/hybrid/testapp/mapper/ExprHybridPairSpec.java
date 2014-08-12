package jetbrains.jetpad.hybrid.testapp.mapper;

import jetbrains.jetpad.hybrid.PairSpec;
import jetbrains.jetpad.hybrid.parser.Token;

class ExprHybridPairSpec implements PairSpec {
  @Override
  public boolean isLeft(Token t) {
    return t == Tokens.LP || t == Tokens.LP_CALL;
  }

  @Override
  public boolean isRight(Token t) {
    return t == Tokens.RP;
  }

  @Override
  public boolean isPair(Token l, Token r) {
    return (l == Tokens.LP || l == Tokens.LP_CALL) && r == Tokens.RP;
  }
}
