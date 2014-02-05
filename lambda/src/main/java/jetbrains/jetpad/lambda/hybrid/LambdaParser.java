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

import com.google.common.base.Predicate;
import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.grammar.*;
import jetbrains.jetpad.grammar.lr.Lexeme;
import jetbrains.jetpad.hybrid.parser.IdentifierToken;
import jetbrains.jetpad.hybrid.parser.Parser;
import jetbrains.jetpad.hybrid.parser.ValueToken;
import jetbrains.jetpad.hybrid.parser.simple.SimpleParserSpecification;
import jetbrains.jetpad.lambda.model.*;

class LambdaParser {
  static final Parser<Expr> EXPR;

  static {
    SimpleParserSpecification<Expr> spec = new SimpleParserSpecification<Expr>();
    spec.changeGrammar(new Handler<SimpleParserSpecification.SimpleGrammarContext>() {
      @Override
      public void handle(SimpleParserSpecification.SimpleGrammarContext ctx) {
        Grammar g = ctx.grammar();
        NonTerminal expr = ctx.expr();
        NonTerminal pe = g.newNonTerminal("pe");
        NonTerminal app = g.newNonTerminal("app");

        Terminal wildcard = ctx.terminal(Tokens.WILDCARD);
        Terminal lp = ctx.terminal(Tokens.LP);
        Terminal rp = ctx.terminal(Tokens.RP);
        Terminal id = ctx.id();
        Terminal valExpr = ctx.value("valExpr", new Predicate<Object>() {
          @Override
          public boolean apply(Object o) {
            return o instanceof Expr;
          }
        });

        g.newRule(pe, wildcard).setHandler(new RuleHandler() {
          @Override
          public Object handle(RuleContext ctx) {
            return new WildCardExpr();
          }
        });
        g.newRule(pe, id).setHandler(new RuleHandler() {
          @Override
          public Object handle(RuleContext ctx) {
            VarExpr result = new VarExpr();
            Lexeme lexeme = (Lexeme) ctx.get(0);
            IdentifierToken idToken = (IdentifierToken) lexeme.getValue();
            result.name.set(idToken.text());
            return result;
          }
        });
        g.newRule(pe, lp, expr, rp).setHandler(new RuleHandler() {
          @Override
          public Object handle(RuleContext ctx) {
            Expr expr = (Expr) ctx.get(1);
            ParensExpr result = new ParensExpr();
            result.expr.set(expr);
            return result;
          }
        });
        g.newRule(pe, valExpr).setHandler(new RuleHandler() {
          @Override
          public Object handle(RuleContext ctx) {
            Lexeme lexeme = (Lexeme) ctx.get(0);
            return ((Expr) ((ValueToken) lexeme.getValue()).value()).copy();
          }
        });

        g.newRule(app, pe).setHandler(new RuleHandler() {
          @Override
          public Object handle(RuleContext ctx) {
            return (Expr) ctx.get(0);
          }
        });

        g.newRule(app, app, pe).setHandler(new RuleHandler() {
          @Override
          public Object handle(RuleContext ctx) {
            Expr fun = (Expr) ctx.get(0);
            Expr arg = (Expr) ctx.get(1);

            AppExpr result = new AppExpr();
            result.fun.set(fun);
            result.arg.set(arg);
            return result;
          }
        });

        g.newRule(expr, app).setHandler(new RuleHandler() {
          @Override
          public Object handle(RuleContext ctx) {
            return (Expr) ctx.get(0);
          }
        });
      }
    });

    EXPR = spec.buildParser();
  }
}