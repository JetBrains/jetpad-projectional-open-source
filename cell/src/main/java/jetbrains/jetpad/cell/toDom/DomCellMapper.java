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
package jetbrains.jetpad.cell.toDom;

import com.google.gwt.dom.client.Node;
import com.google.gwt.query.client.Function;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.dom.DomCell;
import jetbrains.jetpad.cell.event.FocusEvent;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.WritableProperty;

import static com.google.gwt.query.client.GQuery.$;

class DomCellMapper extends BaseCellMapper<DomCell> {
  DomCellMapper(DomCell source, CellToDomContext ctx) {
    super(source, ctx, DOM.createDiv());

    getTarget().addClassName(CellContainerToDomMapper.CSS.domCell());
  }

  @Override
  protected boolean isAutoChildManagement() {
    return false;
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forPropsOneWay(getSource().node, new WritableProperty<Node>() {
      Node currentValue;

      @Override
      public void set(Node value) {
        if (currentValue != null) {
          $(currentValue).unbind(Event.ONFOCUS | Event.ONBLUR);
        }

        getTarget().removeAllChildren();
        if (value != null) {
          getTarget().appendChild(value);
          $(value).focus(new Function() {
            @Override
            public boolean f(Event e) {
              getContext().focused.set(true);
              return false;
            }
          });
          $(value).blur(new Function() {
            @Override
            public boolean f(Event e) {
              getContext().focused.set(false);
              return false;
            }
          });
        }

        currentValue = value;
      }
    }));

    conf.add(Synchronizers.forRegistration(getSource().addTrait(new CellTrait() {
      @Override
      public void onFocusGained(Cell cell, FocusEvent event) {
        if (getSource().node.get() != null) {
          $(getSource().node.get()).focus();
        }
        super.onFocusGained(cell, event);
      }

      @Override
      public void onFocusLost(Cell cell, FocusEvent event) {
        $(getContext().focusElement).focus();
        super.onFocusLost(cell, event);
      }
    })));
  }
}