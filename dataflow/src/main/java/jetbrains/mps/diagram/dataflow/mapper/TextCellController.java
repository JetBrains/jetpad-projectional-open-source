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
package jetbrains.mps.diagram.dataflow.mapper;

import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.Registration;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.cell.view.CellView;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.jetpad.projectional.view.ViewEventHandler;
import jetbrains.jetpad.projectional.view.ViewEvents;
import jetbrains.jetpad.projectional.view.ViewTraitBuilder;

class TextCellController {
  private static final long MAX_CLICK_TIME = 500;

  private Registration myEditingRegistration;
  private long myPressedTime = -1;

  private TextCell myTextCell = new TextCell();

  private Property<Boolean> myEditing = new ValueProperty<Boolean>(false);

  Registration install(CellView contextView) {
    contextView.cell.set(myTextCell);

    final Registration mouseReg = contextView.addTrait(new ViewTraitBuilder().on(ViewEvents.MOUSE_PRESSED, new ViewEventHandler<MouseEvent>() {
      @Override
      public void handle(View view, MouseEvent e) {
        long time = System.currentTimeMillis();
        if (time - myPressedTime < MAX_CLICK_TIME) {
          addEditing();
        }
        myPressedTime = time;
      }
    }).build());
    final Registration focusReg = contextView.focused().addHandler(new EventHandler<PropertyChangeEvent<Boolean>>() {
      @Override
      public void onEvent(PropertyChangeEvent<Boolean> event) {
        if (!event.getNewValue()) {
          removeEditing();
        }
        myTextCell.caretVisible().set(false);
        myTextCell.container().focusedCell.set(null);
      }
    });

    return new Registration() {
      @Override
      public void remove() {
        removeEditing();
        mouseReg.remove();
        focusReg.remove();
      }
    };
  }

  Property<Boolean> editing() {
    return myEditing;
  }

  private void addEditing() {
    if (myEditingRegistration != null) return;
    myEditingRegistration = myTextCell.addTrait(TextEditing.textEditing());
    myEditing.set(true);
  }

  private void removeEditing() {
    if (myEditingRegistration != null) {
      myEditingRegistration.remove();
    }
    myEditingRegistration = null;
    myEditing.set(false);
  }

  TextCell getTextCell() {
    return myTextCell;
  }
}