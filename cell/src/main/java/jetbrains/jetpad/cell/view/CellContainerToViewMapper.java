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
package jetbrains.jetpad.cell.view;

import com.google.common.base.Supplier;
import jetbrains.jetpad.event.*;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.MappingContext;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.event.CompositeRegistration;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.Registration;
import jetbrains.jetpad.model.property.Properties;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.projectional.view.*;

import java.util.HashSet;
import java.util.Set;

public class CellContainerToViewMapper extends Mapper<CellContainer, View> {
  private CellContainerView myCellContainerView;
  private View myTargetView;
  private View myPopupView;
  private CellToViewContext myContext;
  private Set<TextCell> myWithCaret = new HashSet<TextCell>();
  private Set<Cell> myHighlighted = new HashSet<Cell>();

  public CellContainerToViewMapper(CellContainer source, View target, View targetView, View popupView) {
    super(source, target);

    myTargetView = targetView;
    myPopupView = popupView;

    myContext = new CellToViewContext(getTarget(), myTargetView, myPopupView);
  }

  @Override
  protected void onAttach(MappingContext ctx) {
    super.onAttach(ctx);

    getSource().setCellContainerPeer(createContainerPeer());

    if (myCellContainerView != null) {
      getTarget().children().add(myCellContainerView);
    }
  }

  @Override
  protected void onDetach() {
    super.onDetach();

    getSource().resetContainerPeer();
    if (myCellContainerView != null) {
      getTarget().children().remove(myCellContainerView);
    }
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forProperty(Properties.constant(true), myTargetView.focusable()));
    conf.add(Synchronizers.<Cell, View>forSingleRole(this, Properties.<Cell>constant(getSource().root),
      new WritableProperty<View>() {
        @Override
        public void set(View value) {
          myTargetView.children().clear();
          if (value != null) {
            myTargetView.children().add(value);
          }
        }
      },
      new MapperFactory<Cell, View>() {
        @Override
        public Mapper<? extends Cell, ? extends View> createMapper(Cell source) {
          return CellMappers.create(source, myContext);
        }
      }
    ));
    conf.add(Synchronizers.forRegistration(new Supplier<Registration>() {
      @Override
      public Registration get() {
        ViewTrait redispatchTrait = createRedistpatchTrait();
        myContext.containerFocused().set(myTargetView.focused().get());
        return new CompositeRegistration(
          getSource().addListener(createCellContainerListener()),
          getSource().focusedCell.addHandler(new EventHandler<PropertyChangeEvent<Cell>>() {
            @Override
            public void onEvent(PropertyChangeEvent<Cell> event) {
              if (event.getNewValue() != null) {
                myTargetView.container().focusedView().set(myTargetView);
              }
            }
          }),
          myTargetView.focused().addHandler(new EventHandler<PropertyChangeEvent<Boolean>>() {
            @Override
            public void onEvent(PropertyChangeEvent<Boolean> event) {
              myContext.containerFocused().set(event.getNewValue());
              for (TextCell cell : myWithCaret) {
                BaseCellMapper<?, ?> mapper = (BaseCellMapper<?, ?>) rootMapper().getDescendantMapper(cell);
                mapper.refreshProperties();
              }
              for (Cell cell : myHighlighted) {
                BaseCellMapper<?, ?> mapper = (BaseCellMapper<?, ?>) rootMapper().getDescendantMapper(cell);
                mapper.refreshProperties();
              }
            }
          }),
          getTarget().addTrait(redispatchTrait),
          myPopupView.addTrait(redispatchTrait)
        );
      }
    }));
  }

  private CellContainerPeer createContainerPeer() {
    final RootCellMapper rootMapper = rootMapper();
    return new CellContainerPeer() {
      @Override
      public int getCaretAt(TextCell tv, int x) {
        TextView textView = (TextView) rootMapper.getDescendantMapper(tv).getTarget();
        return textView.getCaretAt(x);
      }

      @Override
      public int getCaretOffset(TextCell tv, int caret) {
        Mapper<? super TextCell, ?> mapper = rootMapper.getDescendantMapper(tv);

        if (mapper == null) {
          throw new UnsupportedOperationException();
        }

        TextView textView = (TextView) mapper.getTarget();
        return textView.getCaretOffset(caret);
      }

      @Override
      public Rectangle getBounds(Cell cell) {
        Rectangle bounds = calculateBounds(cell);
        if (bounds == null) return new Rectangle(Vector.ZERO, Vector.ZERO);
        return bounds;
      }

      private Rectangle calculateBounds(Cell cell) {
        getTarget().container().root().validate();
        BaseCellMapper<?, ?> descendantMapper = (BaseCellMapper<?, ?>) rootMapper.getDescendantMapper(cell);
        if (descendantMapper == null) {
          Rectangle result = null;
          for (Cell child : cell.children()) {
            Rectangle childBounds = calculateBounds(child);
            if (childBounds == null) continue;
            if (result == null) {
              result = childBounds;
            } else {
              result = result.union(childBounds);
            }
          }
          if (result != null) {
            return result;
          } else {
            return null;
          }
        }
        return descendantMapper.getTarget().bounds().get();
      }

      @Override
      public void scrollTo(Cell cell) {
        BaseCellMapper<?, ?> mapper = (BaseCellMapper<?, ?>) rootMapper.getDescendantMapper(cell);
        if (mapper == null) return;
        mapper.getTarget().scrollTo();
      }

      @Override
      public void requestFocus() {
        getTarget().container().requestFocus();
      }

      @Override
      public ReadableProperty<Boolean> focused() {
        return getTarget().focused();
      }
    };
  }

  private RootCellMapper rootMapper() {
    return (RootCellMapper) getDescendantMapper(getSource().root);
  }

  private ViewTrait createRedistpatchTrait() {
    final View targetView = myTargetView;
    final CellContainer cellContainer = getSource();

    return new ViewTraitBuilder()
      .on(ViewEvents.MOUSE_PRESSED, new ViewEventHandler<MouseEvent>() {
        @Override
        public void handle(View view, MouseEvent e) {
          cellContainer.mousePressed(e);
          if (targetView.isAttached()) {
            targetView.container().focusedView().set(targetView);
          }
        }
      })
      .on(ViewEvents.MOUSE_RELEASED, new ViewEventHandler<MouseEvent>() {
        @Override
        public void handle(View view, MouseEvent e) {
          cellContainer.mouseReleased(e);
        }
      })
      .on(ViewEvents.MOUSE_MOVED, new ViewEventHandler<MouseEvent>() {
        @Override
        public void handle(View view, MouseEvent e) {
          cellContainer.mouseMoved(e);
        }
      })
      .on(ViewEvents.MOUSE_ENTERED, new ViewEventHandler<MouseEvent>() {
        @Override
        public void handle(View view, MouseEvent e) {
          cellContainer.mouseEntered(e);
        }
      })
      .on(ViewEvents.MOUSE_LEFT, new ViewEventHandler<MouseEvent>() {
        @Override
        public void handle(View view, MouseEvent e) {
          cellContainer.mouseLeft(e);
        }
      })
      .on(ViewEvents.MOUSE_DRAGGED, new ViewEventHandler<MouseEvent>() {
        @Override
        public void handle(View view, MouseEvent e) {
          cellContainer.mouseMoved(e);
        }
      })
      .on(ViewEvents.KEY_PRESSED, new ViewEventHandler<KeyEvent>() {
        @Override
        public void handle(View view, KeyEvent e) {
          cellContainer.keyPressed(e);
        }
      })
      .on(ViewEvents.KEY_RELEASED, new ViewEventHandler<KeyEvent>() {
        @Override
        public void handle(View view, KeyEvent e) {
          cellContainer.keyReleased(e);
        }
      })
      .on(ViewEvents.KEY_TYPED, new ViewEventHandler<KeyEvent>() {
        @Override
        public void handle(View view, KeyEvent e) {
          cellContainer.keyTyped(e);
        }
      })
      .on(ViewEvents.COPY, new ViewEventHandler<CopyCutEvent>() {
        @Override
        public void handle(View view, CopyCutEvent e) {
          cellContainer.copy(e);
        }
      })
      .on(ViewEvents.CUT, new ViewEventHandler<CopyCutEvent>() {
        @Override
        public void handle(View view, CopyCutEvent e) {
          cellContainer.cut(e);
        }
      })
      .on(ViewEvents.PASTE, new ViewEventHandler<PasteEvent>() {
        @Override
        public void handle(View view, PasteEvent e) {
          ClipboardContent content = e.getContent();
          if (content.isSupported(ContentKinds.TEXT)) {
            cellContainer.paste(content.get(ContentKinds.TEXT));
            e.consume();
          }
        }
      })
      .build();
  }

  private CellContainerAdapter createCellContainerListener() {
    return new CellContainerAdapter() {
      @Override
      public void onViewPropertyChanged(Cell cell, CellPropertySpec<?> prop, PropertyChangeEvent<?> event) {
        BaseCellMapper<?, ?> target = (BaseCellMapper<?, ?>) rootMapper().getDescendantMapper(cell);
        if (target == null) return;

        if (Cell.isPopupProp(prop)) {
          target.updatePopup((PropertyChangeEvent<Cell>) event);
          PropertyChangeEvent<Cell> changeEvent = (PropertyChangeEvent<Cell>) event;

          if (changeEvent.getOldValue() != null) {
            updateCachesOnRemove(changeEvent.getOldValue());
          }
          if (changeEvent.getNewValue() != null) {
            updateCachesOnAdd(changeEvent.getNewValue());
          }
        } else {
          target.refreshProperties();
        }

        if (prop == TextCell.CARET_VISIBLE) {
          TextCell text = (TextCell) cell;
          if (text.caretVisible().get()) {
            myWithCaret.add(text);
          } else {
            myWithCaret.remove(text);
          }
        }

        if (prop == Cell.HIGHLIGHTED) {
          if (cell.highlighted().get()) {
            myHighlighted.add(cell);
          } else {
            myHighlighted.remove(cell);
          }
        }
      }

      @Override
      public void onChildAdded(Cell parent, CollectionItemEvent<Cell> change) {
        BaseCellMapper<?, ?> parentMapper = (BaseCellMapper<?, ?>) rootMapper().getDescendantMapper(parent);
        if (parentMapper == null) return;

        parentMapper.childAdded(change.getIndex(), change.getItem());

        updateCachesOnAdd(change.getItem());
      }

      @Override
      public void onChildRemoved(Cell parent, CollectionItemEvent<Cell> change) {
        BaseCellMapper<?, ?> parentMapper = (BaseCellMapper<?, ?>) rootMapper().getDescendantMapper(parent);
        if (parentMapper == null) return;

        parentMapper.childRemoved(change.getIndex(), change.getItem());

        updateCachesOnRemove(change.getItem());
      }

      private void updateCachesOnAdd(Cell cell) {
        if (cell instanceof TextCell) {
          TextCell text = (TextCell) cell;
          if (text.caretVisible().get()) {
            myWithCaret.add(text);
          }
        }

        if (cell.highlighted().get()) {
          myHighlighted.add(cell);
        }

        for (Cell child : cell.children()) {
          updateCachesOnAdd(child);
        }
      }

      private void updateCachesOnRemove(Cell cell) {
        for (Cell child : cell.children()) {
          updateCachesOnRemove(child);
        }

        if (cell instanceof TextCell) {
          TextCell text = (TextCell) cell;
          myWithCaret.remove(text);
        }

        myHighlighted.remove(cell);
      }
    };
  }
}