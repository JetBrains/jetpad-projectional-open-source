/*
 * Copyright 2012-2013 JetBrains s.r.o
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

import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.children.Composites;
import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.CollectionListener;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.Registration;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

public class ViewTest {
  static final ViewPropertySpec<String> NAME = new ViewPropertySpec<String>("name");

  static final ViewTrait X_NAME_TRAIT = new ViewTraitBuilder().set(NAME, "x").build();

  private String myPrevValue;
  private String myCurrentValue;

  private ViewContainer container = new ViewContainer();

  @Test
  public void viewAdd() {
    View v1 = newView();
    View v2 = newView();

    assertNull(v1.parent().get());

    v2.children().add(v1);

    assertSame(v2, v1.parent().get());
  }

  @Test
  public void viewRemove() {
    View v1 = newView();
    View v2 = newView();
    v1.children().add(v2);
    v1.children().remove(v2);

    assertNull(v2.parent().get());
  }

  @Test
  public void propSet() {
    View v = newView();
    v.prop(NAME).addHandler(nameChangeListener());

    v.set(NAME, "aaa");
    assertEquals("aaa", v.get(NAME));

    assertFired(null, "aaa");
  }

  @Test
  public void propChangeWithAddingTrait() {
    View v = newView();
    v.prop(NAME).addHandler(nameChangeListener());

    v.addTrait(X_NAME_TRAIT);
    assertEquals("x", v.get(NAME));

    assertFired(null, "x");
  }

  @Test
  public void propChangeWithRemovingTrait() {
    View v = newView();

    v.prop(NAME).addHandler(nameChangeListener());

    Registration reg = v.addTrait(X_NAME_TRAIT);
    reg.remove();

    assertNull(v.get(NAME));

    assertFired("x", null);
  }

  @Test
  public void ownNameTakesPrecedenceOverTraits() {
    View v = newView();

    v.addTrait(X_NAME_TRAIT);
    v.set(NAME, "y");

    assertEquals("y", v.get(NAME));
  }

  @Test
  public void validation() {
    View view = newView();

    assertFalse(view.valid().get());
    view.validate();
    assertTrue(view.valid().get());
  }

  @Test
  public void parentInvalidation() {
    View parent = newView();
    View child = newView();

    parent.children().add(child);
    parent.validate();

    child.invalidate();

    assertFalse(parent.valid().get());
  }

  @Test
  public void attachment() {
    View child = newView();

    container.root().validate();
    container.root().children().add(child);

    assertTrue(child.isAttached());
    assertFalse(container.root().valid().get());
  }

  @Test
  public void detachment() {
    View child = newView();
    container.root().children().add(child);
    container.root().validate();

    container.root().children().remove(child);

    assertFalse(child.isAttached());
    assertFalse(container.root().valid().get());
  }

  @Test
  public void relayoutPropertySetLeadsToInvalidation() {
    View view = newView();
    view.validate();

    view.set(new ViewPropertySpec<Object>("a", ViewPropertyKind.RELAYOUT), new Object());

    assertFalse(view.valid().get());
  }


  @Test
  public void repaintPropertySetLeadsToInvalidation() {
    final Value<Boolean> repaintCalled = new Value<Boolean>(false);

    View view = new MyView() {
      @Override
      void repaint() {
        super.repaint();
        repaintCalled.set(true);
      }
    };

    view.set(new ViewPropertySpec<Object>("b", ViewPropertyKind.REPAINT), new Object());

    assertTrue(repaintCalled.get());
  }

  @Test
  public void simpleMove() {
    View view = newView();

    List<Vector> origins = new ArrayList<Vector>();
    Registration reg = view.bounds().addHandler(originTracker(origins));
    view.move(new Vector(10, 10));
    view.move(new Vector(0, 0));

    reg.remove();
    view.move(new Vector(100, 100));

    assertEquals(Arrays.asList(new Vector(10, 10)), origins);
  }

  @Test
  public void containerMove() {
    View parent = newView();
    View child = newView();
    parent.children().add(child);

    List<Vector> origins = new ArrayList<Vector>();
    Registration reg = child.bounds().addHandler(originTracker(origins));

    parent.move(new Vector(10, 10));

    reg.remove();
    parent.move(new Vector(100, 20));

    assertEquals(Arrays.asList(new Vector(10, 10)), origins);
  }

  @Test(expected = IllegalStateException.class)
  public void cantFocusUnfocusable() {
    View view = newView();
    container.root().children().add(view);
    container.focusedView().set(view);
  }

  @Test
  public void focusing() {
    View view = newFocusableView();
    container.root().children().add(view);

    container.focusedView().set(view);

    assertTrue(view.focused().get());
  }

  @Test
  public void changingFocus() {
    View view1 = newFocusableView();
    View view2 = newFocusableView();

    container.root().children().addAll(Arrays.asList(view1, view2));

    container.focusedView().set(view1);
    container.focusedView().set(view2);

    assertFalse(view1.focused().get());
    assertTrue(view2.focused().get());
  }

  @Test(expected = IllegalStateException.class)
  public void cantChangeFocusableToFalseOnFocused() {
    View view = newFocusableView();
    container.root().children().add(view);

    container.focusedView().set(view);

    view.focusable().set(false);
  }

  @Test
  public void childAddedEventFiredWhenChildIsAdded() {
    CollectionListener<View> listener = mock(CollectionListener.class);

    final View view = newFocusableView();
    view.children().addListener(listener);

    View child = newFocusableView();
    view.children().add(child);

    verify(listener).onItemAdded(new CollectionItemEvent<View>(child, 0, true));
  }

  @Test
  public void childRemoveEventFiredWhenChildIsRemoved() {
    CollectionListener<View> listener = mock(CollectionListener.class);

    final View view = newFocusableView();
    final View child = newFocusableView();
    view.children().add(child);

    view.children().addListener(listener);
    view.children().remove(child);

    verify(listener).onItemRemoved(new CollectionItemEvent<View>(child, 0, false));
  }

  @Test
  public void parentPropertyChangeOnAdd() {
    EventHandler<PropertyChangeEvent<View>> handler = mock(EventHandler.class);

    View child = newFocusableView();
    child.parent().addHandler(handler);

    View parent = newFocusableView();
    parent.children().add(child);

    verify(handler).onEvent(new PropertyChangeEvent<View>(null, parent));
  }

  @Test
  public void parentPropertyChangeOnRemvoe() {
    EventHandler<PropertyChangeEvent<View>> handler = mock(EventHandler.class);

    View child = newFocusableView();
    View parent = newFocusableView();

    parent.children().add(child);
    child.parent().addHandler(handler);

    parent.children().remove(child);

    verify(handler).onEvent(new PropertyChangeEvent<View>(parent, null));
  }

  @Test
  public void viewDetachLeadsToFocusLoss() {
    View view = newFocusableView();
    container.root().children().add(view);
    container.focusedView().set(view);

    Composites.<View>removeFromParent(view);

    assertNull(container.focusedView().get());
  }

  @Test
  public void viewAtIgnoresInvisible() {
    View view = newFocusableView();

    container.root().children().add(view);
    view.visible().set(false);

    assertTrue(container.root().viewAt(view.bounds().get().center()) != view);
  }

  private EventHandler<PropertyChangeEvent<Rectangle>> originTracker(final List<Vector> origins) {
    return new EventHandler<PropertyChangeEvent<Rectangle>>() {
      @Override
      public void onEvent(PropertyChangeEvent<Rectangle> event) {
        origins.add(event.getNewValue().origin);
      }
    };
  }

  private EventHandler<PropertyChangeEvent<String>> nameChangeListener() {
    return new EventHandler<PropertyChangeEvent<String>>() {
      @Override
      public void onEvent(PropertyChangeEvent<String> event) {
        myPrevValue = event.getOldValue();
        myCurrentValue = event.getNewValue();
      }
    };
  }

  private void assertFired(String oldValue, String newValue) {
    assertEquals(oldValue, myPrevValue);
    assertEquals(newValue, myCurrentValue);
  }

  private View newView() {
    return new MyView();
  }

  private View newFocusableView() {
    View result = newView();
    result.focusable().set(true);
    return result;
  }

  private class MyView extends View {}
}
