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
package jetbrains.jetpad.projectional.selection;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.cell.TraitPropagator;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;

public class SelectionController {
  public static final CellTraitPropertySpec<SelectionController> PROPERTY = new CellTraitPropertySpec<>("SelectionController");
  private static final CellPropertySpec<Boolean> HAS_SELECTION_CONTROLLER =
    new CellPropertySpec<>("hasSelectionController", false);

  public static Registration install(CellContainer container) {
    SelectionController sm = new SelectionController();
    return TraitPropagator.install(container, sm.trait(), HAS_SELECTION_CONTROLLER);
  }

  private SelectionId myLegacySelectionId = null;

  private Listeners<SelectionListener> myListeners = new Listeners<>();

  public Registration addListener(SelectionListener l) {
    return myListeners.add(l);
  }

  public void updateLegacySelection(final Cell start, final Cell end) {
    if (myLegacySelectionId == null) {
      myLegacySelectionId = new SelectionId();
      myListeners.fire(new ListenerCaller<SelectionListener>() {
        @Override
        public void call(SelectionListener l) {
          l.onSelectionOpened(myLegacySelectionId, new SimpleSelection(start, null, end, null));
        }
      });
    } else {
      myListeners.fire(new ListenerCaller<SelectionListener>() {
        @Override
        public void call(SelectionListener l) {
          l.onSelectionChanged(myLegacySelectionId, new SimpleSelection(start, null, end, null));
        }
      });
    }
  }

  public void closeLegacySelection() {
    myListeners.fire(new ListenerCaller<SelectionListener>() {
      @Override
      public void call(SelectionListener l) {
        l.onSelectionClosed(myLegacySelectionId);
      }
    });
    myLegacySelectionId = null;
  }

  private CellTrait trait(){
    return new CellTrait() {
      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == PROPERTY) {
          return SelectionController.this;
        } else {
          return super.get(cell, spec);
        }
      }

      @Override
      public void onAdd(Cell cell) {
        SelectionSupport<?> selectionSupport = cell.get(SelectionSupport.SELECTION_SUPPORT);
        if (selectionSupport != null) {
          selectionSupport.setSelectionController(SelectionController.this);
        }
      }
    };
  }
}
