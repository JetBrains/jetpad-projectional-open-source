package jetbrains.jetpad.hybrid;

import jetbrains.jetpad.hybrid.parser.Token;

public interface PairSpec {
  public static final PairSpec EMPTY = new EmptyPairSpec();

  boolean isLeft(Token t);
  boolean isRight(Token t);
  boolean isPair(Token l, Token r);
}
