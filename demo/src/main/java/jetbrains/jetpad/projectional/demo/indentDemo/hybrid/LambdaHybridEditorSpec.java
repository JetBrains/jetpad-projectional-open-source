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
package jetbrains.jetpad.projectional.demo.indentDemo.hybrid;

import com.google.common.base.Function;
import jetbrains.jetpad.completion.CompletionItem;
import jetbrains.jetpad.completion.CompletionParameters;
import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.completion.SimpleCompletionItem;
import jetbrains.jetpad.hybrid.*;
import jetbrains.jetpad.hybrid.parser.IdentifierToken;
import jetbrains.jetpad.hybrid.parser.Parser;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.hybrid.parser.ValueToken;
import jetbrains.jetpad.hybrid.parser.prettyprint.PrettyPrinter;
import jetbrains.jetpad.hybrid.parser.prettyprint.PrettyPrinterContext;
import jetbrains.jetpad.projectional.demo.indentDemo.model.*;

import java.util.ArrayList;
import java.util.List;

public class LambdaHybridEditorSpec extends BaseHybridEditorSpec<Expr> {
  @Override
  public Parser<Expr> getParser() {
    return LambdaParser.EXPR;
  }

  @Override
  public PrettyPrinter<? super Expr> getPrettyPrinter() {
    return new PrettyPrinter<Expr>() {
      @Override
      public void print(Expr value, PrettyPrinterContext<Expr> ctx) {
        if (value instanceof WildCardExpr) {
          ctx.append(Tokens.WILDCARD);
          return;
        }

        if (value instanceof VarExpr) {
          VarExpr varExpr = (VarExpr) value;
          String name = varExpr.name.get();
          ctx.append(new IdentifierToken(name == null ? "" : name));
          return;
        }

        if (value instanceof ParensExpr) {
          ParensExpr parens = (ParensExpr) value;
          ctx.append(Tokens.LP);
          ctx.append(parens.expr);
          ctx.append(Tokens.RP);
          return;
        }

        if (value instanceof AppExpr) {
          AppExpr app = (AppExpr) value;
          ctx.append(app.fun);
          ctx.append(app.arg);
          return;
        }

        ctx.append(new ValueToken(value, new ExprCloner()));
      }
    };
  }

  @Override
  public PairSpec getPairSpec() {
    return PairSpec.EMPTY;
  }

  @Override
  public CompletionSupplier getTokenCompletion(CompletionContext completionContext, Completer completer, final Function<Token, Runnable> tokenHandler) {
    return new CompletionSupplier() {
      @Override
      public List<CompletionItem> get(CompletionParameters cp) {
        List<CompletionItem> result = new ArrayList<>();
        TokenCompletionItems items = new TokenCompletionItems(tokenHandler);
        result.addAll(items.forTokens(Tokens.WILDCARD, Tokens.LP, Tokens.RP));
        result.add(items.forId());

        result.add(new SimpleCompletionItem("\\") {
          @Override
          public Runnable complete(String text) {
            LambdaExpr lambda = new LambdaExpr();
            lambda.varName.set("x");
            return tokenHandler.apply(new ValueToken(lambda, new ExprCloner()));
          }
        });
        return result;
      }
    };
  }

  @Override
  public CompletionSupplier getAdditionalCompletion(CompletionContext ctx, Completer completer) {
    return CompletionSupplier.EMPTY;
  }

}