package jetbrains.jetpad.hybrid;

import com.google.common.base.Function;
import jetbrains.jetpad.completion.SimpleCompletionItem;
import jetbrains.jetpad.hybrid.parser.Token;

//todo we have the same code in other HybridPositionSpecs. Nice to make it less duplicated.
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
