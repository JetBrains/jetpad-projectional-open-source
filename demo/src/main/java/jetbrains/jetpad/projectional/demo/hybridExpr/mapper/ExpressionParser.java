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
package jetbrains.jetpad.projectional.demo.hybridExpr.mapper;

import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.grammar.*;
import jetbrains.jetpad.grammar.parser.Lexeme;
import jetbrains.jetpad.projectional.demo.hybridExpr.model.*;
import jetbrains.jetpad.hybrid.parser.IdentifierToken;
import jetbrains.jetpad.hybrid.parser.IntValueToken;
import jetbrains.jetpad.hybrid.parser.Parser;
import jetbrains.jetpad.hybrid.parser.simple.BinaryExpressionFactory;
import jetbrains.jetpad.hybrid.parser.simple.SimpleParserSpecification;
import jetbrains.jetpad.hybrid.parser.simple.UnaryExpressionFactory;

import java.util.List;

import static jetbrains.jetpad.grammar.GrammarSugar.oneOf;
import static jetbrains.jetpad.grammar.GrammarSugar.separated;

class ExpressionParser {
  static final Parser<Expression> PARSER;

  static {
    SimpleParserSpecification<Expression> spec = new SimpleParserSpecification<Expression>();

    spec.addBinaryOperator(Tokens.PLUS, new BinExprFactory() {
      @Override
      protected BinaryExpression createExpression() {
        return new PlusExpression();
      }
    }, 0, true);
    spec.addBinaryOperator(Tokens.MINUS, new BinExprFactory() {
      @Override
      protected BinaryExpression createExpression() {
        return new MinusExpression();
      }
    }, 0, true);
    spec.addBinaryOperator(Tokens.MUL, new BinExprFactory() {
      @Override
      protected BinaryExpression createExpression() {
        return new MulExpression();
      }
    }, 1, true);
    spec.addBinaryOperator(Tokens.DIV, new BinExprFactory() {
      @Override
      protected BinaryExpression createExpression() {
        return new DivExpression();
      }
    }, 1, true);

    UnaryExpressionFactory<Expression> prefixIncrFactory = new UnaryExprFactory() {
      @Override
      protected UnaryExpression createExpression() {
        return new PrefixIncrementExpression();
      }
    };
    spec.addPrefix(Tokens.INCREMENT, prefixIncrFactory, 100);
    spec.addPrefix(Tokens.INCREMENT_LEFT, prefixIncrFactory, 100);
    spec.addPrefix(Tokens.INCREMENT_RIGHT, prefixIncrFactory, 100);

    UnaryExpressionFactory<Expression> prefixDecrFactory = new UnaryExprFactory() {
      @Override
      protected UnaryExpression createExpression() {
        return new PrefixDecrementExpression();
      }
    };
    spec.addPrefix(Tokens.DECREMENT, prefixDecrFactory, 100);
    spec.addPrefix(Tokens.DECREMENT_LEFT, prefixDecrFactory, 100);
    spec.addPrefix(Tokens.DECREMENT_RIGHT, prefixDecrFactory, 100);

    UnaryExpressionFactory<Expression> postfixIncFactory = new UnaryExprFactory() {
      @Override
      protected UnaryExpression createExpression() {
        return new PostifxIncrementExpression();
      }
    };
    spec.addSuffix(Tokens.INCREMENT, postfixIncFactory, 101);
    spec.addSuffix(Tokens.INCREMENT_LEFT, postfixIncFactory, 101);
    spec.addSuffix(Tokens.INCREMENT_RIGHT, postfixIncFactory, 101);

    UnaryExpressionFactory<Expression> postfixDecrFactory = new UnaryExprFactory() {
      @Override
      protected UnaryExpression createExpression() {
        return new PostfixDecrementExpression();
      }
    };
    spec.addSuffix(Tokens.DECREMENT, postfixDecrFactory, 101);
    spec.addSuffix(Tokens.DECREMENT_LEFT, postfixDecrFactory, 101);
    spec.addSuffix(Tokens.DECREMENT_RIGHT, postfixDecrFactory, 101);

    spec.changeGrammar(new Handler<SimpleParserSpecification.SimpleGrammarContext>() {
      @Override
      public void handle(SimpleParserSpecification.SimpleGrammarContext ctx) {
        Grammar g = ctx.grammar();
        NonTerminal expr = ctx.expr();
        Symbol lp = oneOf(ctx.terminal(Tokens.LEFT_PAREN), ctx.terminal(Tokens.LEFT_PARENT_METHOD_CALL));
        Symbol rp = ctx.terminal(Tokens.RIGHT_PAREN);

        g.newRule(expr, ctx.terminal(Tokens.TRUE)).setHandler(new RuleHandler() {
          @Override
          public Object handle(RuleContext ctx) {
            BoolExpression boolExpr = new BoolExpression();
            boolExpr.value.set(true);
            return boolExpr;
          }
        });
        g.newRule(expr, ctx.terminal(Tokens.FALSE)).setHandler(new RuleHandler() {
          @Override
          public Object handle(RuleContext ctx) {
            BoolExpression boolExpr = new BoolExpression();
            boolExpr.value.set(false);
            return boolExpr;
          }
        });
        g.newRule(expr, ctx.number()).setHandler(new RuleHandler() {
          @Override
          public Object handle(RuleContext ctx) {
            Lexeme lexeme = (Lexeme) ctx.get(0);
            IntValueToken intValueToken = (IntValueToken) lexeme.getValue();

            NumberExpression expr = new NumberExpression();
            expr.value.set(intValueToken.getValue());
            return expr;
          }
        });
        g.newRule(expr, ctx.id()).setHandler(new RuleHandler() {
          @Override
          public Object handle(RuleContext ctx) {
            Lexeme lexeme = (Lexeme) ctx.get(0);
            IdentifierToken idToken = (IdentifierToken) lexeme.getValue();

            VarExpression varExpr = new VarExpression();
            varExpr.name.set(idToken.text());
            return varExpr;
          }
        });

        g.newRule(expr, lp, expr, rp).setHandler(new RuleHandler() {
          @Override
          public Object handle(RuleContext ctx) {
            Expression expr = (Expression) ctx.get(1);

            ParensExpression parens = new ParensExpression();
            parens.expression.set(expr);
            return parens;
          }
        });

        NonTerminal operation = g.newNonTerminal("O");

        g.newRule(expr, expr, ctx.terminal(Tokens.DOT), operation).setHandler(new RuleHandler() {
          @Override
          public Object handle(RuleContext ctx) {
            DotExpression result = new DotExpression();
            result.operand.set((Expression) ctx.get(0));
            result.operation.set((Operation) ctx.get(2));
            return result;
          }
        }).setPriority(1000);

        g.newRule(operation, ctx.id()).setHandler(new RuleHandler() {
          @Override
          public Object handle(RuleContext ctx) {
            FieldReferenceOperation op = new FieldReferenceOperation();
            Lexeme lex = (Lexeme) ctx.get(0);
            IdentifierToken id = (IdentifierToken) lex.getValue();
            op.fieldName.set(id.text());
            return op;
          }
        });

        g.newRule(operation, ctx.id(), lp, separated(expr, ctx.terminal(Tokens.COMMA)), rp).setHandler(new RuleHandler() {
          @Override
          public Object handle(RuleContext ctx) {
            Lexeme lex = (Lexeme) ctx.get(0);
            List<Expression> args = (List<Expression>) ctx.get(2);
            MethodCallOperation call = new MethodCallOperation();
            call.arguments.addAll(args);
            IdentifierToken id = (IdentifierToken) lex.getValue();
            call.methodName.set(id.text());
            return call;
          }
        });
      }
    });

    PARSER = spec.buildParser();
  }

  private static abstract class BinExprFactory implements BinaryExpressionFactory<Expression> {
    protected abstract BinaryExpression createExpression();

    @Override
    public Expression create(ParserParameters params, Expression left, Expression right) {
      BinaryExpression result = createExpression();
      result.left.set(left);
      result.right.set(right);
      return result;
    }
  }

  private static abstract class UnaryExprFactory implements UnaryExpressionFactory<Expression> {
    protected abstract UnaryExpression createExpression();

    @Override
    public Expression create(ParserParameters params, Expression expr) {
      UnaryExpression result = createExpression();
      result.expression.set(expr);
      return result;
    }
  }
}