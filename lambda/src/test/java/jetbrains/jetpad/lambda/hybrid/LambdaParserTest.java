/*
 * Copyright 2012-2014 JetBrains s.r.o
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.jetpad.lambda.hybrid;

import jetbrains.jetpad.hybrid.parser.IdentifierToken;
import jetbrains.jetpad.hybrid.parser.ParsingContext;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.lambda.model.Expr;
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