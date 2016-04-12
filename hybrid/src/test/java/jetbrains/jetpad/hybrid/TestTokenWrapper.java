package jetbrains.jetpad.hybrid;

import jetbrains.jetpad.hybrid.parser.Token;

public class TestTokenWrapper {
  public final Token token;

  public TestTokenWrapper(Token token) {
    this.token = token;
  }

  @Override
  public String toString() {
    return token.toString();
  }
}
