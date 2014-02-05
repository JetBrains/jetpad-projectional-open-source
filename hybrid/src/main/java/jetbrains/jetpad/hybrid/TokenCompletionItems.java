package jetbrains.jetpad.hybrid;

import com.google.common.base.Function;
import jetbrains.jetpad.cell.util.Validators;
import jetbrains.jetpad.completion.BaseCompletionItem;
import jetbrains.jetpad.completion.CompletionItem;
import jetbrains.jetpad.completion.SimpleCompletionItem;
import jetbrains.jetpad.hybrid.parser.BoolValueToken;
import jetbrains.jetpad.hybrid.parser.IdentifierToken;
import jetbrains.jetpad.hybrid.parser.IntValueToken;
import jetbrains.jetpad.hybrid.parser.Token;

import java.util.ArrayList;
import java.util.List;

public class TokenCompletionItems {
  private Function<Token, Runnable> myTokenHandler;

  public TokenCompletionItems(Function<Token, Runnable> tokenHandler) {
    myTokenHandler = tokenHandler;
  }

  public CompletionItem forToken(final Token token) {
    return new SimpleCompletionItem(token.toString()) {
      @Override
      public Runnable complete(String text) {
        return myTokenHandler.apply(token);
      }
    };
  }

  public List<CompletionItem> forTokens(Token ...tokens) {
    List<CompletionItem> result = new ArrayList<CompletionItem>();
    for (Token t : tokens) {
      result.add(forToken(t));
    }
    return result;
  }

  public List<CompletionItem> forBooleans() {
    return forTokens(new BoolValueToken(true), new BoolValueToken(false));
  }

  public CompletionItem forId() {
    return new BaseCompletionItem() {
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
        return myTokenHandler.apply(new IdentifierToken(text));
      }

      @Override
      public boolean isLowPriority() {
        return true;
      }
    };
  }

  public CompletionItem forNumber() {
    return new BaseCompletionItem() {
      @Override
      public String visibleText(String text) {
        return "number";
      }

      @Override
      public boolean isStrictMatchPrefix(String text) {
        if ("".equals(text)) return true;
        return isMatch(text);
      }

      @Override
      public boolean isMatch(String text) {
        return Validators.integer().apply(text);
      }

      @Override
      public Runnable complete(String text) {
        int value;
        if (text == null || text.isEmpty()) {
          value = 0;
        } else {
          value = Integer.parseInt(text);
        }
        return myTokenHandler.apply(new IntValueToken(value));
      }
    };
  }
}
