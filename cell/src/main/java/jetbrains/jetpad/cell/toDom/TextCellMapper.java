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
package jetbrains.jetpad.cell.toDom;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.mapper.MappingContext;
import jetbrains.jetpad.model.event.CompositeRegistration;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.projectional.domUtil.DomTextEditor;

class TextCellMapper extends BaseCellMapper<TextCell> {
  private static final int CARET_BLINK_DELAY = 1000;

  private DomTextEditor myTextEditor;
  private long myLastChangeTime = System.currentTimeMillis();
  private boolean myCaretVisible = true;
  private boolean myContainerFocused;
  private Registration myFocusRegistration;

  TextCellMapper(TextCell source, CellToDomContext ctx) {
    super(source, ctx, DOM.createDiv());
    myContainerFocused = ctx.focused.get();
    myTextEditor = new DomTextEditor(getTarget());
  }

  @Override
  protected boolean isAutoChildManagement() {
    return false;
  }

  @Override
  protected void onAttach(MappingContext ctx) {
    super.onAttach(ctx);
    getTarget().addClassName(CellContainerToDomMapper.CSS.fitContentWidth());
  }

  @Override
  protected void onDetach() {
    super.onDetach();

    if (myFocusRegistration != null) {
      myFocusRegistration.remove();
      myFocusRegistration = null;
    }
  }

  int getCaretOffset(int caret) {
    return (int) myTextEditor.getCaretOffset(caret);
  }

  int getCaretAt(int x) {
    return myTextEditor.getCaretPositionAt(x);
  }

  @Override
  protected boolean isLeaf() {
    return true;
  }

  @Override
  public void refreshProperties() {
    super.refreshProperties();

    Boolean focused = getSource().get(Cell.FOCUSED);
    if (focused) {
      if (myFocusRegistration == null) {
        final Timer timer = new Timer() {
          @Override
          public void run() {
            if (System.currentTimeMillis() - myLastChangeTime < CARET_BLINK_DELAY) return;
            myCaretVisible = !myCaretVisible;
            updateCaretVisibility();
          }
        };
        timer.scheduleRepeating(500);
        myContainerFocused = getContext().focused.get();
        myFocusRegistration = new CompositeRegistration(
          getContext().focused.addHandler(new EventHandler<PropertyChangeEvent<Boolean>>() {
            @Override
            public void onEvent(PropertyChangeEvent<Boolean> event) {
              myContainerFocused = event.getNewValue();
              updateCaretVisibility();
            }
          }),
          getSource().caretVisible().addHandler(new EventHandler<PropertyChangeEvent<Boolean>>() {
            @Override
            public void onEvent(PropertyChangeEvent<Boolean> event) {
              if (event.getNewValue()) {
                myLastChangeTime = System.currentTimeMillis();
                myCaretVisible = true;
              }
            }
          }),
          new Registration() {
            @Override
            protected void doRemove() {
              timer.cancel();
            }
          }
        );
      }
    } else {
      if (myFocusRegistration != null) {
        myFocusRegistration.remove();
        myFocusRegistration = null;
      }
    }

    myLastChangeTime = System.currentTimeMillis();

    myTextEditor.setText(getSource().text().get());
    myTextEditor.setCaretPosition(getSource().caretPosition().get());
    myTextEditor.setCaretVisible(getSource().caretVisible().get() && focused);
    myTextEditor.setTextColor(getSource().textColor().get());
    myTextEditor.setBold(getSource().bold().get());
    myTextEditor.setFontFamily(getSource().fontFamily().get());
    myTextEditor.setFontSize(getSource().fontSize().get());

    myTextEditor.setSelectionVisible(getSource().selectionVisible().get() && focused);
    myTextEditor.setSelectionStart(getSource().selectionStart().get());
  }

  private void updateCaretVisibility() {
    myTextEditor.setCaretVisible(myContainerFocused && myCaretVisible && getSource().caretVisible().get());
  }
}