package jetbrains.jetpad.hybrid;

import com.google.common.base.Function;
import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.hybrid.parser.Token;

public interface CompletionSpec {
  public CompletionSupplier getTokenCompletion(final Function<Token, Runnable> tokenHandler);
}
