package jetbrains.jetpad.hybrid.parser;

import java.util.List;

public class TokenUtil {
  public static String join(List<Token> tokens) {
    StringBuilder joined = new StringBuilder();
    Token prevToken = null;
    for (Token currToken : tokens) {
      if (prevToken != null && !prevToken.noSpaceToRight() && !currToken.noSpaceToLeft()) {
        joined.append(' ');
      }
      // NB: Token.text() may throw UnsupportedOperationException
      joined.append(currToken.text());
      prevToken = currToken;
    }
    return joined.toString();
  }

  private TokenUtil() {
  }
}
