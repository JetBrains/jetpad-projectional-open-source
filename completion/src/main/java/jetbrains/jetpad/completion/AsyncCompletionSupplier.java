package jetbrains.jetpad.completion;

import jetbrains.jetpad.base.Async;
import jetbrains.jetpad.base.Asyncs;

import java.util.ArrayList;
import java.util.List;

public abstract class AsyncCompletionSupplier {
  public static final AsyncCompletionSupplier EMPTY = new AsyncCompletionSupplier() {
    @Override
    public Async<List<CompletionItem>> get(CompletionParameters cp) {
      return Asyncs.<List<CompletionItem>>constant(new ArrayList<CompletionItem>());
    }

    @Override
    public boolean isEmpty(CompletionParameters cp) {
      return true;
    }
  };

  public abstract Async<List<CompletionItem>> get(CompletionParameters cp);

  public abstract boolean isEmpty(CompletionParameters cp);
}
