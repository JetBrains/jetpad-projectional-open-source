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
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.message.MessageController;
import jetbrains.jetpad.cell.util.CellState;
import jetbrains.jetpad.cell.util.CellStateHandler;
import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.hybrid.parser.Parser;
import jetbrains.jetpad.hybrid.parser.ParsingContextFactory;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.hybrid.parser.prettyprint.PrettyPrinter;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.model.collections.CollectionListener;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.event.CompositeRegistration;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.Properties;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ReadableProperty;

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
      public CompletionSupplier getTokenCompletion(Function<Token, Runnable> tokenHandler) {
        return spec.getTokenCompletion(tokenHandler);
      }

      @Override
      public CompletionSupplier getAdditionalCompletion(CompletionContext ctx, Completer completer) {
        return spec.getAdditionalCompletion(ctx, completer);
      }

      @Override
      public ParsingContextFactory getParsingContextFactory() {
        return spec.getParsingContextFactory();
      }
    };
  }

  public SimpleHybridSynchronizer(
    Mapper<?, ?> contextMapper,
    HybridProperty<SourceT> source,
    Cell target,
    HybridEditorSpec<SourceT> spec) {
    super(contextMapper, source, target, Properties.constant(spec),
      new TokenListEditor<>(spec, source.getTokens(), false));
  }

  @Deprecated
  public SimpleHybridSynchronizer(
      Mapper<?, ?> contextMapper,
      ObservableList<Token> tokens,
      ReadableProperty<SourceT> source,
      Cell target,
      final SimpleHybridEditorSpec<SourceT> spec) {
    super(contextMapper, source, target, Properties.constant(spec),
      new TokenListEditor<>(toHybridEditorSpec(spec), tokens, false));
  }

  @Override
  protected Registration onAttach(CollectionListener<Token> tokensListener) {
    updateTargetError();
    return new CompositeRegistration(
      myTokenListEditor.tokens.addListener(tokensListener),
      Properties.isNull(getSource()).addHandler(new EventHandler<PropertyChangeEvent<Boolean>>() {
        @Override
        public void onEvent(PropertyChangeEvent<Boolean> event) {
          updateTargetError();
        }
      }));
  }

  @Override
  protected CellStateHandler<Cell, ? extends CellState> getCellStateHandler() {
    return null;
  }

  private void updateTargetError() {
    MessageController.setError(getTarget(), getSource().get() == null ? "parsing error" : null);
  }
}
