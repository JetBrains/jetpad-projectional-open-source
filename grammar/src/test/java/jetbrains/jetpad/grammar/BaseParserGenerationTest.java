/*
 * Copyright 2012-2016 JetBrains s.r.o
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

import com.google.common.collect.Range;
import jetbrains.jetpad.grammar.parser.LRParser;
import jetbrains.jetpad.grammar.parser.LRParserTable;
import jetbrains.jetpad.grammar.parser.Lexeme;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static jetbrains.jetpad.grammar.GrammarTestUtil.asLexemes;
import static org.junit.Assert.*;

public abstract class BaseParserGenerationTest {
  protected abstract LRParserTable generateTable(Grammar g);

  @Test(expected = IllegalArgumentException.class)
  public void emptyGrammarInvalid() {
    Grammar g = new Grammar();
    generateTable(g);
  }


  @Test(expected = IllegalArgumentException.class)
  public void grammarWithTerminalStartRuleInvalid() {
    Grammar g = new Grammar();
    g.newRule(g.getStart(), g.newTerminal("id"));
    generateTable(g);
  }

  @Test(expected = IllegalArgumentException.class)
  public void grammarWithTooManyRulesInvalid() {
    Grammar g = new Grammar();
    g.newRule(g.getStart(), g.newNonTerminal("x"));
    g.newRule(g.getStart(), g.newNonTerminal("y"));
    generateTable(g);
  }

  @Test
  public void parserGenerationAndParsing() {
    Grammar g = new Grammar();

    NonTerminal start = g.getStart();
    NonTerminal expr = g.newNonTerminal("E");
    NonTerminal term = g.newNonTerminal("T");
    NonTerminal fact = g.newNonTerminal("F");

    Terminal id = g.newTerminal("id");
    Terminal plus = g.newTerminal("+");
    Terminal star = g.newTerminal("*");
    Terminal lp = g.newTerminal("(");
    Terminal rp = g.newTerminal(")");

    g.newRule(start, expr);
    g.newRule(expr, expr, plus, term);
    g.newRule(expr, term);
    g.newRule(term, term, star, fact);
    g.newRule(term, fact);
    g.newRule(fact, id);
    g.newRule(fact, lp, expr, rp);

    LRParserTable table = generateTable(g);

    LRParser parser = new LRParser(table);

    assertTrue(parser.parse(id));
    assertFalse(parser.parse(id, plus));
    assertTrue(parser.parse(id, plus, id));
    assertTrue(parser.parse(lp, id, rp));
  }

  @Test(expected = IllegalStateException.class)
  public void ambiguityDetection() {
    Grammar g = new Grammar();
    NonTerminal start = g.getStart();
    NonTerminal expr = g.newNonTerminal("E");
    Symbol plus = g.newTerminal("+");
    Symbol id = g.newTerminal("id");

    g.newRule(start, expr);
    g.newRule(expr, plus, expr);
    g.newRule(expr, expr, plus, expr);
    g.newRule(expr, id);

    generateTable(g);
  }

  @Test
  public void ambiguityResolutionWithAssocPropertyToLeftAssoc() {
    SimplePrecedenceGrammar g = new SimplePrecedenceGrammar();
    g.plusRule.setAssociativity(Associativity.LEFT).setPriority(0);

    LRParser parser = new LRParser(generateTable(g.grammar));
    Object parse = parser.parse(asLexemes(g.id, g.plus, g.id, g.plus, g.id));

    assertEquals("((id + id) + id)", parse.toString());
  }

  @Test
  public void ambiguityResolutionWithAssocPropertyToRightAssoc() {
    SimplePrecedenceGrammar g = new SimplePrecedenceGrammar();
    g.plusRule.setAssociativity(Associativity.RIGHT).setPriority(0);

    LRParser parser = new LRParser(generateTable(g.grammar));
    Object parse = parser.parse(asLexemes(g.id, g.plus, g.id, g.plus, g.id));

    assertEquals("(id + (id + id))", parse.toString());
  }

  @Test
  public void ambiguityResolutionWithPriority() {
    SimplePrecedenceGrammarWithDifferentPriorities g = new SimplePrecedenceGrammarWithDifferentPriorities();

    g.plusRule.setAssociativity(Associativity.LEFT);
    g.plusRule.setPriority(0);

    g.mulRule.setAssociativity(Associativity.LEFT);
    g.mulRule.setPriority(1);

    LRParserTable table = generateTable(g.grammar);
    LRParser parser = new LRParser(table);

    Object parse = parser.parse(asLexemes(g.id, g.plus, g.id, g.mul, g.id));

    assertEquals("(id + (id * id))", parse.toString());
  }

  @Test
  public void positionsDuringParsing() {
    SimplePrecedenceGrammar g = new SimplePrecedenceGrammar();
    g.plusRule.setAssociativity(Associativity.LEFT);
    g.plusRule.setPriority(0);

    LRParserTable table = generateTable(g.grammar);

    LRParser parser = new LRParser(table);
    BinExpr parse = (BinExpr) parser.parse(asLexemes(g.id, g.plus, g.id));

    assertEquals(Range.closed(0, 3), parse.getRange());
    assertEquals(of("id", "+", "id"), parse.getLexemesValues());

    assertEquals(Range.closed(0, 1), parse.left.getRange());
    assertEquals(of("id"), parse.left.getLexemesValues());

    assertEquals(Range.closed(2, 3), parse.right.getRange());
    assertEquals(of("id"), parse.right.getLexemesValues());
  }

  private class SimplePrecedenceGrammar {
    final Grammar grammar = new Grammar();

    final NonTerminal start = grammar.getStart();
    final NonTerminal expr = grammar.newNonTerminal("E");
    final Terminal plus = grammar.newTerminal("+");
    final Terminal id = grammar.newTerminal("id");

    final Rule startRule = grammar.newRule(start, expr);
    final Rule idRule = grammar.newRule(expr, id);
    final Rule plusRule = grammar.newRule(expr, expr, plus, expr);

    {
      startRule.setHandler(new RuleHandler() {
        @Override
        public Object handle(RuleContext ctx) {
          return ctx.get(0);
        }
      });

      idRule.setHandler(new RuleHandler() {
        @Override
        public Object handle(RuleContext ctx) {
          return new IdExpr(ctx.getRange(), ctx.getLexemeValues());
        }
      });

      plusRule.setHandler(new BinOpHandler());
    }

    class BinOpHandler implements RuleHandler {
      @Override
      public Object handle(RuleContext ctx) {
        Expr left = (Expr) ctx.get(0);
        Lexeme sign = (Lexeme) ctx.get(1);
        Expr right = (Expr) ctx.get(2);
        return new BinExpr(left, right, sign.getTerminal(), ctx.getRange(), ctx.getLexemeValues());
      }
    }
  }

  private class SimplePrecedenceGrammarWithDifferentPriorities extends SimplePrecedenceGrammar {
    final Terminal mul = grammar.newTerminal("*");

    final Rule mulRule = grammar.newRule(expr, expr, mul, expr);

    {
      mulRule.setHandler(new BinOpHandler());
    }
  }

  private abstract class Expr {
    private Range<Integer> myRange;
    private List<Object> myLexemesValues;

    protected Expr(Range<Integer> range, List<Object> lexemesValues) {
      myRange = range;
      myLexemesValues = lexemesValues;
    }

    Range<Integer> getRange() {
      return myRange;
    }

    List<Object> getLexemesValues() {
      return myLexemesValues;
    }
  }

  private class IdExpr extends Expr {
    private IdExpr(Range<Integer> range, List<Object> lexemesValues) {
      super(range, lexemesValues);
    }

    @Override
    public String toString() {
      return "id";
    }
  }

  private class BinExpr extends Expr {
    final Expr left;
    final Expr right;
    final Terminal symbol;

    private BinExpr(Expr left, Expr right, Terminal symbol, Range<Integer> range, List<Object> lexemesValues) {
      super(range, lexemesValues);
      this.left = left;
      this.right = right;
      this.symbol = symbol;
    }

    @Override
    public String toString() {
      return "(" + left + " " + symbol + " " + right + ")";
    }
  }
}