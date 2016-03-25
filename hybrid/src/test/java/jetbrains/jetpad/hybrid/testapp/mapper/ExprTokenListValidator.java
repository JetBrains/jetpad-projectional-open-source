package jetbrains.jetpad.hybrid.testapp.mapper;

import jetbrains.jetpad.hybrid.TokenListValidator;
import jetbrains.jetpad.hybrid.testapp.model.CommentToken;
import jetbrains.jetpad.hybrid.parser.Token;

import java.util.List;

final class ExprTokenListValidator implements TokenListValidator {

  private static String getText(List<Token> tokenList) {
    StringBuilder builder = new StringBuilder();
    for (Token token : tokenList) {
      builder.append(token.text());
    }
    return builder.toString();
  }

  ExprTokenListValidator() {
  }

  @Override
  public void validate(List<Token> tokenList) {
    int size = tokenList.size();
    for (int i = 0; i < size; i++) {
      Token token = tokenList.get(i);
      if (token instanceof CommentToken && i < size - 1) {
        List<Token> subList = tokenList.subList(i, size);
        String tokenListText = getText(subList);
        Token terminatorToken = new CommentToken(tokenListText);
        subList.clear();
        tokenList.add(terminatorToken);
        break;
      }
    }
  }

}
