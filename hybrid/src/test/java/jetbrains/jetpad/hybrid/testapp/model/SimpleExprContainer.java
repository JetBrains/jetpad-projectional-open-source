package jetbrains.jetpad.hybrid.testapp.model;

import jetbrains.jetpad.hybrid.HybridProperty;
import jetbrains.jetpad.hybrid.ParsingHybridProperty;
import jetbrains.jetpad.hybrid.parser.CommentParsingContextFactory;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.hybrid.testapp.mapper.ExprHybridEditorSpec;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;

public class SimpleExprContainer extends ExprNode {
  private final ExprHybridEditorSpec editorSpec = new ExprHybridEditorSpec();

  public final HybridProperty<Expr> expr = new ParsingHybridProperty<>(
    editorSpec.getParser(), editorSpec.getPrettyPrinter(),
    new ObservableArrayList<Token>(), new CommentParsingContextFactory());
}
