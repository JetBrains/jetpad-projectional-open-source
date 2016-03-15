package jetbrains.jetpad.hybrid.testapp.model;

import jetbrains.jetpad.hybrid.HybridProperty;
import jetbrains.jetpad.hybrid.parser.SimpleParsingContextFactory;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.hybrid.testapp.mapper.ExprHybridEditorSpec;
import jetbrains.jetpad.hybrid.util.SimpleHybridProperty;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;

public class SimpleExprContainer extends ExprNode {
  public final HybridProperty<Expr> expr = new SimpleHybridProperty<>(
    new ExprHybridEditorSpec().getParser(), new ObservableArrayList<Token>(), new SimpleParsingContextFactory());
}
