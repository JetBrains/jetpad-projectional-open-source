package jetbrains.jetpad.projectional.demo.indentDemo.hybrid;

import com.google.common.base.Function;
import jetbrains.jetpad.completion.CompletionItem;
import jetbrains.jetpad.completion.CompletionParameters;
import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.completion.SimpleCompletionItem;
import jetbrains.jetpad.hybrid.Completer;
import jetbrains.jetpad.hybrid.CompletionContext;
import jetbrains.jetpad.hybrid.HybridPositionSpec;
import jetbrains.jetpad.hybrid.TokenCompletionItems;
import jetbrains.jetpad.hybrid.parser.IdentifierToken;
import jetbrains.jetpad.hybrid.parser.Parser;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.hybrid.parser.ValueToken;
import jetbrains.jetpad.hybrid.parser.prettyprint.PrettyPrinter;
import jetbrains.jetpad.hybrid.parser.prettyprint.PrettyPrinterContext;
import jetbrains.jetpad.projectional.demo.indentDemo.model.*;

import java.util.ArrayList;
import java.util.List;

public class LambdaHybridPositionSpec implements HybridPositionSpec<Expr> {
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

        ctx.append(new ValueToken(value.copy()));
      }
    };

  }

  @Override
  public CompletionSupplier getTokenCompletion(final Function<Token, Runnable> tokenHandler) {
    return new CompletionSupplier() {
      @Override
      public List<CompletionItem> get(CompletionParameters cp) {
        List<CompletionItem> result = new ArrayList<CompletionItem>();
        TokenCompletionItems items = new TokenCompletionItems(tokenHandler);
        result.addAll(items.forTokens(Tokens.WILDCARD, Tokens.LP, Tokens.RP));
        result.add(items.forId());

        result.add(new SimpleCompletionItem("\\") {
          @Override
          public Runnable complete(String text) {
            LambdaExpr lambda = new LambdaExpr();
            lambda.varName.set("x");
            return tokenHandler.apply(new ValueToken(lambda));
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
