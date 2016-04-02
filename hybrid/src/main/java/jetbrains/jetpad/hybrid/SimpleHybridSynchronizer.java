/*
 * Copyright 2012-2016 JetBrains s.r.o
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.jetpad.hybrid;

import com.google.common.base.Function;
import jetbrains.jetpad.base.Disposable;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.util.CellState;
import jetbrains.jetpad.cell.util.CellStateHandler;
import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.hybrid.parser.CommentToken;
import jetbrains.jetpad.hybrid.parser.Parser;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.hybrid.parser.prettyprint.PrettyPrinter;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SimpleHybridSynchronizer<SourceT> extends BaseHybridSynchronizer<SourceT, SimpleHybridEditorSpec<SourceT>> {

  private static <SourceT> HybridEditorSpec<SourceT> toHybridEditorSpec(final SimpleHybridEditorSpec<SourceT> spec) {
    return new HybridEditorSpec<SourceT>() {
      @Override
      public Parser<SourceT> getParser() {
        throw new UnsupportedOperationException("Parser is not available for SimpleHybridSynchronizer");
      }

      @Override
      public PrettyPrinter<? super SourceT> getPrettyPrinter() {
        return spec.getPrettyPrinter();
      }

      @Override
      public PairSpec getPairSpec() {
        return spec.getPairSpec();
      }

      @Override
      public CommentSpec getCommentSpec() {
        return spec.getCommentSpec();
      }

      @Override
      public CompletionSupplier getTokenCompletion(Function<Token, Runnable> tokenHandler) {
        return spec.getTokenCompletion(tokenHandler);
      }

      @Override
      public CompletionSupplier getAdditionalCompletion(CompletionContext ctx, Completer completer) {
        return spec.getAdditionalCompletion(ctx, completer);
      }
    };
  }

  private final Function<Integer, Object> mySourceSupplier;
  private final ReadableProperty<Boolean> myValid;
  private final Registration myDetachRegistration;

  public SimpleHybridSynchronizer(
      Mapper<?, ?> contextMapper,
      HybridProperty<SourceT> source,
      Cell target,
      SimpleHybridEditorSpec<SourceT> spec) {
    this(contextMapper, source, target, spec, null);
  }

  public SimpleHybridSynchronizer(
      Mapper<?, ?> contextMapper,
      HybridProperty<SourceT> source,
      Cell target,
      SimpleHybridEditorSpec<SourceT> spec,
      Function<Integer, Object> sourceSupplier) {

    super(contextMapper, source, target, Properties.constant(spec),
      new TokenListEditor<>(toHybridEditorSpec(spec), source.getTokens(), false));

    mySourceSupplier = sourceSupplier;

    HasOnlyCommentTokens hasOnlyCommentTokens = new HasOnlyCommentTokens(source);
    myDetachRegistration = Registration.from(hasOnlyCommentTokens);
    myValid = Properties.or(Properties.notNull(source), hasOnlyCommentTokens);
  }

  @Override
  protected Registration onAttach(Property<SourceT> syncValue) {
    return PropertyBinding.bindOneWay(getSource(), syncValue);
  }

  @Override
  public List<Cell> getCells(Object source) {
    if (mySourceSupplier == null || source == null) {
      return Collections.emptyList();
    }

    List<Cell> res = new ArrayList<>();
    for (int i = 0; i < tokenCells().size(); i++) {
      if (Objects.equals(mySourceSupplier.apply(i), source)) {
        res.add(tokenCells().get(i));
      }
    }
    return res;
  }

  @Override
  public Object getSource(Cell cell) {
    if (mySourceSupplier == null || cell == null) return null;

    if (cell == getTarget() && tokenCells().isEmpty()) {
      return tokenCells().get(0);
    }

    for (int i = 0; i < tokenCells().size(); i++) {
      Cell c = tokenCells().get(i);
      if (Composites.isDescendant(c, cell)) {
        return mySourceSupplier.apply(i);
      }
    }

    return null;
  }

  @Override
  protected CellStateHandler<Cell, ? extends CellState> getCellStateHandler() {
    return null;
  }

  @Override
  public ReadableProperty<Boolean> valid() {
    return myValid;
  }

  @Override
  public void detach() {
    myDetachRegistration.remove();
    super.detach();
  }

  private static class HasOnlyCommentTokens implements ReadableProperty<Boolean>, Disposable {
    private final HybridProperty<?> mySource;
    private final ValueProperty<Boolean> myValue;
    private final Registration mySourceRegistration;
    private int myNotCommentTokensCount = 0;

    HasOnlyCommentTokens(HybridProperty<?> source) {
      mySource = source;

      for (Token token : source.getTokens()) {
        if (isNotComment(token)) {
          myNotCommentTokensCount++;
        }
      }
      myValue = new ValueProperty<>(myNotCommentTokensCount == 0);

      mySourceRegistration = source.getTokens().addListener(new CollectionAdapter<Token>() {
        @Override
        public void onItemAdded(CollectionItemEvent<? extends Token> event) {
          if (isNotComment(event.getNewItem())) {
            myNotCommentTokensCount++;
          }
          update();
        }

        @Override
        public void onItemSet(CollectionItemEvent<? extends Token> event) {
          if (isNotComment(event.getOldItem())) {
            myNotCommentTokensCount--;
          }
          if (isNotComment(event.getNewItem())) {
            myNotCommentTokensCount++;
          }
          update();
        }

        @Override
        public void onItemRemoved(CollectionItemEvent<? extends Token> event) {
          if (isNotComment(event.getOldItem())) {
            myNotCommentTokensCount--;
          }
          update();
        }
      });
    }

    private boolean isNotComment(Token token) {
      return !(token instanceof CommentToken);
    }

    private void update() {
      myValue.set(myNotCommentTokensCount == 0);
    }

    @Override
    public String getPropExpr() {
      return "HasOnlyCommentTokens(" + mySource + ") = " + myValue.get();
    }

    @Override
    public Boolean get() {
      return myValue.get();
    }

    @Override
    public Registration addHandler(EventHandler<? super PropertyChangeEvent<Boolean>> handler) {
      return myValue.addHandler(handler);
    }

    @Override
    public void dispose() {
      mySourceRegistration.remove();
    }
  }
}
