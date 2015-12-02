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

import com.google.common.base.Strings;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.cell.indent.IndentContainerCellListener;
import jetbrains.jetpad.cell.indent.updater.CellWrapper;
import jetbrains.jetpad.cell.indent.updater.IndentUpdater;
import jetbrains.jetpad.cell.indent.updater.IndentUpdaterTarget;
import jetbrains.jetpad.cell.toUtil.AncestorUtil;
import jetbrains.jetpad.cell.toUtil.CounterUtil;
import jetbrains.jetpad.mapper.MappingContext;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.projectional.domUtil.DomTextEditor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

class IndentRootCellMapper extends BaseCellMapper<IndentCell> {
  private Set<BaseCellMapper<?>> myCellMappers;
  private IndentUpdater<Node> myIndentUpdater;
  private Registration myRegistration;
  private Timer myPositionUpdater;
  private Set<Cell> myChildrenWithPopups;

  IndentRootCellMapper(IndentCell source, CellToDomContext ctx) {
    super(source, ctx, DOM.createDiv());
    myCellMappers = createChildSet();

    myIndentUpdater = new IndentUpdater<Node>(
      getSource(),
      getTarget(),
      new IndentUpdaterTarget<Node>() {
        @Override
        public Element newLine() {
          Element result = DOM.createDiv();
          result.addClassName(CellContainerToDomMapper.CSS.horizontal());
          return result;
        }

        @Override
        public Element newIndent(int size) {
          Element result = DOM.createDiv();
          DomTextEditor editor = new DomTextEditor(result);
          editor.setText(Strings.repeat("  ", size));
          return result;
        }

        @Override
        public CellWrapper<Node> wrap(final Cell cell) {
          final BaseCellMapper<?> mapper = createMapper(cell);
          CounterUtil.updateOnAdd(getSource(), cell, mapper);
          mapper.setAncestorBackground(AncestorUtil.getAncestorBackground(getSource(), cell));

          myCellMappers.add(mapper);

          return new CellWrapper<Node>() {
            @Override
            public Node item() {
              return mapper.getTarget();
            }

            @Override
            public void remove() {
              CounterUtil.updateOnRemove(getSource(), cell, mapper);
              myCellMappers.remove(mapper);
            }
          };
        }

        @Override
        public List<Node> children(Node item) {
          if (item instanceof Element) {
            return divWrappedElementChildren((Element) item);
          } else {
            throw new IllegalStateException();
          }
        }

        @Override
        public Element parent(Node item) {
          if (item instanceof Element) {
            return item.getParentElement().getParentElement();
          } else {
            throw new IllegalStateException();
          }
        }
      }
    ) {
      @Override
      protected void onVisibilityChanged(Cell cell, PropertyChangeEvent<Boolean> event) {
        myIndentUpdater.visibilityChanged(cell, event);
      }
    };
  }

  @Override
  protected boolean isAutoChildManagement() {
    return false;
  }

  @Override
  protected boolean isAutoPopupManagement() {
    return false;
  }

  @Override
  protected void onAttach(MappingContext ctx) {
    super.onAttach(ctx);

    for (Cell child : getSource().children()) {
      myIndentUpdater.childAdded(child);
    }
    myIndentUpdater.initialized();

    myRegistration = getSource().addListener(new IndentContainerCellListener() {
      @Override
      public void childAdded(CollectionItemEvent<? extends Cell> event) {
        myIndentUpdater.childAdded(event.getNewItem());
      }

      @Override
      public void childRemoved(CollectionItemEvent<? extends Cell> event) {
        myIndentUpdater.childRemoved(event.getOldItem());
      }

      @Override
      public void propertyChanged(final Cell cell, final CellPropertySpec<?> prop, final PropertyChangeEvent<?> event) {
        if (CounterUtil.isCounterProp(prop)) {
          iterateLeaves(cell, new Handler<Cell>() {
            @Override
            public void handle(Cell item) {
              BaseCellMapper<?> mapper = (BaseCellMapper<?>) getDescendantMapper(item);
              if (mapper == null) {
                throw new IllegalStateException();
              }
              if (CounterUtil.update(mapper, prop, event)) {
                mapper.refreshProperties();
              }
            }
          });
        } else if (Cell.isPopupProp(prop)) {
          updateIndentCellPopup(cell, (PropertyChangeEvent<Cell>) event);
        } else if (prop == Cell.VISIBLE) {
          myIndentUpdater.visibilityChanged(cell, (PropertyChangeEvent<Boolean>) event);
        } else if (prop == Cell.BACKGROUND) {
          iterateLeaves(cell, new Handler<Cell>() {
            @Override
            public void handle(Cell item) {
              BaseCellMapper itemMapper = (BaseCellMapper) getDescendantMapper(item);
              itemMapper.setAncestorBackground(AncestorUtil.getAncestorBackground(getSource(), item));
              itemMapper.refreshProperties();
            }
          });
        }
      }

      private void updateIndentCellPopup(Cell cell, PropertyChangeEvent<Cell> event) {
        if (event.getOldValue() != null) {
          BaseCellMapper<?> popupMapper = (BaseCellMapper<?>) getDescendantMapper(event.getOldValue());
          myCellMappers.remove(popupMapper);
          popupMapper.getTarget().removeFromParent();
          myChildrenWithPopups.remove(popupMapper.getSource());
          if (myChildrenWithPopups.isEmpty()) {
            myPositionUpdater.cancel();
            myChildrenWithPopups = null;
          }
        }
        if (event.getNewValue() != null) {
          BaseCellMapper<?> popupMapper = createMapper(event.getNewValue());
          myCellMappers.add(popupMapper);
          final Element popupElement = popupMapper.getTarget();
          popupElement.getStyle().setPosition(Style.Position.ABSOLUTE);
          getContext().rootElement.appendChild(popupElement);
          updatePopupPositions(cell);
          if (myChildrenWithPopups == null) {
            myChildrenWithPopups = new HashSet<>();
            myPositionUpdater.scheduleRepeating(50);
          }
          myChildrenWithPopups.add(popupMapper.getSource());
        }
      }

      private void iterateLeaves(Cell cell, Handler<Cell> handler) {
        for (Cell child : cell.children()) {
          if (!Composites.isVisible(child)) continue;
          if (child instanceof IndentCell) {
            iterateLeaves(child, handler);
          } else {
            handler.handle(child);
          }
        }
      }
    });

    myPositionUpdater = new Timer() {
      @Override
      public void run() {
        for (Cell childWithPopup : myChildrenWithPopups) {
          updatePopupPositions(childWithPopup);
        }
      }
    };
  }

  @Override
  protected void onDetach() {
    List<Cell> children = getSource().children();
    for (int i = children.size() - 1; i >= 0; i--) {
      Cell c = children.get(i);
      myIndentUpdater.childRemoved(c);
    }
    myPositionUpdater.cancel();
    myRegistration.remove();
    super.onDetach();
  }
}