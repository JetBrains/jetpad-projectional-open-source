package jetbrains.jetpad.hybrid;

import jetbrains.jetpad.hybrid.parser.Token;

class EmptyPairSpec implements PairSpec {
  @Override
  public boolean isLeft(Token t) {
    return false;
  }

  @Override
  public boolean isRight(Token t) {
    return false;
  }

  @Override
  public boolean isPair(Token l, Token r) {
    return false;
  }
}
