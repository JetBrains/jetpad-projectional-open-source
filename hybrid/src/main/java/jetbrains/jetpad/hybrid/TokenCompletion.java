package jetbrains.jetpad.hybrid;

import com.google.common.base.Function;
import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.hybrid.parser.Token;

public interface TokenCompletion {
  public CompletionSupplier getTokenCompletion(final Function<Token, Runnable> tokenHandler);
}
