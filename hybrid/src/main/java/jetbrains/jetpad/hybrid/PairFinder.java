package jetbrains.jetpad.hybrid;

import jetbrains.jetpad.hybrid.parser.Token;

public interface PairFinder {
  public static final PairFinder EMPTY = new PairFinder() {
    @Override public boolean hasPair(Token t) { return false; }
    @Override public boolean isPair(Token t1, Token t2) { return false; }
  };

  boolean hasPair(Token t);
  boolean isPair(Token t1, Token t2);
}
