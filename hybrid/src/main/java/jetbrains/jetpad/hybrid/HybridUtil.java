package jetbrains.jetpad.hybrid;

import com.google.common.base.Function;
import jetbrains.jetpad.hybrid.parser.Token;

public class HybridUtil {
  public static final Function<Token, Runnable> getAutoInsertHandler(final CompletionContext ctx, final Completer completer, final HybridPositionSpec<?> spec) {
    return new Function<Token, Runnable>() {
      @Override
      public Runnable apply(Token input) {
        if (ctx.getTargetIndex() == ctx.getTokens().size()) {
          PairSpec pairSpec = spec.getPairSpec();
          if (pairSpec.isLeft(input) && pairSpec.getAutoInsert(input) != null) {
            return completer.complete(0, input, pairSpec.getAutoInsert(input));
          } else {
            return completer.complete(input);
          }
        } else {
          return completer.complete(input);
        }
      }
    };
  }
}
