package jetbrains.jetpad.projectional.demo.lambda.hybrid;

import jetbrains.jetpad.hybrid.parser.IdentifierToken;
import jetbrains.jetpad.hybrid.parser.ParsingContext;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.projectional.demo.lambda.model.Expr;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class LambdaParserTest {
  @Test
  public void simpleId() {
    assertParsed("y", new IdentifierToken("y"));
  }

  @Test
  public void appAssociativity() {
    assertParsed("(app (app x y) z)", new IdentifierToken("x"), new IdentifierToken("y"), new IdentifierToken("z"));
  }

  private void assertParsed(String textRep, Token... tokens) {
    Expr expr = LambdaParser.EXPR.parse(new ParsingContext(Arrays.asList(tokens)));
    assertEquals(textRep, expr.toString());
  }


}
