package jetbrains.jetpad.hybrid.util;

import com.google.common.base.Function;
import jetbrains.jetpad.cell.util.Validators;
import jetbrains.jetpad.completion.BaseCompletionItem;
import jetbrains.jetpad.hybrid.parser.IdentifierToken;
import jetbrains.jetpad.hybrid.parser.Token;

public class IdentifierTokenCompletionItem extends BaseCompletionItem {
  private Function<Token, Runnable> myTokenHandler;

  public IdentifierTokenCompletionItem(Function<Token, Runnable> tokenHandler) {
    myTokenHandler = tokenHandler;
  }

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
}
