/*
 * Copyright 2012-2015 JetBrains s.r.o
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

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Timer;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.mappersUtil.BasePopupManager;
import jetbrains.jetpad.cell.mappersUtil.PopupPositionUpdater;
import jetbrains.jetpad.mapper.Mapper;

import java.util.HashMap;
import java.util.Map;

import static jetbrains.jetpad.cell.toDom.CellContainerToDomMapper.CSS;

abstract class DomPopupManager extends BasePopupManager<Element> {
  private static final int POPUPS_REFRESH_MILLIS = 50;
  private CellToDomContext myContext;
  private Map<Element, Registration> myRegistrations = null;

  DomPopupManager(CellToDomContext context) {
    myContext = context;
  }

  @Override
  protected Mapper<? extends Cell, ? extends Element> attachPopup(Cell popup) {
    BaseCellMapper<?> mapper = myContext.apply(popup);
    Element element = mapper.getTarget();
    myContext.rootElement.appendChild(element);
    element.addClassName(CSS.popup());
    element.getStyle().setPosition(Style.Position.ABSOLUTE);
    element.getStyle().setZIndex(100);
    if (myRegistrations == null) {
      myRegistrations = new HashMap<>();
    }
    myRegistrations.put(element, Tooltip.applyDecoration(mapper));
    return mapper;
  }

  @Override
  protected void detachPopup(Mapper<? extends Cell, ? extends Element> popupMapper) {
    popupMapper.getTarget().removeFromParent();
    myRegistrations.remove(popupMapper.getTarget()).remove();
    if (myRegistrations.isEmpty()) {
      myRegistrations = null;
    }
  }

  @Override
  protected Registration setPopupUpdate() {
    final Timer timer = new Timer() {
      @Override
      public void run() {
        updatePopupPositions();
      }
    };
    timer.scheduleRepeating(POPUPS_REFRESH_MILLIS);
    return new Registration() {
      @Override
      protected void doRemove() {
        timer.cancel();
      }
    };
  }

  @Override
  protected PopupPositionUpdater<Element> getPositionUpdater(Mapper<? extends Cell, ? extends Element> popupMapper) {
    return new PopupPositioner(myContext);
  }
}
