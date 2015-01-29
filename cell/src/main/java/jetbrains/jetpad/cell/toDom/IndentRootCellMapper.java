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
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MappingContext;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.projectional.domUtil.DomTextEditor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class IndentRootCellMapper extends BaseCellMapper<IndentCell> {
  private Set<BaseCellMapper<?>> myCellMappers;
  private IndentUpdater<Node> myIndentUpdater;
  private Registration myRegistration;
  private Timer myPositionUpdater;
  private Map<Mapper<?, ?>, Runnable> myPositionUpdaters = new HashMap<>();

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
          myCellMappers.add(mapper);

          final Registration visibilityReg = cell.visible().addHandler(new EventHandler<PropertyChangeEvent<Boolean>>() {
            @Override
            public void onEvent(PropertyChangeEvent<Boolean> event) {
              myIndentUpdater.visibilityChanged(cell, event);
            }
          });

          return new CellWrapper<Node>() {
            @Override
            public Node item() {
              return mapper.getTarget();
            }

            @Override
            public void remove() {
              myCellMappers.remove(mapper);
              visibilityReg.remove();
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
      public void childAdded(CollectionItemEvent<Cell> event) {
        myIndentUpdater.childAdded(event.getItem());
      }

      @Override
      public void childRemoved(CollectionItemEvent<Cell> event) {
        myIndentUpdater.childRemoved(event.getItem());
      }

      @Override
      public void propertyChanged(Cell cell, final CellPropertySpec<?> prop, final PropertyChangeEvent<?> event) {
        if (prop == Cell.CURRENT_HIGHLIGHTED || prop == Cell.SELECTED) {
          iterateLeaves(cell, new Handler<Cell>() {
            @Override
            public void handle(Cell item) {
              BaseCellMapper<?> mapper = (BaseCellMapper<?>) getDescendantMapper(item);
              if (prop == Cell.CURRENT_HIGHLIGHTED) {
                if ((Boolean) event.getNewValue()) {
                  mapper.changeExtenralHighlight(1);
                } else {
                  mapper.changeExtenralHighlight(-1);
                }
                mapper.refreshProperties();
              } else if (prop == Cell.SELECTED) {
                if ((Boolean) event.getNewValue()) {
                  mapper.changeExternalSelect(1);
                } else {
                  mapper.changeExternalSelect(-1);
                }
                mapper.refreshProperties();
              }
            }
          });
        } else if (Cell.isPopupProp(prop)) {
          PropertyChangeEvent<Cell> popupChangeEvent = (PropertyChangeEvent<Cell>) event;
          if (popupChangeEvent.getOldValue() != null) {
            BaseCellMapper<?> popupMapper = (BaseCellMapper<?>) getDescendantMapper(popupChangeEvent.getOldValue());
            myCellMappers.remove(popupMapper);
            popupMapper.getTarget().removeFromParent();

            myPositionUpdaters.remove(popupMapper);
            if (myPositionUpdaters.isEmpty()) {
              myPositionUpdater.cancel();
            }
          }

          if (popupChangeEvent.getNewValue() != null) {
            BaseCellMapper<?> popupMapper = createMapper(popupChangeEvent.getNewValue());
            myCellMappers.add(popupMapper);

            final PopupPositioner positioner = new PopupPositioner(getContext());
            final Rectangle bounds = cell.getBounds();

            final Element popupElement = popupMapper.getTarget();
            popupElement.getStyle().setPosition(Style.Position.ABSOLUTE);
            getContext().rootElement.appendChild(popupElement);

            Runnable updater = new Runnable() {
              @Override
              public void run() {
                if (prop == Cell.BOTTOM_POPUP) {
                  positioner.positionBottom(bounds, popupElement);
                } else if (prop == Cell.FRONT_POPUP) {
                  positioner.positionFront(bounds, popupElement);
                } else if (prop == Cell.LEFT_POPUP) {
                  positioner.positionLeft(bounds, popupElement);
                } else if (prop == Cell.RIGHT_POPUP) {
                  positioner.positionRight(bounds, popupElement);
                }
              }
            };

            updater.run();

            if (myPositionUpdaters.isEmpty()) {
              myPositionUpdater.scheduleRepeating(50);
            }
            myPositionUpdaters.put(popupMapper, updater);
          }
        } else if (prop == Cell.VISIBLE) {
          myIndentUpdater.visibilityChanged(cell, (PropertyChangeEvent<Boolean>) event);
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
        for (Runnable r : myPositionUpdaters.values()) {
          r.run();
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