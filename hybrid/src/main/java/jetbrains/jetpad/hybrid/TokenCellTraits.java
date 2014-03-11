/*
 * Copyright 2012-2014 JetBrains s.r.o
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
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.event.Event;
import jetbrains.jetpad.event.Key;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.cell.completion.Completion;
import jetbrains.jetpad.cell.position.Positions;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.cell.trait.CellTraitEventSpec;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.cell.util.Cells;
import jetbrains.jetpad.event.KeyStrokeSpecs;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.cell.*;

import java.util.List;

class TokenCellTraits {
  static class BaseTokenCellTrait extends CellTrait {
    protected HybridSynchronizer<?> hybridSync(Cell cell) {
      HybridSynchronizer<?> sync = cell.get(HybridSynchronizer.HYBRID_SYNCHRONIZER);
      if (sync != null) return sync;
      Cell parent = cell.parent().get();
      if (parent == null) return null;
      return hybridSync(parent);
    }

    protected Cell tokenView(Cell context) {
      Cell parent = context.parent().get();
      if (parent == null) return null;
      if (parent.get(HybridSynchronizer.HYBRID_SYNCHRONIZER) != null) return context;
      return tokenView(parent);
    }

    protected List<Token> tokens(Cell cell) {
      return hybridSync(cell).tokens();
    }

    protected List<Cell> tokenViews(Cell cell) {
      return hybridSync(cell).tokenCells();
    }

    protected TokenOperations<?> tokenOperations(Cell cell) {
      return hybridSync(cell).tokenOperations();
    }

    protected TokenCompletion tokenCompletion(Cell cell) {
      return hybridSync(cell).tokenCompletion();
    }
  }

  static class TokenCellTrait extends BaseTokenCellTrait {
    private boolean myValueToken;

    TokenCellTrait(boolean valueToken) {
      myValueToken = valueToken;
    }

    @Override
    public void onKeyPressed(Cell tokenCell, KeyEvent event) {
      if (hybridSync(tokenCell).hasSelection() &&
          (event.is(Key.BACKSPACE) || event.is(Key.DELETE) || event.is(KeyStrokeSpecs.DELETE_CURRENT))) {
        hybridSync(tokenCell).clearSelection();
        event.consume();
        return;
      }

      super.onKeyPressed(tokenCell, event);
    }

    @Override
    public void onKeyTyped(Cell cell, KeyEvent event) {

      HybridSynchronizer<?> sync = hybridSync(cell);
      if (sync.hasSelection()) {
        CellContainer container = cell.cellContainer().get();
        sync.clearSelection();
        container.keyTyped(new KeyEvent(event.key(), event.keyChar(), event.modifiers()));
        event.consume();
        return;
      }

      super.onKeyTyped(cell, event);
    }

    @Override
    public void onKeyPressedLowPriority(Cell tokenCell, KeyEvent event) {
      TokenOperations<?> tokenOps = tokenOperations(tokenCell);

      if ((event.is(Key.DELETE) || event.is(Key.BACKSPACE)) && (Cells.isEmpty(tokenCell) || myValueToken)) {
        tokenOps.deleteToken(tokenCell, 0).run();
        event.consume();
        return;
      }

      if (event.is(Key.DELETE) && Positions.isLastPosition(tokenCell) && tokenOps.canDelete(tokenCell, 1)) {
        if (tokenOps.canMerge(tokenCell, 1)) {
          tokenOps.mergeTokens(tokenCell, false).run();
        } else {
          tokenOps.deleteToken(tokenCell, 1).run();
        }
        event.consume();
        return;
      }

      if (event.is(Key.BACKSPACE) && Positions.isFirstPosition(tokenCell) && tokenOps.canDelete(tokenCell, -1)) {
        if (tokenOps.canMerge(tokenCell, -1)) {
          tokenOps.mergeTokens(tokenCell, true).run();
        } else {
          tokenOps.deleteToken(tokenCell, -1).run();
        }
        event.consume();
        return;
      }

      if (event.is(KeyStrokeSpecs.DELETE_CURRENT)) {
        tokenOps.deleteToken(tokenCell, 0).run();
        event.consume();
        return;
      }

      super.onKeyPressed(tokenCell, event);
    }

    @Override
    public void onViewTraitEvent(Cell cell, CellTraitEventSpec<?> spec, Event event) {
      if (spec == Cells.BECAME_EMPTY) {
        int index = tokenViews(cell).indexOf(cell);

        Token current = tokens(cell).get(index);
        Token prev = index == 0 ? null : tokens(cell).get(index - 1);
        Token next = index == tokens(cell).size() - 1 ? null : tokens(cell).get(index + 1);

        if (
            (prev != null && (prev.noSpaceToRight() || current.noSpaceToLeft()) ||
            (next != null && (next.noSpaceToLeft()) || current.noSpaceToRight()))) {
          tokenOperations(cell).deleteToken(cell, 0).run();
          event.consume();
          return;
        }
      }

      super.onViewTraitEvent(cell, spec, event);
    }

    @Override
    public Object get(Cell cell, CellTraitPropertySpec<?> spec) {

      if (spec == Completion.COMPLETION && !hybridSync(cell).hasSelection()) return tokenCompletion(cell).tokenCompletion(cell);

      return super.get(cell, spec);
    }
  }

  static class LeftLeafTokenCellTrait extends BaseTokenCellTrait {
    @Override
    public Object get(final Cell cell, CellTraitPropertySpec<?> spec) {
      if (spec == Completion.LEFT_TRANSFORM) return tokenCompletion(cell).sideTransform(tokenView(cell), 0);

      if (spec == TextEditing.EXPAND_LEFT) {
        return new Function<String, Runnable>() {
          @Override
          public Runnable apply(String text) {
            return tokenOperations(cell).expandToError(cell, text, 0);
          }
        };
      }

      return super.get(cell, spec);
    }
  }

  static class RightLeafTokenCellTrait extends BaseTokenCellTrait {
    @Override
    public Object get(final Cell cell, CellTraitPropertySpec<?> spec) {
      if (spec == Completion.RIGHT_TRANSFORM) return tokenCompletion(cell).sideTransform(tokenView(cell), 1);

      if (spec == TextEditing.EXPAND_RIGHT) {
        return new Function<String, Runnable>() {
          @Override
          public Runnable apply(String text) {
            return tokenOperations(cell).expandToError(cell, text, 1);
          }
        };
      }

      return super.get(cell, spec);
    }
  }
}