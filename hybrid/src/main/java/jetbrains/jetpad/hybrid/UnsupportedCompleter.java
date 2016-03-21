package jetbrains.jetpad.hybrid;

import jetbrains.jetpad.hybrid.parser.Token;

final class UnsupportedCompleter extends BaseCompleter {

  UnsupportedCompleter() {
  }

  @Override
  public Runnable complete(int selectionIndex, Token... tokens) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Runnable completeTerminatorToken(TerminatorToken<?> terminatorToken) {
    throw new UnsupportedOperationException();
  }

}
