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

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.message.MessageController;
import jetbrains.jetpad.cell.util.CellState;
import jetbrains.jetpad.cell.util.CellStateDifference;
import jetbrains.jetpad.cell.util.CellStateHandler;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.model.event.CompositeRegistration;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.*;
import jetbrains.jetpad.projectional.cell.mapping.ToCellMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class HybridSynchronizer<SourceT> extends BaseHybridSynchronizer<SourceT, HybridEditorSpec<SourceT>> implements ToCellMapping {
  private Property<SourceT> myWritableSource;

  public HybridSynchronizer(Mapper<?, ?> contextMapper, Property<SourceT> source, Cell target,
                            HybridEditorSpec<SourceT> spec) {
    this(contextMapper, source, target, Properties.constant(spec));
  }

  public HybridSynchronizer(Mapper<?, ?> contextMapper, Property<SourceT> source, Cell target,
                            ReadableProperty<HybridEditorSpec<SourceT>> spec) {
    super(contextMapper, source, target, spec, new TokenListEditor<>(spec, new ObservableArrayList<Token>(), true));
    myWritableSource = source;
  }

  @Override
  protected Registration onAttach(Property<SourceT> syncValue) {
    updateTargetError();
    return new CompositeRegistration(
      PropertyBinding.bindTwoWay(myWritableSource, syncValue),
      valid().addHandler(new EventHandler<PropertyChangeEvent<Boolean>>() {
        @Override
        public void onEvent(PropertyChangeEvent<Boolean> event) {
          updateTargetError();
        }
      }));
  }

  @Override
  public List<Cell> getCells(Object source) {
    if (source == getSource().get()) {
      return Collections.singletonList(getTarget());
    }
    List<Object> tokenObjects = tokenListEditor().getObjects();
    return selectCells(source, tokenObjects);
  }

  private List<Cell> selectCells(Object source, List<Object> tokenObjects) {
    List<Cell> result = null;
    for (int i = 0; i < tokenObjects.size(); i++) {
      Object o = tokenObjects.get(i);
      if (o == source) {
        if (result == null) {
          result = new ArrayList<>(1);
        }
        result.add(getTargetList().get(i));
      }
    }
    return result == null ? Collections.<Cell>emptyList() : result;
  }

  @Override
  public Object getSource(Cell cell) {
    if (cell == getTarget()) {
      return getSource().get();
    }
    int index = 0;
    for (; index < getTargetList().size(); index++) {
      if (Composites.isDescendant(getTargetList().get(index), cell)) {
        List<Object> objects = tokenListEditor().getObjects();
        return objects.isEmpty() ? null : objects.get(index);
      }
    }
    return null;
  }

  @Override
  protected CellStateHandler<Cell, HybridCellState> getCellStateHandler() {
    return new CellStateHandler<Cell, HybridCellState>() {
      @Override
      public boolean synced(Cell cell) {
        return valid().get();
      }

      @Override
      public HybridCellState saveState(Cell cell) {
        if (valid().get()) {
          return new HybridCellState(null);
        }

        List<Token> result = new ArrayList<>();
        for (Token t : tokens()) {
          result.add(t.copy());
        }
        return new HybridCellState(result);
      }

      @Override
      public void restoreState(Cell cell, HybridCellState state) {
        tokenListEditor().restoreState(state.tokens);
      }
    };
  }

  @Override
  public ReadableProperty<Boolean> valid() {
    return tokenListEditor().valid;
  }

  private void updateTargetError() {
    MessageController.setError(getTarget(), valid().get() ? null : "parsing error");
  }

  private static class HybridCellState implements CellState {
    private final List<Token> tokens;

    HybridCellState(List<Token> tokens) {
      this.tokens = tokens;
    }

    @Override
    public CellStateDifference getDifference(CellState state) {
      if (!(state instanceof HybridCellState)) {
        return tokens == null ? CellStateDifference.NAVIGATION : CellStateDifference.EDIT;
      }
      if (!Objects.equals(tokens, ((HybridCellState)state).tokens)) {
        return CellStateDifference.EDIT;
      }
      return CellStateDifference.EQUAL;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      HybridCellState that = (HybridCellState) o;

      return !(tokens != null ? !tokens.equals(that.tokens) : that.tokens != null);
    }

    @Override
    public int hashCode() {
      return tokens != null ? tokens.hashCode() : 0;
    }
  }
}
