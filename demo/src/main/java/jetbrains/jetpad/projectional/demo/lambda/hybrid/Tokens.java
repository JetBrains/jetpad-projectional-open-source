package jetbrains.jetpad.projectional.demo.lambda.hybrid;

import jetbrains.jetpad.hybrid.parser.IdentifierToken;
import jetbrains.jetpad.hybrid.parser.SimpleToken;
import jetbrains.jetpad.hybrid.parser.Token;

class Tokens {
  static final Token LP = new SimpleToken("(");
  static final Token RP = new SimpleToken(")");
  static final Token WILDCARD = new SimpleToken("?");
}
