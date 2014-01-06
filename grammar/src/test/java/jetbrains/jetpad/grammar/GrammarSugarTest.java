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
package jetbrains.jetpad.grammar;

import jetbrains.jetpad.grammar.lr.LRParser;
import jetbrains.jetpad.grammar.slr.SLRTableGenerator;
import org.junit.Test;

import static jetbrains.jetpad.grammar.GrammarSugar.*;
import static jetbrains.jetpad.grammar.GrammarTestUtil.asTokens;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class GrammarSugarTest {
  @Test
  public void sequence() {
    Grammar g = new Grammar();
    Terminal id = g.newTerminal("id");
    g.newRule(g.getStart(), seq(id, id));

    LRParser parser = new LRParser(new SLRTableGenerator(g).generateTable());

    assertEquals("[id, id]", "" + parser.parse(asTokens(id, id)));
    assertFalse(parser.parse(id));
  }

  @Test
  public void optionalPart() {
    Grammar g = new Grammar();
    Terminal id = g.newTerminal("id");
    Terminal error = g.newTerminal("error");
    g.newRule(g.getStart(), optional(id));

    LRParser parser = new LRParser(new SLRTableGenerator(g).generateTable());

    assertEquals("[id]", "" + parser.parse(asTokens(id)));
    assertEquals("[]", "" + parser.parse(asTokens()));
    assertFalse(parser.parse(error));
  }

  @Test
  public void starPart() {
    Grammar g = new Grammar();
    Terminal id = g.newTerminal("id");
    Terminal error = g.newTerminal("error");
    g.newRule(g.getStart(), star(id));

    LRParser parser = new LRParser(new SLRTableGenerator(g).generateTable());

    assertFalse(parser.parse(error));
    assertEquals("[]", "" + parser.parse(asTokens(new Terminal[0])));
    assertEquals("[id, id, id]", "" + parser.parse(asTokens(id, id, id)));
  }

  @Test
  public void plusPart() {
    Grammar g = new Grammar();
    Terminal id = g.newTerminal("id");
    Terminal error = g.newTerminal("error");
    g.newRule(g.getStart(), plus(id));

    LRParser parser = new LRParser(new SLRTableGenerator(g).generateTable());

    assertFalse(parser.parse(error));
    assertFalse(parser.parse(new Terminal[0]));
    assertEquals("[id, id, id]", "" + parser.parse(asTokens(id, id, id)));
  }

  @Test
  public void separatedPart() {
    Grammar g = new Grammar();
    Terminal id = g.newTerminal("id");
    Terminal comma = g.newTerminal(",");
    g.newRule(g.getStart(), separated(id, comma));

    LRParser parser = new LRParser(new SLRTableGenerator(g).generateTable());

    assertFalse(parser.parse(comma));
    assertEquals("[]", "" + parser.parse(asTokens()));
    assertEquals("[id]", "" + parser.parse(asTokens(id)));
    assertEquals("[id, id]", "" + parser.parse(asTokens(id, comma, id)));
  }
}