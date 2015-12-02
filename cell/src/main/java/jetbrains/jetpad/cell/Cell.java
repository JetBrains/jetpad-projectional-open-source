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
package jetbrains.jetpad.cell;

import com.google.common.base.Objects;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.event.CellEventSpec;
import jetbrains.jetpad.cell.event.EventPriority;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitEventSpec;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.event.Event;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.CollectionListener;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.composite.*;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;
import jetbrains.jetpad.model.property.BaseReadableProperty;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.model.util.ListMap;
import jetbrains.jetpad.values.Color;

import java.util.*;

public abstract class Cell implements NavComposite<Cell>, HasVisibility, HasFocusability, HasBounds {

  public static final CellPropertySpec<Cell> BOTTOM_POPUP = new CellPropertySpec<>("bottomPopup");
  public static final CellPropertySpec<Cell> FRONT_POPUP = new CellPropertySpec<>("frontPopup");
  public static final CellPropertySpec<Cell> LEFT_POPUP = new CellPropertySpec<>("leftPopup");
  public static final CellPropertySpec<Cell> RIGHT_POPUP = new CellPropertySpec<>("rightPopup");

  public static final List<CellPropertySpec<Cell>> POPUP_SPECS =
      Collections.unmodifiableList(Arrays.asList(LEFT_POPUP, RIGHT_POPUP, BOTTOM_POPUP, FRONT_POPUP));

  public static final CellPropertySpec<Boolean> FOCUSED = new CellPropertySpec<>("focused", false);
  public static final CellPropertySpec<Boolean> VISIBLE = new CellPropertySpec<>("visible", true);
  public static final CellPropertySpec<Boolean> POPUP = new CellPropertySpec<>("popup", false);
  public static final CellPropertySpec<Boolean> SELECTED = new CellPropertySpec<>("selected", false);
  public static final CellPropertySpec<Boolean> FOCUS_HIGHLIGHTED = new CellPropertySpec<>("focusHighlighted", false);
  public static final CellPropertySpec<Boolean> PAIR_HIGHLIGHTED = new CellPropertySpec<>("pairHighlighted", false);
  public static final CellPropertySpec<Boolean> FOCUSABLE = new CellPropertySpec<>("focusable", false);

  public static final CellPropertySpec<Color> BACKGROUND = new CellPropertySpec<>("background");
  public static final CellPropertySpec<Color> BORDER_COLOR = new CellPropertySpec<>("borderColor");
  public static final CellPropertySpec<Boolean> HAS_SHADOW = new CellPropertySpec<>("hasShadow", false);

  public static final CellPropertySpec<Boolean> HAS_ERROR = new CellPropertySpec<>("hasError", false);
  public static final CellPropertySpec<Boolean> HAS_WARNING = new CellPropertySpec<>("hasWarning", false);


  public static boolean isPopupProp(CellPropertySpec<?> prop) {
    return POPUP_SPECS.contains(prop);
  }

  private CellTrait[] myCellTraits = CellTrait.EMPTY_ARRAY;

  private List<Cell> myChildren;
  private CellContainer myContainer;
  private Cell myParent;
  private Cell myNext;
  private Cell myPrev;
  private ListMap<CellPropertySpec<?>, Object> myProperties;
  private Listeners<CellListener> myListeners;

  protected Cell() {
    this(null);
  }

  Cell(CellContainer cellContainer) {
    if (cellContainer != null) {
      attach(cellContainer);
    }
  }

  public ObservableList<Cell> children() {
    return new ExternalChildList();
  }

  @Override
  public Cell nextSibling() {
    return myNext;
  }

  @Override
  public Cell prevSibling() {
    return myPrev;
  }

  @Override
  public Cell firstChild() {
    if (myChildren == null) return null;
    return myChildren.get(0);
  }

  @Override
  public Cell lastChild() {
    if (myChildren == null) return null;
    return myChildren.get(myChildren.size() - 1);
  }

  public Property<Boolean> visible() {
    return getProp(VISIBLE);
  }

  public Property<Boolean> selected() {
    return getProp(SELECTED);
  }

  public Property<Boolean> pairHighlighted() {
    return getProp(PAIR_HIGHLIGHTED);
  }

  public Property<Boolean> focusHighlighted() {
    return getProp(FOCUS_HIGHLIGHTED);
  }

  public Property<Boolean> focusable() {
    return getProp(FOCUSABLE);
  }

  public Property<Color> background() {
    return getProp(BACKGROUND);
  }

  public Property<Color> borderColor() {
    return getProp(BORDER_COLOR);
  }

  public Property<Cell> bottomPopup() {
    return getProp(BOTTOM_POPUP);
  }

  public Property<Cell> frontPopup() {
    return getProp(FRONT_POPUP);
  }

  public Property<Cell> leftPopup() {
    return getProp(LEFT_POPUP);
  }

  public Property<Cell> rightPopup() {
    return getProp(RIGHT_POPUP);
  }

  public Property<Boolean> hasShadow() {
    return getProp(HAS_SHADOW);
  }

  public Property<Boolean> hasError() {
    return getProp(HAS_ERROR);
  }

  public Property<Boolean> hasWarning() {
    return getProp(HAS_WARNING);
  }

  public <EventT extends Event> void dispatch(EventT e, CellEventSpec<EventT> spec) {
    dispatchStep(e, spec);
    if (e.isConsumed()) return;

    if (getParent() != null) {
      getParent().dispatch(e, spec);
    }
  }

  private <EventT extends Event> void dispatchStep(EventT e, CellEventSpec<EventT> spec) {
    if (spec == CellEventSpec.KEY_PRESSED || spec == CellEventSpec.KEY_RELEASED || spec == CellEventSpec.KEY_TYPED) {
      for (EventPriority p : EventPriority.values()) {
        for (CellTrait t : myCellTraits) {
          if (p == EventPriority.LOW) {
            if (spec == CellEventSpec.KEY_PRESSED) {
              t.onKeyPressedLowPriority(this, (KeyEvent) e);
            } else if (spec == CellEventSpec.KEY_RELEASED) {
              t.onKeyReleasedLowPriority(this, (KeyEvent) e);
            } else {
              t.onKeyTypedLowPriority(this, (KeyEvent) e);
            }
          } else {
            if (spec == CellEventSpec.KEY_PRESSED) {
              t.onKeyPressed(this, (KeyEvent) e);
            } else if (spec == CellEventSpec.KEY_RELEASED) {
              t.onKeyReleased(this, (KeyEvent) e);
            } else {
              t.onKeyTyped(this, (KeyEvent) e);
            }
          }
          if (e.isConsumed()) return;
        }
      }
    } else {
      for (CellTrait t : myCellTraits) {
        spec.dispatch(this, e, t);
        if (e.isConsumed()) return;
      }
    }
  }

  public <EventT extends Event> void dispatch(EventT e, CellTraitEventSpec<EventT> spec) {
    for (CellTrait t : myCellTraits) {
      t.onCellTraitEvent(this, spec, e);
      if (e.isConsumed()) return;
    }

    if (getParent() != null && spec.isBubbling()) {
      getParent().dispatch(e, spec);
    }
  }

  public Registration addTrait(final CellTrait trait) {
    Runnable r = createFiringRunnable(trait);
    CellTrait[] newTraits = new CellTrait[myCellTraits.length + 1];
    newTraits[0] = trait;
    System.arraycopy(myCellTraits, 0, newTraits, 1, myCellTraits.length);
    myCellTraits = newTraits;
    r.run();
    return new Registration() {
      @Override
      protected void doRemove() {
        Runnable r = createFiringRunnable(trait);
        int index = Arrays.asList(myCellTraits).indexOf(trait);
        CellTrait[] newTraits = new CellTrait[myCellTraits.length - 1];
        System.arraycopy(myCellTraits, 0, newTraits, 0, index);
        System.arraycopy(myCellTraits, index + 1, newTraits, index, myCellTraits.length - index - 1);
        myCellTraits = newTraits;
        r.run();
      }
    };
  }

  private Runnable createFiringRunnable(CellTrait t) {
    final List<Runnable> toRun = new ArrayList<>();

    for (final CellPropertySpec<?> p : t.getChangedProperties(this)) {
      final Object val = get(p);
      toRun.add(new Runnable() {
        @Override
        public void run() {
          final Object newVal = get(p);
          if (Objects.equal(val, newVal)) return;
          firePropertyChange(p, new PropertyChangeEvent(val, newVal));
        }
      });
    }

    return new Runnable() {
      @Override
      public void run() {
        for (Runnable r : toRun) {
          r.run();
        }
      }
    };
  }

  @Override
  public final Cell getParent() {
    return myParent;
  }

  public boolean isPopup() {
    return get(POPUP);
  }

  public final ReadableProperty<Cell> parent() {
    return new BaseReadableProperty<Cell>() {
      @Override
      public Cell get() {
        return myParent;
      }

      @Override
      public Registration addHandler(final EventHandler<? super PropertyChangeEvent<Cell>> handler) {
        return addListener(new CellAdapter() {
          @Override
          public void onParentChanged(PropertyChangeEvent<Cell> event) {
            handler.onEvent(event);
          }
        });
      }
    };
  }

  public ReadableProperty<CellContainer> cellContainer() {
    return new ReadableProperty<CellContainer>() {
      @Override
      public String getPropExpr() {
        return "cell.cellContainer";
      }

      @Override
      public CellContainer get() {
        return myContainer;
      }

      @Override
      public Registration addHandler(final EventHandler<? super PropertyChangeEvent<CellContainer>> handler) {
        return addListener(new CellAdapter() {
          @Override
          public void onAttach(CellContainer container) {
            handler.onEvent(new PropertyChangeEvent<>(null, container));
          }

          @Override
          public void onDetach(CellContainer container) {
            handler.onEvent(new PropertyChangeEvent<>(container, null));
          }
        });
      }
    };
  }

  public ReadableProperty<Boolean> focused() {
    return getProp(FOCUSED);
  }

  boolean canFocus() {
    if (!focusable().get()) return false;
    if (myContainer == null) return false;
    if (!visible().get()) return false;
    return true;
  }

  public void focus() {
    if (!canFocus()) {
      throw new IllegalStateException();
    }
    myContainer.focusedCell.set(this);
  }

  public Vector origin() {
    return getPeer().getBounds(this).origin;
  }

  public Vector dimension() {
    return getPeer().getBounds(this).dimension;
  }

  public Rectangle getBounds() {
    return getPeer().getBounds(this);
  }

  public void scrollTo() {
    scrollTo(new Rectangle(Vector.ZERO, getBounds().dimension));
  }

  public void scrollTo(Rectangle rect) {
    getPeer().scrollTo(rect, this);
  }

  protected CellContainerPeer getPeer() {
    if (myContainer == null) {
      throw new IllegalStateException();
    }
    return myContainer.getCellContainerPeer();
  }

  public Registration addListener(CellListener l) {
    if (myListeners == null) {
      myListeners = new Listeners<>();
    }
    return myListeners.add(l);
  }

  public <ValueT> ValueT get(CellPropertySpec<ValueT> prop) {
    if (myProperties == null || !myProperties.containsKey(prop)) {
      return getDefaultValue(prop);
    }
    return (ValueT) myProperties.get(prop);
  }

  public <ValueT> Registration set(final CellPropertySpec<ValueT> prop, ValueT value) {
    final ValueT old = get(prop);
    if (Objects.equal(value, old)) return Registration.EMPTY;
    final PropertyChangeEvent<ValueT> event = new PropertyChangeEvent<>(old, value);
    beforePropertySet(prop, event);
    if (Objects.equal(value, getDefaultValue(prop))) {
      myProperties.remove(prop);
      if (myProperties.isEmpty()) {
        myProperties = null;
      }
    } else {
      if (myProperties == null) {
        myProperties = new ListMap<>();
      }
      myProperties.put(prop, value);
    }
    firePropertyChange(prop, event);
    return new Registration() {
      @Override
      protected void doRemove() {
        set(prop, old);
      }
    };
  }

  private <ValueT> void firePropertyChange(final CellPropertySpec<ValueT> prop, final PropertyChangeEvent<ValueT> event) {
    propertySet(prop, event);
    onPropertySet(prop, event);
    for (CellTrait t : myCellTraits) {
      t.onPropertyChanged(this, prop, event);
    }
    if (myListeners != null) {
      myListeners.fire(new ListenerCaller<CellListener>() {
        @Override
        public void call(CellListener l) {
          l.onPropertyChanged(prop, event);
        }
      });
    }
    if (myContainer != null) {
      myContainer.cellPropertyChanged(this, prop, event);
    }
  }

  private <ValueT> ValueT getDefaultValue(CellPropertySpec<ValueT> prop) {
    for (CellTrait t : myCellTraits) {
      Object result = t.get(this, prop);
      if (result == CellTrait.NULL) return null;
      if (result != null) {
        return (ValueT) result;
      }
    }
    return prop.getDefault(this);
  }

  public <ValueT> ValueT get(CellTraitPropertySpec<ValueT> prop) {
    ValueT result = getRaw(prop);
    if (result != null) return result;
    return prop.getDefault(this);
  }

  public <ValueT> ValueT getRaw(CellTraitPropertySpec<ValueT> prop) {
    for (CellTrait t : myCellTraits) {
      Object result = t.get(this, prop);
      if (result == CellTrait.NULL) return null;
      if (result != null) {
        return (ValueT) result;
      }
    }
    return null;
  }

  public <ValueT> Property<ValueT> getProp(final CellPropertySpec<ValueT> prop) {
    class MyProperty extends BaseReadableProperty<ValueT> implements Property<ValueT> {
      @Override
      public ValueT get() {
        return Cell.this.get(prop);
      }

      @Override
      public void set(ValueT value) {
        Cell.this.set(prop, value);
      }

      @Override
      public Registration addHandler(final EventHandler<? super PropertyChangeEvent<ValueT>> handler) {
        return addListener(new CellAdapter() {
          @Override
          public void onPropertyChanged(CellPropertySpec<?> cellProp, PropertyChangeEvent<?> event) {
            if (!Objects.equal(prop, cellProp)) return;
            handler.onEvent((PropertyChangeEvent<ValueT>) event);
          }
        });
      }

      @Override
      public String getPropExpr() {
        return Cell.this + "." + prop;
      }
    }

    return new MyProperty();
  }

  List<Cell> popups() {
    if (myProperties == null) return Collections.emptyList();
    List<Cell> result = null;
    for (CellPropertySpec<Cell> ps : POPUP_SPECS) {
      Cell cell = (Cell) myProperties.get(ps);
      if (cell != null) {
        if (result == null) {
          result = new ArrayList<>();
        }
        result.add(cell);
      }
    }
    return result == null ? Collections.<Cell>emptyList() : result;
  }

  public boolean isAttached() {
    return myContainer != null;
  }

  private void attach(final CellContainer container) {
    if (container == null) {
      throw new IllegalStateException();
    }
    if (myContainer != null) {
      throw new IllegalStateException();
    }
    myContainer = container;
    myContainer.cellAdded(this);
    if (myListeners != null) {
      myListeners.fire(new ListenerCaller<CellListener>() {
        @Override
        public void call(CellListener l) {
          l.onAttach(container);
        }
      });
    }
    if (myChildren != null) {
      for (Cell child : myChildren) {
        child.attach(container);
      }
    }
    for (Cell popup : popups()) {
      popup.attach(container);
    }
  }

  private void detach() {
    if (myContainer == null) {
      throw new IllegalStateException();
    }
    myContainer.cellRemoved(this);
    final CellContainer oldContainer = myContainer;
    myContainer = null;
    if (myListeners != null) {
      myListeners.fire(new ListenerCaller<CellListener>() {
        @Override
        public void call(CellListener l) {
          l.onDetach(oldContainer);
        }
      });
    }
    if (myChildren != null) {
      for (Cell child : myChildren) {
        child.detach();
      }
    }
    for (Cell popup : popups()) {
      popup.detach();
    }
  }

  public void removeFromParent() {
    if (myParent == null) {
      throw new IllegalStateException();
    }
    if (myParent.myChildren != null && myParent.myChildren.contains(this)) {
      myParent.children().remove(this);
      return;
    }
    for (CellPropertySpec<Cell> prop : POPUP_SPECS) {
      if (myParent.get(prop) == this) {
        myParent.set(prop, null);
        return;
      }
    }
    throw new IllegalStateException();
  }

  private <ValueT> void beforePropertySet(CellPropertySpec<ValueT> prop, PropertyChangeEvent<ValueT> event) {
    if (isPopupProp(prop)) {
      PropertyChangeEvent<Cell> cellChangeEvent = (PropertyChangeEvent<Cell>) event;
      Cell oldValue = cellChangeEvent.getOldValue();
      Cell value = cellChangeEvent.getNewValue();
      if (myContainer != null) {
        if (oldValue != null) {
          myContainer.popupRemoved(oldValue);
          oldValue.set(POPUP, false);
          oldValue.changeParent(null);
        }
        if (value != null) {
          myContainer.popupAdded(value);
          value.set(POPUP, true);
          value.changeParent(Cell.this);
        }
      }
    }
  }

  private <ValueT> void propertySet(CellPropertySpec<ValueT> prop, PropertyChangeEvent<ValueT> event) {
    if (isPopupProp(prop)) {
      PropertyChangeEvent<Cell> cellChangeEvent = (PropertyChangeEvent<Cell>) event;
      Cell oldValue = cellChangeEvent.getOldValue();
      Cell value = cellChangeEvent.getNewValue();
      if (isAttached()) {
        if (oldValue != null) {
          oldValue.detach();
        }
        if (value != null) {
          value.attach(myContainer);
        }
      }
    }
  }

  private void changeParent(Cell newParent) {
    final PropertyChangeEvent<Cell> event = myListeners != null ? new PropertyChangeEvent<>(myParent, newParent) : null;
    myParent = newParent;
    if (myListeners != null) {
      myListeners.fire(new ListenerCaller<CellListener>() {
        @Override
        public void call(CellListener l) {
          l.onParentChanged(event);
        }
      });
    }
  }

  protected void onPropertySet(CellPropertySpec<?> prop, PropertyChangeEvent<?> event) {
  }

  protected void onBeforeChildAdded(CollectionItemEvent<Cell> event) {
  }

  protected void onChildAdded(CollectionItemEvent<? extends Cell> event) {
  }

  protected void onBeforeChildRemoved(CollectionItemEvent<Cell> event) {
  }

  protected void onChildRemoved(CollectionItemEvent<? extends Cell> event) {
  }

  public <ValueT> Registration set(final CellTraitPropertySpec<ValueT> spec, final ValueT value) {
    return addTrait(new CellTrait() {
      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> propSpec) {
        if (spec == propSpec) {
          return value;
        }
        return super.get(cell, spec);
      }
    });
  }

  @Override
  public String toString() {
    String name = getClass().getName();
    int dotIndex = name.lastIndexOf('.');
    String className = dotIndex == 1 ? name : name.substring(dotIndex + 1);
    return className + "@" + Integer.toHexString(hashCode());
  }

  private class ChildList extends ObservableArrayList<Cell> {
    public ChildList() {
      addListener(new CollectionAdapter<Cell>() {
        @Override
        public void onItemAdded(final CollectionItemEvent<? extends Cell> event) {
          if (isAttached()) {
            ((Cell) event.getNewItem()).attach(myContainer);
          }
          onChildAdded(event);
          if (myContainer != null) {
            myContainer.cellChildAdded(Cell.this, event);
          }
          if (myListeners != null) {
            myListeners.fire(new ListenerCaller<CellListener>() {
              @Override
              public void call(CellListener l) {
                l.onChildAdded(event);
              }
            });
          }
        }

        @Override
        public void onItemRemoved(final CollectionItemEvent<? extends Cell> event) {
          if (isAttached()) {
            ((Cell) event.getOldItem()).detach();
          }
          onChildRemoved(event);
          if (myContainer != null) {
            myContainer.cellChildRemoved(Cell.this, event);
          }
          if (myListeners != null) {
            myListeners.fire(new ListenerCaller<CellListener>() {
              @Override
              public void call(CellListener l) {
                l.onChildRemoved(event);
              }
            });
          }
        }
      });
    }

    @Override
    protected void checkAdd(int index, Cell item) {
      super.checkAdd(index, item);
      if (item.getParent() != null) {
        throw new IllegalStateException();
      }
    }

    @Override
    protected void beforeItemAdded(int index, Cell item) {
      attachItem(item, index - 1, index, index);
    }

    @Override
    protected void checkSet(int index, Cell oldItem, Cell newItem) {
      checkAdd(index, newItem);
      checkRemove(index, oldItem);
    }

    @Override
    protected void beforeItemSet(int index, Cell oldItem, Cell newItem) {
      detachItem(oldItem, index);
      attachItem(newItem, index - 1, index, index + 1);
    }

    @Override
    protected void beforeItemRemoved(int index, Cell item) {
      detachItem(item, index);
    }

    private void attachItem(Cell item, int prevIndex, int index, int nextIndex) {
      onBeforeChildAdded(new CollectionItemEvent<>(null, item, index, CollectionItemEvent.EventType.ADD));

      item.changeParent(Cell.this);

      if (prevIndex >= 0 && prevIndex < size()) {
        Cell prev = get(prevIndex);
        prev.myNext = item;
        item.myPrev = prev;
      }
      if (nextIndex >= 0 && nextIndex < size()) {
        Cell next = get(nextIndex);
        next.myPrev = item;
        item.myNext = next;
      }
    }

    private void detachItem(Cell item, int index) {
      if (isAttached() && myContainer.focusedCell.get() != null && Composites.isDescendant(item, myContainer.focusedCell.get())) {
        myContainer.focusedCell.set(null);
      }

      onBeforeChildRemoved(new CollectionItemEvent<>(item, null, index, CollectionItemEvent.EventType.REMOVE));

      item.changeParent(null);
      final Cell prev = item.myPrev;
      final Cell next = item.myNext;
      if (prev != null) {
        prev.myNext = next;
      }
      if (next != null) {
        next.myPrev = prev;
      }
      item.myPrev = null;
      item.myNext = null;
    }
  }

  private class ExternalChildList extends AbstractList<Cell> implements ObservableList<Cell> {
    @Override
    public Cell get(int index) {
      if (myChildren == null) {
        throw new IndexOutOfBoundsException();
      }
      return myChildren.get(index);
    }

    @Override
    public int size() {
      if (myChildren == null) return 0;
      return myChildren.size();
    }

    @Override
    public Cell set(int index, Cell element) {
      if (myChildren == null) {
        throw new IndexOutOfBoundsException();
      }
      return myChildren.set(index, element);
    }

    @Override
    public void add(int index, Cell element) {
      ensureChildrenInitialized();
      myChildren.add(index, element);
    }

    private void ensureChildrenInitialized() {
      if (myChildren == null) {
        myChildren = new ChildList();
      }
    }

    @Override
    public Cell remove(int index) {
      if (myChildren == null) {
        throw new IndexOutOfBoundsException();
      }
      Cell result = myChildren.remove(index);
      if (myChildren.isEmpty()) {
        myChildren = null;
      }
      return result;
    }

    @Override
    public Registration addListener(final CollectionListener<Cell> l) {
      return Cell.this.addListener(new CellAdapter() {
        @Override
        public void onChildAdded(CollectionItemEvent<? extends Cell> event) {
          l.onItemAdded(event);
        }

        @Override
        public void onChildRemoved(CollectionItemEvent<? extends Cell> event) {
          l.onItemRemoved(event);
        }
      });
    }

    @Override
    public Registration addHandler(final EventHandler<? super CollectionItemEvent<? extends Cell>> handler) {
      return addListener(new CollectionListener<Cell>() {
        @Override
        public void onItemAdded(CollectionItemEvent<? extends Cell> event) {
          handler.onEvent(event);
        }

        @Override
        public void onItemSet(CollectionItemEvent<? extends Cell> event) {
          handler.onEvent(event);
        }

        @Override
        public void onItemRemoved(CollectionItemEvent<? extends Cell> event) {
          handler.onEvent(event);
        }
      });
    }
  }
}