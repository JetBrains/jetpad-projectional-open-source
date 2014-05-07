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
package jetbrains.jetpad.projectional.view;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.animation.Animations;
import jetbrains.jetpad.event.Event;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.CollectionListener;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.composite.Composite;
import jetbrains.jetpad.model.composite.HasBounds;
import jetbrains.jetpad.model.composite.HasFocusability;
import jetbrains.jetpad.model.composite.HasVisibility;
import jetbrains.jetpad.model.event.*;
import jetbrains.jetpad.model.property.*;
import jetbrains.jetpad.model.util.ListMap;
import jetbrains.jetpad.base.animation.Animation;
import jetbrains.jetpad.values.Color;

import java.util.*;

public abstract class View implements Composite<View>, HasFocusability, HasVisibility, HasBounds {
  //debug attribute which is used in toString
  public static final ViewPropertySpec<String> NAME = new ViewPropertySpec<>("name", ViewPropertyKind.NONE, "");

  public static final ViewPropertySpec<Boolean> VISIBLE = new ViewPropertySpec<>("visible", ViewPropertyKind.RELAYOUT_PARENT, true);
  public static final ViewPropertySpec<Boolean> FOCUSED = new ViewPropertySpec<>("focused", ViewPropertyKind.NONE, false);
  public static final ViewPropertySpec<Boolean> FOCUSABLE = new ViewPropertySpec<>("focusabled", ViewPropertyKind.NONE, false);
  public static final ViewPropertySpec<Color> BACKGROUND = new ViewPropertySpec<>("background", ViewPropertyKind.REPAINT, null);
  public static final ViewPropertySpec<Color> BORDER_COLOR = new ViewPropertySpec<>("bordercolor", ViewPropertyKind.REPAINT, null);

  private View myParent;
  private ObservableList<View> myChildren;
  private ListMap<ViewPropertySpec<?>, Object> myProperties;
  private List<ViewTrait> myTraits;
  private Listeners<ViewListener> myListeners;
  private ViewContainer myContainer;

  private boolean myValid;
  private int myBaseLine = 0;

  private int myX;
  private int myY;
  private int myWidth;
  private int myHeight;
  private int myDeltaX;
  private int myDeltaY;
  private int myDeltaListenersCount;

  public Property<Boolean> visible() {
    return prop(VISIBLE);
  }

  public ReadableProperty<Boolean> focused() {
    return prop(FOCUSED);
  }

  void focused(boolean focused) {
    prop(FOCUSED).set(focused);
  }

  public Property<Boolean> focusable() {
    return prop(FOCUSABLE);
  }

  public Property<Color> background() {
    return prop(BACKGROUND);
  }

  public Property<Color> border() {
    return prop(BORDER_COLOR);
  }

  public Property<String> name() {
    return prop(NAME);
  }

  public ReadableProperty<View> parent() {
    return new BaseReadableProperty<View>() {
      @Override
      public View get() {
        return myParent;
      }

      @Override
      public Registration addHandler(final EventHandler<? super PropertyChangeEvent<View>> handler) {
        return addListener(new ViewAdapter() {
          @Override
          public void onParentChanged(PropertyChangeEvent<View> event) {
            handler.onEvent(event);
          }
        });
      }
    };
  }

  public ObservableList<View> children() {
    return new ExternalChildList();
  }

  public ViewContainer container() {
    return myContainer;
  }

  public boolean isAttached() {
    return myContainer != null;
  }

  protected void onAttach() {
  }

  protected void onDetach() {
  }

  void attach(ViewContainer container) {
    if (myContainer != null) {
      throw new IllegalStateException();
    }

    for (View child : children()) {
      child.attach(container);
    }

    myContainer = container;
    myContainer.viewAttached(this);

    onAttach();

    fire(new ListenerCaller<ViewListener>() {
      @Override
      public void call(ViewListener l) {
        l.onViewAttached();
      }
    });
  }

  void detach() {
    if (myContainer == null) {
      throw new IllegalStateException();
    }

    fire(new ListenerCaller<ViewListener>() {
      @Override
      public void call(ViewListener l) {
        l.onViewDetached();
      }
    });

    onDetach();

    for (View child : children()) {
      child.detach();
    }

    myContainer.viewDetached(this);
    myContainer = null;
  }

  public Registration addTrait(final ViewTrait trait) {
    if (myTraits == null) {
      myTraits = new ArrayList<>();
    }

    Runnable fire = createFiringRunnable(trait);
    myTraits.add(0, trait);
    fire.run();

    return new Registration() {
      @Override
      public void remove() {
        Runnable fire = createFiringRunnable(trait);
        myTraits.remove(trait);
        fire.run();

        if (myTraits.isEmpty()) {
          myTraits = null;
        }
      }
    };
  }

  private Runnable createFiringRunnable(ViewTrait t) {
    Set<ViewPropertySpec<?>> props = new HashSet<>();
    final List<Runnable> toRun = new ArrayList<>();

    ViewTrait current = t;
    while (current != null) {
      for (final ViewPropertySpec<?> p : current.properties()) {
        if (props.contains(p)) continue;
        final Object val = get(p);
        toRun.add(new Runnable() {
          @Override
          public void run() {
            final Object newVal = get(p);
            if (Objects.equal(val, newVal)) return;
            propertyChanged(p, new PropertyChangeEvent(val, newVal));
          }
        });
        props.add(p);
      }
      current = current.parent();
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

  <ValueT> ValueT get(ViewPropertySpec<ValueT> prop) {
    if (myProperties != null && myProperties.containsKey(prop)) {
      return (ValueT) myProperties.get(prop);
    }

    if (myTraits != null) {
      for (ViewTrait vt : myTraits) {
        ViewTrait current = vt;
        while (current != null) {
          if (current.hasValue(prop)) {
            return current.get(prop);
          }
          current = current.parent();
        }
      }
    }

    return prop.defaultValue(this);
  }

  <ValueT> void set(final ViewPropertySpec<ValueT> prop, ValueT value) {
    final ValueT oldValue = get(prop);

    if (myProperties == null && value == null) return;
    if (myProperties == null) {
      myProperties = new ListMap<>();
    }

    if (value == null) {
      myProperties.remove(prop);
      if (myProperties.isEmpty()) {
        myProperties = null;
      }
    } else {
      myProperties.put(prop, value);
    }

    final ValueT newValue = get(prop);
    if (!Objects.equal(newValue, oldValue)) {
      propertyChanged(prop, new PropertyChangeEvent<>(oldValue, newValue));
    }
  }


  protected Property<Vector> toParentOffsetProp(final ViewPropertySpec<Vector> spec) {
    return new ToParentOffsetProperty(prop(spec));
  }

  public <ValueT> Property<ValueT> prop(final ViewPropertySpec<ValueT> spec) {
    return new Property<ValueT>() {
      @Override
      public String getPropExpr() {
        return this + "." + spec;
      }

      @Override
      public ValueT get() {
        return View.this.get(spec);
      }

      @Override
      public void set(ValueT value) {
        View.this.set(spec, value);
      }

      @Override
      public Registration addHandler(final EventHandler<? super PropertyChangeEvent<ValueT>> handler) {
        return addListener(new ViewAdapter() {
          @Override
          public void onPropertySet(ViewPropertySpec<?> p, PropertyChangeEvent<?> event) {
            if (p != spec) return;
            handler.onEvent((PropertyChangeEvent<ValueT>) event);
          }
        });
      }
    };
  }

  Registration addListener(ViewListener l) {
    if (myListeners == null) {
      myListeners = new Listeners<>();
    }
    final Registration reg = myListeners.add(l);
    return new Registration() {
      @Override
      public void remove() {
        reg.remove();
        if (myListeners.isEmpty()) {
          myListeners = null;
        }
      }
    };
  }

  public EventSource<Object> customFeatureChange(final CustomViewFeatureSpec spec) {
    return new EventSource<Object>() {
      @Override
      public Registration addHandler(final EventHandler<? super Object> handler) {
        return addListener(new ViewAdapter() {
          @Override
          public void onCustomViewFeatureChange(CustomViewFeatureSpec s) {
            if (spec == s) {
              handler.onEvent(null);
            }
          }
        });
      }
    };
  }

  public ReadableProperty<Boolean> valid() {
    return new BaseReadableProperty<Boolean>() {
      @Override
      public Boolean get() {
        return myValid;
      }

      @Override
      public Registration addHandler(final EventHandler<? super PropertyChangeEvent<Boolean>> handler) {
        return addListener(new ViewAdapter() {
          @Override
          public void onViewValidated() {
            handler.onEvent(new PropertyChangeEvent<>(false, true));
          }

          @Override
          public void onViewInvalidated() {
            handler.onEvent(new PropertyChangeEvent<>(true, false));
          }
        });
      }
    };
  }

  public void invalidate() {
    if (!myValid) return;

    myValid = false;

    fire(new ListenerCaller<ViewListener>() {
      @Override
      public void call(ViewListener l) {
        l.onViewInvalidated();
      }
    });

    if (parent().get() != null) {
      parent().get().invalidate();
    }
  }

  protected void fire(ListenerCaller<ViewListener> caller) {
    if (myListeners != null) {
      myListeners.fire(caller);
    }
  }

  void invalidateTree() {
    invalidate();
    for (View child : children()) {
      child.invalidateTree();
    }
  }

  void repaint() {
    if (myContainer == null) return;
    myContainer.repaint(this);
  }

  protected void doValidate(ValidationContext ctx) {
    for (View child : children()) {
      child.validate();
    }
  }

  public void validate() {
    if (myValid) return;

    doValidate(new ValidationContext() {
      @Override
      public Vector origin() {
        return View.this.bounds().get().origin;
      }

      @Override
      public void bounds(Vector dim, int baseLine) {
        bounds(new Rectangle(View.this.bounds().get().origin, dim), baseLine);
      }

      @Override
      public void bounds(Rectangle bounds, int baseLine) {
        final Rectangle oldBounds = View.this.bounds().get();
        localBounds(bounds.sub(toRootDelta().get()));

        if (!Objects.equal(oldBounds, bounds)) {
          final PropertyChangeEvent<Rectangle> event = new PropertyChangeEvent<>(oldBounds, bounds);
          fire(new ListenerCaller<ViewListener>() {
            @Override
            public void call(ViewListener l) {
              l.onBoundsChanged(event);
            }
          });

          if (myContainer != null) {
            myContainer.boundsChanged(View.this, event);
          }
        }

        myBaseLine = baseLine;
      }
    });

    for (View child : children()) {
      if (!child.valid().get()) {
        throw new IllegalStateException("After doValidate all children must be valid");
      }
    }

    myValid = true;

    fire(new ListenerCaller<ViewListener>() {
      @Override
      public void call(ViewListener l) {
        l.onViewValidated();
      }
    });
  }

  protected ReadableProperty<Vector> toRootDelta() {
    return new BaseReadableProperty<Vector>() {
      @Override
      public Vector get() {
        View current = View.this;
        int x = 0;
        int y = 0;
        while (current != null) {
          x += current.myDeltaX;
          y += current.myDeltaY;
          current = current.myParent;
        }
        return new Vector(x, y);
      }

      @Override
      public Registration addHandler(final EventHandler<? super PropertyChangeEvent<Vector>> handler) {
        final Registration reg = addListener(new ViewAdapter() {
          @Override
          public void onToRootDeltaChanged(PropertyChangeEvent<Vector> change) {
            handler.onEvent(change);
          }
        });
        changeToRootDeltaListenersCount(+1);
        return new Registration() {
          @Override
          public void remove() {
            changeToRootDeltaListenersCount(-1);
            reg.remove();
          }
        };
      }
    };
  }

  public ReadableProperty<Rectangle> bounds() {
    return new BaseReadableProperty<Rectangle>() {
      @Override
      public Rectangle get() {
        return localBounds().add(toRootDelta().get());
      }

      @Override
      public Registration addHandler(final EventHandler<? super PropertyChangeEvent<Rectangle>> handler) {
        return new CompositeRegistration(
          toRootDelta().addHandler(new EventHandler<PropertyChangeEvent<Vector>>() {
            @Override
            public void onEvent(PropertyChangeEvent<Vector> event) {
              Vector oldDelta = event.getOldValue();
              Vector newDelta = event.getNewValue();
              handler.onEvent(new PropertyChangeEvent<>(localBounds().add(oldDelta), localBounds().add(newDelta)));
            }
          }),
          addListener(new ViewAdapter() {
            @Override
            public void onBoundsChanged(PropertyChangeEvent<Rectangle> change) {
              handler.onEvent(new PropertyChangeEvent<>(change.getOldValue(), change.getNewValue()));
            }
          })
        );
      }
    };
  }

  private Rectangle localBounds() {
    return new Rectangle(myX, myY, myWidth, myHeight);
  }

  private void localBounds(Rectangle r) {
    myX = r.origin.x;
    myY = r.origin.y;
    myWidth = r.dimension.x;
    myHeight = r.dimension.y;
  }

  public int baseLine() {
    return myBaseLine;
  }

  public View viewAt(Vector loc) {
    List<View> children = children();
    for (int i = children.size() - 1; i >= 0; i--) {
      View child = children.get(i);

      if (!child.visible().get()) continue;

      if (child.bounds().get().contains(loc)) {
        View result = child.viewAt(loc);
        if (result != null) return result;
      }
    }
    if (contains(loc)) {
      return this;
    }
    return null;
  }

  protected boolean contains(Vector loc) {
    return bounds().get().contains(loc);
  }

  private void changeToRootDeltaListenersCount(int delta) {
    myDeltaListenersCount += delta;
    if (myParent != null) {
      myParent.changeToRootDeltaListenersCount(delta);
    }
  }

  private <ValueT> void propertyChanged(final ViewPropertySpec<ValueT> prop, final PropertyChangeEvent<ValueT> event) {
    if (prop == FOCUSABLE && focused().get() && !((Boolean) event.getNewValue())) {
      throw new IllegalStateException();
    }
    prop.kind().invalidate(this);

    if (container() != null) {
      container().propertyChanged(this, prop, event);
    }

    fire(new ListenerCaller<ViewListener>() {
      @Override
      public void call(ViewListener l) {
        l.onPropertySet(prop, event);
      }
    });
  }

  /**
   * This method should only be called during layout
   */
  public void moveTo(Vector to) {
    Rectangle rect = bounds().get();
    move(to.sub(rect.origin));
  }

  /**
   * This method should only be called during layout
   */
  public void move(Vector delta) {
    if (delta.equals(Vector.ZERO)) return;

    myDeltaX += delta.x;
    myDeltaY += delta.y;

    if (parent().get() != null) {
      parent().get().invalidate();
    }

    if (container() != null) {
      Rectangle newBounds = bounds().get();
      Rectangle oldBounds = newBounds.sub(delta);
      container().boundsChanged(this, new PropertyChangeEvent<>(oldBounds, newBounds));
    }

    if (myDeltaListenersCount != 0) {
      fireMove(delta);
    }
  }

  private void fireMove(final Vector delta) {
    fire(new ListenerCaller<ViewListener>() {
      @Override
      public void call(ViewListener l) {
        Vector toRootDelta = toRootDelta().get();
        final PropertyChangeEvent<Vector> event = new PropertyChangeEvent<>(toRootDelta.sub(delta), toRootDelta);
        l.onToRootDeltaChanged(event);
      }
    });

    for (View child : children()) {
      if (child.myDeltaListenersCount == 0) continue;
      child.fireMove(delta);
    }
  }

  public <EventT extends Event> void dispatch(ViewEventSpec<EventT> spec, EventT event) {
    if (myTraits != null) {
      for (ViewTrait t : myTraits) {
        ViewTrait current = t;
        while (current != null) {
          current.dispatch(this, spec, event);
          if (event.isConsumed()) return;
          current = current.parent();
        }
      }
    }

    if (parent().get() != null) {
      parent().get().dispatch(spec, event);
    }
  }

  private void scrollInScrollViews() {
    View current = parent().get();

    while (current != null) {
      if (current instanceof ScrollView) {
        ScrollView scrolView = (ScrollView) current;
        scrolView.scrollTo(this);
      }

      current = current.parent().get();
    }
  }

  public void scrollTo() {
    scrollTo(new Rectangle(Vector.ZERO, getBounds().dimension));
  }

  public void scrollTo(Rectangle rect) {
    scrollInScrollViews();

    if (container() != null) {
      container().peer().scrollTo(rect, this);
    }
  }

  public EventSource<?> attachEvents() {
    return new EventSource<Object>() {
      @Override
      public Registration addHandler(final EventHandler<? super Object> handler) {
        return addListener(new ViewAdapter() {
          @Override
          public void onViewAttached() {
            handler.onEvent(null);
          }
        });
      }
    };
  }

  public EventSource<?> detachEvents() {
    return new EventSource<Object>() {
      @Override
      public Registration addHandler(final EventHandler<? super Object> handler) {
        return addListener(new ViewAdapter() {
          @Override
          public void onViewDetached() {
            handler.onEvent(null);
          }
        });
      }
    };
  }

  @Override
  public Rectangle getBounds() {
    return bounds().get();
  }

  @Override
  public String toString() {
    return toStringPrefix() + toStringSuffix();
  }

  protected String toStringPrefix() {
    String name = getClass().getName();
    int dotIndex = name.lastIndexOf('.');
    return dotIndex == 1 ? name : name.substring(dotIndex + 1);
  }

  protected String toStringSuffix() {
    String result = "@" + Integer.toHexString(hashCode());
    if (!Strings.isNullOrEmpty(name().get())) {
      result += "<" + name().get() + ">";
    }
    return result;
  }

  public Animation fadeIn(int duration) {
    if (container() == null) return Animations.finishedAnimation();
    return container().peer().fadeIn(this, duration);
  }

  public Animation fadeOut(int duration) {
    if (container() == null) return Animations.finishedAnimation();
    return container().peer().fadeOut(this, duration);
  }

  public Animation hideSlide(int duration) {
    if (container() == null) return Animations.finishedAnimation();
    return container().peer().hideSlide(this, duration);
  }

  public Animation showSlide(int duration) {
    if (container() == null) return Animations.finishedAnimation();
    return container().peer().showSlide(this, duration);
  }

  private class ChildList extends ObservableArrayList<View> {
    @Override
    public void add(final int index, final View item) {
      invalidate();

      item.myParent = View.this;
      if (isAttached()) {
        item.attach(myContainer);
      }

      item.fire(new ListenerCaller<ViewListener>() {
        @Override
        public void call(ViewListener l) {
          l.onParentChanged(new PropertyChangeEvent<>(null, View.this));
        }
      });
      super.add(index, item);
      fire(new ListenerCaller<ViewListener>() {
        @Override
        public void call(ViewListener l) {
          l.onChildAdded(new CollectionItemEvent<>(item, index, true));
        }
      });
    }

    @Override
    public View remove(final int index) {
      final View item = get(index);

      if (isAttached()) {
        item.detach();
      }
      final View oldParent = item.myParent;
      item.myParent = null;

      item.fire(new ListenerCaller<ViewListener>() {
        @Override
        public void call(ViewListener l) {
          l.onParentChanged(new PropertyChangeEvent<>(oldParent, null));
        }
      });
      View result = super.remove(index);
      fire(new ListenerCaller<ViewListener>() {
        @Override
        public void call(ViewListener l) {
          l.onChildRemoved(new CollectionItemEvent<>(item, index, false));
        }
      });
      invalidate();
      return result;
    }
  }

  private class ExternalChildList extends AbstractList<View> implements ObservableList<View> {
    @Override
    public View get(int index) {
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
    public View set(int index, View element) {
      if (myChildren == null) {
        throw new IndexOutOfBoundsException();
      }
      return myChildren.set(index, element);
    }

    @Override
    public void add(int index, View element) {
      ensureChildrenInitialized();
      myChildren.add(index, element);
    }

    private void ensureChildrenInitialized() {
      if (myChildren == null) {
        myChildren = new ChildList();
      }
    }

    @Override
    public View remove(int index) {
      if (myChildren == null) {
        throw new IndexOutOfBoundsException();
      }
      View result = myChildren.remove(index);
      if (myChildren.isEmpty()) {
        myChildren = null;
      }
      return result;
    }

    @Override
    public Registration addListener(final CollectionListener<View> l) {
      return View.this.addListener(new ViewAdapter() {
        @Override
        public void onChildAdded(CollectionItemEvent<View> event) {
          l.onItemAdded(event);
        }

        @Override
        public void onChildRemoved(CollectionItemEvent<View> event) {
          l.onItemRemoved(event);
        }
      });
    }

    @Override
    public Registration addHandler(final EventHandler<? super CollectionItemEvent<View>> handler) {
      return addListener(new CollectionListener<View>() {
        @Override
        public void onItemAdded(CollectionItemEvent<View> event) {
          handler.onEvent(event);
        }

        @Override
        public void onItemRemoved(CollectionItemEvent<View> event) {
          handler.onEvent(event);
        }
      });
    }
  }

  protected interface ValidationContext {
    Vector origin();
    void bounds(Vector rect, int baseLine);
    void bounds(Rectangle bounds, int baseLine);
  }

  private class ToParentOffsetProperty extends DerivedProperty<Vector> implements Property<Vector> {
    private Property<Vector> myBaseProperty;

    private ToParentOffsetProperty(Property<Vector> baseProperty) {
      super(baseProperty, toRootDelta());
      myBaseProperty = baseProperty;
    }

    @Override
    public Vector get() {
      return myBaseProperty.get().add(toRootDelta().get());
    }

    @Override
    public void set(Vector value) {
      myBaseProperty.set(value.sub(toRootDelta().get()));
    }
  }
}