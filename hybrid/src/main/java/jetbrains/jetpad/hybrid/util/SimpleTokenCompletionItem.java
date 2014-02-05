package jetbrains.jetpad.hybrid.util;

import com.google.common.base.Function;
import jetbrains.jetpad.completion.SimpleCompletionItem;
import jetbrains.jetpad.hybrid.parser.Token;

public class SimpleTokenCompletionItem extends SimpleCompletionItem {
  private Token myToken;
  private Function<Token, Runnable> myTokenHandler;

  public SimpleTokenCompletionItem(Token token, Function<Token, Runnable> tokenHandler) {
    super(token.toString());
    myToken = token;
    myTokenHandler = tokenHandler;
  }

  @Override
  public Runnable complete(String text) {
    return myTokenHandler.apply(myToken);
  }
}
