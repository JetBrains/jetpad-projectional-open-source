/*
 * Copyright 2012-2015 JetBrains s.r.o
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

import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GrammarTest {
  @Test
  public void trivialNullability() {
    Grammar g = new Grammar();
    g.newRule(g.getStart());

    assertTrue(g.getStart().isNullable());
  }

  @Test
  public void nonNullability() {
    Grammar g = new Grammar();
    Terminal id = g.newTerminal("id");
    g.newRule(g.getStart(), id);

    assertFalse(g.getStart().isNullable());
  }

  @Test
  public void nonNullabilityInCaseOfRecursion() {
    Grammar g = new Grammar();
    g.newRule(g.getStart(), g.getStart());

    assertFalse(g.getStart().isNullable());
  }

  @Test
  public void simpleFirst() {
    Grammar g = new Grammar();
    Terminal id = g.newTerminal("id");
    g.newRule(g.getStart(), id);

    assertEquals(Sets.newHashSet(id), g.getStart().getFirst());
  }

  @Test
  public void firstInCaseOfPartNullability() {
    Grammar g = new Grammar();
    NonTerminal empty = g.newNonTerminal("empty");
    g.newRule(empty);
    Terminal id = g.newTerminal("id");
    g.newRule(g.getStart(), empty, id);

    assertEquals(Sets.newHashSet(id), g.getStart().getFirst());
  }

  @Test
  public void firstInCaseOfRecursion() {
    Grammar g = new Grammar();
    g.newRule(g.getStart(), g.getStart());
    Terminal id = g.newTerminal("id");
    g.newRule(g.getStart(), id);

    assertEquals(Sets.newHashSet(id), g.getStart().getFirst());
  }

  @Test
  public void firstInEmptyGrammar() {
    Grammar g = new Grammar();

    assertEquals(Sets.<Terminal>newHashSet(), g.getStart().getFirst());
  }

  @Test
  public void followOfStartContainsEnd() {
    Grammar g = new Grammar();

    assertEquals(Sets.newHashSet(g.getEnd()), g.getStart().getFollow());
  }

  @Test
  public void simpleFollow() {
    Grammar g = new Grammar();
    NonTerminal expr = g.newNonTerminal("E");
    Terminal id = g.newTerminal("id");
    g.newRule(g.getStart(), expr, id);
    g.newRule(expr);

    assertEquals(Sets.newHashSet(id), expr.getFollow());
  }

  @Test
  public void followTakesIntoAccountNullable() {
    Grammar g = new Grammar();

    Terminal id = g.newTerminal("id");
    Terminal x = g.newTerminal("x");
    NonTerminal a = g.newNonTerminal("A");
    NonTerminal b = g.newNonTerminal("B");

    g.newRule(g.getStart(), a, b, id);

    g.newRule(a, id);
    g.newRule(b);
    g.newRule(b, x);

    assertEquals(Sets.newHashSet(x, id), a.getFollow());
  }

  @Test
  public void nullablilityIsntTakenIntoAccountBug() {
    Grammar g = new Grammar();

    Terminal x = g.newTerminal("x");
    Terminal y = g.newTerminal("y");
    NonTerminal expr = g.newNonTerminal("E");

    NonTerminal exprd = g.newNonTerminal("E'");

    g.newRule(g.getStart(), expr);

    g.newRule(exprd, x);

    g.newRule(expr);
    g.newRule(expr, expr, x, y);
    g.newRule(expr, exprd, y);

    assertEquals(Sets.newHashSet(x, g.getEnd()), expr.getFollow());
  }

  @Test
  public void endToEndTest() {
    Grammar g = new Grammar();
    NonTerminal e = g.newNonTerminal("E");
    NonTerminal ed = g.newNonTerminal("E'");
    NonTerminal t = g.newNonTerminal("T");
    NonTerminal td = g.newNonTerminal("T'");
    NonTerminal f = g.newNonTerminal("F");

    Terminal plus = g.newTerminal("+");
    Terminal star = g.newTerminal("*");
    Terminal lp = g.newTerminal("(");
    Terminal rp = g.newTerminal(")");
    Terminal id = g.newTerminal("id");

    g.newRule(g.getStart(), e);
    g.newRule(e, t, ed);
    g.newRule(ed, plus, t, ed);
    g.newRule(ed);
    g.newRule(t, f, td);
    g.newRule(td, star, f, td);
    g.newRule(td);
    g.newRule(f, lp, e, rp);
    g.newRule(f, id);

    //first
    Set<Terminal> firstETF = Sets.newHashSet(lp, id);
    assertEquals(firstETF, e.getFirst());
    assertEquals(firstETF, t.getFirst());
    assertEquals(firstETF, f.getFirst());
    assertEquals(Sets.newHashSet(plus), ed.getFirst());
    assertEquals(Sets.newHashSet(star), td.getFirst());

    //follow
    Set<Terminal> followEEd = Sets.newHashSet(rp, g.getEnd());
    assertEquals(followEEd, e.getFollow());
    assertEquals(followEEd, ed.getFollow());

    Set<Terminal> followTTd = Sets.newHashSet(plus, rp, g.getEnd());
    assertEquals(followTTd, t.getFollow());
    assertEquals(followTTd, td.getFollow());

    assertEquals(Sets.newHashSet(plus, star, rp, g.getEnd()), f.getFollow());

    //nullable
    assertFalse(e.isNullable());
    assertTrue(ed.isNullable());
    assertFalse(t.isNullable());
    assertTrue(td.isNullable());
    assertFalse(f.isNullable());
  }
}