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
package jetbrains.jetpad.hybrid.testapp.mapper;

import com.google.common.base.Function;
import jetbrains.jetpad.base.Async;
import jetbrains.jetpad.base.Asyncs;
import jetbrains.jetpad.completion.*;
import jetbrains.jetpad.hybrid.*;
import jetbrains.jetpad.hybrid.parser.*;
import jetbrains.jetpad.hybrid.parser.prettyprint.PrettyPrinter;
import jetbrains.jetpad.hybrid.parser.prettyprint.PrettyPrinterContext;
import jetbrains.jetpad.hybrid.testapp.model.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExprHybridEditorSpec implements HybridEditorSpec<Expr> {
  private static final Function<Expr, String> STRING_TEXT_GEN = new Function<Expr, String>() {
    @Nullable
    @Override
    public String apply(@Nullable Expr stringExpr) {
      return stringExpr.toString();
    }
  };

  private static String dequote(String text, String quote) {
    if (text.startsWith(quote)) {
      if (text.length() >= quote.length() * 2 && text.endsWith(quote)) {
        return text.substring(quote.length(), text.length() - quote.length());
      }
      return text.substring(quote.length());
    }
    return text;
  }

  private final Token tokenPlus;
  private final Token tokenMul;

  public ExprHybridEditorSpec() {
    this(Tokens.PLUS, Tokens.MUL);
  }

  public ExprHybridEditorSpec(Token tokenPlus, Token tokenMul) {
    this.tokenPlus = tokenPlus;
    this.tokenMul = tokenMul;
  }

  @Override
  public Parser<Expr> getParser() {
    return new Parser<Expr>() {
      @Override
      public Expr parse(ParsingContext ctx) {
        Expr result = parseExpr(ctx);
        if (ctx.current() != null) return null;
        return result;
      }

      private Expr parseExpr(ParsingContext ctx) {
        return parsePlus(ctx);
      }

      private Expr parsePlus(ParsingContext ctx) {
        Expr result = parseMul(ctx);
        if (result == null) return null;
        while (ctx.current() == tokenPlus) {
          ParsingContext.State startState = ctx.saveState();
          ctx.advance();

          Expr otherFactor = parseMul(ctx);
          if (otherFactor == null) {
            startState.restore();
            return result;
          }

          BinExpr binExpr = new PlusExpr();
          binExpr.left.set(result);
          binExpr.right.set(otherFactor);

          result = binExpr;
        }
        return result;
      }

      private Expr parseMul(ParsingContext ctx) {
        Expr result = parsePostfix(ctx);
        if (result == null) return null;
        while (ctx.current() == tokenMul) {
          ParsingContext.State startState = ctx.saveState();
          ctx.advance();

          Expr otherFactor = parsePostfix(ctx);
          if (otherFactor == null) {
            startState.restore();
            return result;
          }


          BinExpr binExpr = new MulExpr();
          binExpr.left.set(result);
          binExpr.right.set(otherFactor);

          result = binExpr;
        }
        return result;
      }

      private Expr parsePostfix(ParsingContext ctx) {
        Expr result = parsePrimary(ctx);
        if (result == null) return null;
        while (true) {
          if (ctx.current() == Tokens.INCREMENT) {
            PostfixIncrementExpr inc = new PostfixIncrementExpr();
            inc.expr.set(result);
            result = inc;
          } else if (ctx.current() == Tokens.FACTORIAL) {
            FactorialExpr fact = new FactorialExpr();
            fact.expr.set(result);
            result = fact;
          } else {
            break;
          }
          ctx.advance();
        }
        return result;
      }

      private Expr parsePrimary(ParsingContext ctx) {
        ParsingContext.State state = ctx.saveState();
        Token current = ctx.current();

        if (current == Tokens.ID) {
          ctx.advance();
          return new IdExpr();
        }

        if (Tokens.isLp(current)) {
          ctx.advance();

          Expr expr = parseExpr(ctx);
          if (expr == null) {
            state.restore();
            return null;
          }

          current = ctx.current();
          if (current == Tokens.RP) {
            ctx.advance();

            ParenExpr parens = new ParenExpr();
            parens.expr.set(expr);
            return parens;
          }

          state.restore();
          return null;
        }

        if (current instanceof IntValueToken) {
          ctx.advance();
          IntValueToken token = (IntValueToken) current;
          NumberExpr result = new NumberExpr();
          result.value.set(token.getValue());
          return result;
        }

        if (current instanceof IdentifierToken) {
          ctx.advance();
          IdentifierToken token = (IdentifierToken) current;

          if (Tokens.isLp(ctx.current())) {
            ctx.advance();
            if (ctx.current() == Tokens.RP) {
              ctx.advance();
              CallExpr call = new CallExpr();
              call.name.set(token.getName());
              return call;
            } else {
              state.restore();
              return null;
            }
          } else {
            VarExpr result = new VarExpr();
            result.name.set(token.getName());
            return result;
          }
        }

        if (current instanceof ValueToken && ((ValueToken) current).value() instanceof ValueExpr) {
          ctx.advance();
          return (Expr) ((ValueToken) current).value();
        }

        if (current instanceof ValueToken && ((ValueToken) current).value() instanceof PosValueExpr) {
          ctx.advance();
          return (Expr) ((ValueToken) current).value();
        }

        if (current instanceof ValueToken && ((ValueToken) current).value() instanceof ComplexValueExpr) {
          ctx.advance();
          return (Expr) ((ValueToken) current).value();
        }

        if (current instanceof ValueToken && ((ValueToken) current).value() instanceof StringExpr) {
          ctx.advance();
          return (Expr) ((ValueToken) current).value();
        }
        return null;
      }
    };
  }

  @Override
  public PrettyPrinter<Expr> getPrettyPrinter() {
    return new PrettyPrinter<Expr>() {
      @Override
      public void print(final Expr value, final PrettyPrinterContext<Expr> ctx) {
        if (value instanceof BinExpr) {
          BinExpr expr = (BinExpr) value;
          ctx.append(expr.left);
          if (expr instanceof PlusExpr) {
            ctx.append(tokenPlus);
          } else if (expr instanceof MulExpr) {
            ctx.append(tokenMul);
          }
          ctx.append(expr.right);
          return;
        }

        if (value instanceof ParenExpr) {
          ParenExpr paren = (ParenExpr) value;
          ctx.append(Tokens.LP);
          ctx.append(paren.expr);
          ctx.append(Tokens.RP);
          return;
        }

        if (value instanceof PostfixIncrementExpr) {
          PostfixIncrementExpr incr = (PostfixIncrementExpr) value;
          ctx.append(incr.expr);
          ctx.append(Tokens.INCREMENT);
          return;
        }

        if (value instanceof FactorialExpr) {
          FactorialExpr fact = (FactorialExpr) value;
          ctx.append(fact.expr);
          ctx.append(Tokens.FACTORIAL);
          return;
        }

        if (value instanceof CallExpr) {
          CallExpr callExpr = (CallExpr) value;
          ctx.appendId(callExpr.name);
          ctx.append(Tokens.LP_CALL);
          ctx.append(Tokens.RP);
          return;
        }

        if (value instanceof VarExpr) {
          VarExpr varExpr = (VarExpr) value;
          ctx.appendId(varExpr.name);
          return;
        }

        if (value instanceof IdExpr) {
          ctx.append(Tokens.ID);
          return;
        }

        if (value instanceof NumberExpr) {
          NumberExpr num = (NumberExpr) value;
          ctx.appendInt(num.value);
          return;
        }

        if (value instanceof ValueExpr) {
          ctx.append(new ValueToken(value, new ValueExprCloner()));
          return;
        }

        if (value instanceof ComplexValueExpr) {
          ctx.append(new ValueToken(value, new ValueExprCloner()));
          return;
        }

        if (value instanceof StringExpr) {
          ctx.append(new ValueTokenWithTextGen<>(value, new ValueExprCloner(), STRING_TEXT_GEN));
          return;
        }

        throw new IllegalStateException();
      }
    };
  }

  @Override
  public PairSpec getPairSpec() {
    return new ExprHybridPairSpec();
  }

  @Override
  public CompletionSupplier getTokenCompletion(final Function<Token, Runnable> tokenHandler) {
    return new CompletionSupplier() {
      @Override
      public List<CompletionItem> get(CompletionParameters cp) {
        List<CompletionItem> result = new ArrayList<>();
        TokenCompletionItems items = new TokenCompletionItems(tokenHandler);
        result.addAll(items.forTokens(Tokens.ID, Tokens.INCREMENT, Tokens.FACTORIAL, Tokens.PLUS, Tokens.MUL, Tokens.LP, Tokens.RP, Tokens.DOT));
        result.add(items.forNumber());
        result.add(new SimpleCompletionItem("func") {
          @Override
          public Runnable complete(String text) {
            return tokenHandler.apply(new IdentifierToken("func"));
          }
        });
        result.add(new SimpleCompletionItem("value") {
          @Override
          public Runnable complete(String text) {
            return tokenHandler.apply(new ValueToken(new ValueExpr(), new ValueExprCloner()));
          }
        });
        result.add(new SimpleCompletionItem("aaaa") {
          @Override
          public Runnable complete(String text) {
            return tokenHandler.apply(new ValueToken(new ComplexValueExpr(), new ValueExprCloner()));
          }
        });
        result.add(new SimpleCompletionItem("posValue") {
          @Override
          public Runnable complete(String text) {
            return tokenHandler.apply(new ValueToken(new PosValueExpr(), new ValueExprCloner()));
          }
        });
        for (final String quote : new String[] { "\"", "'" }) {
          result.add(new SimpleCompletionItem(quote) {
            @Override
            public Runnable complete(String text) {
              StringExpr stringExpr = new StringExpr(quote);
              stringExpr.body.set(dequote(text, quote));
              return tokenHandler.apply(new ValueTokenWithTextGen<>(stringExpr, new ValueExprCloner(), STRING_TEXT_GEN));
            }
            @Override
            public int getSortPriority() {
              return -1;
            }
          });
          result.add(new ByBoundsCompletionItem(quote, quote) {
            @Override
            public Runnable complete(String text) {
              StringExpr stringExpr = new StringExpr(quote);
              stringExpr.body.set(dequote(text, quote));
              return tokenHandler.apply(new ValueTokenWithTextGen<>(stringExpr, new ValueExprCloner(), STRING_TEXT_GEN));
            }
            @Override
            public int getSortPriority() {
              return -2;
            }
          });
        }

        return result;
      }
    };
  }

  @Override
  public CompletionSupplier getAdditionalCompletion(CompletionContext ctx, final Completer complerer) {
    return new CompletionSupplier() {
      @Override
      public Async<Iterable<CompletionItem>> getAsync(CompletionParameters cp) {
        return Asyncs.<Iterable<CompletionItem>>constant(Arrays.<CompletionItem>asList(
          new SimpleCompletionItem("asyncValue") {
            @Override
            public Runnable complete(String text) {
              return complerer.complete(new ValueToken(new AsyncValueExpr(), new ValueExprCloner()));
            }
          }
        ));
      }
    };
  }

  private static class ValueExprCloner implements ValueToken.ValueCloner<Expr> {
    @Override
    public Expr clone(Expr val) {
      if (val instanceof ValueExpr) {
        ValueExpr result = new ValueExpr();
        result.val.set(((ValueExpr) val).val.get());
        return result;
      }

      if (val instanceof ComplexValueExpr) {
        return new ComplexValueExpr();
      }

      if (val instanceof PosValueExpr) {
        return new PosValueExpr();
      }

      if (val instanceof AsyncValueExpr) {
        return new AsyncValueExpr();
      }

      if (val instanceof StringExpr) {
        return new StringExpr((StringExpr) val);
      }

      throw new IllegalArgumentException(val.toString());
    }
  }
}
