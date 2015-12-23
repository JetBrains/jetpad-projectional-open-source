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
package jetbrains.jetpad.cell.mappersUtil;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.EditingTestCase;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class BasePopupManagerTest extends EditingTestCase {
  private TextCell cell;
  private TestPopupManager manager;

  @Before
  public void init() {
    cell = new TextCell();
    myCellContainer.root.children().add(cell);
    manager = new TestPopupManager();
  }

  @Test
  public void attachWithoutPopups() {
    manager.attach(cell);
    assertEquals(0, manager.updatesCount);
    assertEquals(0, manager.attachCount);
  }

  @Test
  public void attachWithPopup() {
    cell.bottomPopup().set(new TextCell());
    manager.attach(cell);
    assertEquals(1, manager.updatesCount);
    assertEquals(1, manager.attachCount);
  }

  @Test
  public void invisiblePopup() {
    attachWithPopup();
    cell.bottomPopup().get().visible().set(false);
    manager.updatePopupPositions();
    assertEquals(2, manager.updatesCount);
  }

  @Test
  public void events() {
    manager.attach(cell);
    TextCell popup = new TextCell();
    cell.bottomPopup().set(popup);
    manager.onEvent(new PropertyChangeEvent<Cell>(null, popup));
    assertEquals(1, manager.updatesCount);
    assertEquals(1, manager.attachCount);

    manager.updatePopupPositions();
    assertEquals(2, manager.updatesCount);

    cell.bottomPopup().set(null);
    manager.onEvent(new PropertyChangeEvent<Cell>(popup, null));
    assertEquals(2, manager.updatesCount);
    assertEquals(1, manager.detachCount);
  }

  @Test
  public void updateWhilePopupDetached() {
    attachWithPopup();
    Cell popup = cell.bottomPopup().get();

    cell.bottomPopup().set(null);
    manager.updatePopupPositions();
    assertEquals(1, manager.updatesCount);

    manager.onEvent(new PropertyChangeEvent<>(popup, null));
    assertEquals(1, manager.updatesCount);
    assertEquals(1, manager.detachCount);
  }

  private static class TestPopupManager extends BasePopupManager<Object> {
    int updatesCount = 0;
    int attachCount = 0;
    int detachCount = 0;

    @Override
    protected Collection<Mapper<? extends Cell, ?>> createContainer() {
      return new HashSet<>();
    }

    @Override
    protected Mapper<Cell, Object> attachPopup(Cell popup) {
      attachCount++;
      return new Mapper<Cell, Object>(popup, new Object()) {} ;
    }

    @Override
    protected void detachPopup(Mapper popupMapper) {
      detachCount++;
    }

    @Override
    protected Registration setPopupUpdate() {
      return Registration.EMPTY;
    }

    @Override
    protected PopupPositionUpdater<Object> getPositionUpdater(Mapper<? extends Cell, ?> popupMapper) {
      Cell popup = popupMapper.getSource();
      if (!popup.isAttached()) {
        throw new RuntimeException();
      }
      if (popup.getParent() == null) {
        throw new RuntimeException();
      }
      updatesCount++;
      return new TestPositionUpdater();
    }
  }

  private static class TestPositionUpdater extends PopupPositionUpdater<Object> {
    @Override
    protected void updateLeft(Rectangle target, Object popup) {
    }

    @Override
    protected void updateRight(Rectangle target, Object popup) {
    }

    @Override
    protected void updateFront(Rectangle target, Object popup) {
    }

    @Override
    protected void updateBottom(Rectangle target, Object popup) {
    }
  }
}