package jetbrains.jetpad.hybrid;

import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.hybrid.parser.ParsingContextFactory;
import jetbrains.jetpad.hybrid.parser.prettyprint.PrettyPrinter;

public interface SimpleHybridEditorSpec<SourceT> extends TokenCompletion {
  PrettyPrinter<? super SourceT> getPrettyPrinter();
  PairSpec getPairSpec();

  CompletionSupplier getAdditionalCompletion(CompletionContext ctx, Completer completer);

  ParsingContextFactory getParsingContextFactory();
}
