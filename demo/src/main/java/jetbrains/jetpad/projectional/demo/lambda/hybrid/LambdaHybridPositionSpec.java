package jetbrains.jetpad.projectional.demo.lambda.hybrid;

import com.google.common.base.Function;
import jetbrains.jetpad.cell.util.Validators;
import jetbrains.jetpad.completion.*;
import jetbrains.jetpad.hybrid.Completer;
import jetbrains.jetpad.hybrid.CompletionContext;
import jetbrains.jetpad.hybrid.HybridPositionSpec;
import jetbrains.jetpad.hybrid.SimpleTokenCompletionItem;
import jetbrains.jetpad.hybrid.parser.IdentifierToken;
import jetbrains.jetpad.hybrid.parser.Parser;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.hybrid.parser.ValueToken;
import jetbrains.jetpad.hybrid.parser.prettyprint.PrettyPrinter;
import jetbrains.jetpad.hybrid.parser.prettyprint.PrettyPrinterContext;
import jetbrains.jetpad.projectional.demo.lambda.model.*;

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
        result.add(new SimpleTokenCompletionItem(Tokens.WILDCARD, tokenHandler));
        result.add(new SimpleTokenCompletionItem(Tokens.LP, tokenHandler));
        result.add(new SimpleTokenCompletionItem(Tokens.RP, tokenHandler));
        result.add(new BaseCompletionItem() {
          @Override
          public String visibleText(String text) {
            return "id";
          }

          @Override
          public boolean isStrictMatchPrefix(String text) {
            if ("".equals(text)) return true;
            return isMatch(text);
          }

          @Override
          public boolean isMatch(String text) {
            return Validators.identifier().apply(text);
          }

          @Override
          public Runnable complete(String text) {
            return tokenHandler.apply(new IdentifierToken(text));
          }

          @Override
          public boolean isLowPriority() {
            return true;
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
