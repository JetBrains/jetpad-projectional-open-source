package jetbrains.jetpad.completion;

import jetbrains.jetpad.base.Async;

import java.util.List;

public interface CompletionFragment {
  Async<List<CompletionItem>> get(CompletionParameters params);
}
