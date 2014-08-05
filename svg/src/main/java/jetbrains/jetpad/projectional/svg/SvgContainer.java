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
package jetbrains.jetpad.projectional.svg;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ValueProperty;

public class SvgContainer {
  private Property<SvgRoot> mySvgRoot = new ValueProperty<SvgRoot>() {
    @Override
    public void set(SvgRoot value) {
      if (mySvgRoot.get() != null) {
        mySvgRoot.get().detach();
      }
      super.set(value);
      mySvgRoot.get().attach(SvgContainer.this);
    }
  };
  private Listeners<SvgContainerListener> myListeners = new Listeners<>();

  public SvgContainer(SvgRoot root) {
    mySvgRoot.set(root);
  }

  public Property<SvgRoot> root() {
    return mySvgRoot;
  }

  public Registration addListener(SvgContainerListener l) {
    return myListeners.add(l);
  }

  void propertyChanged(final SvgElement element, final SvgPropertySpec<?> spec, final PropertyChangeEvent<?> event) {
    myListeners.fire(new ListenerCaller<SvgContainerListener>() {
      @Override
      public void call(SvgContainerListener l) {
        l.onPropertySet(element, spec, event);
      }
    });
  }

  void svgElementAttached(final SvgElement element) {
    myListeners.fire(new ListenerCaller<SvgContainerListener>() {
      @Override
      public void call(SvgContainerListener l) {
        l.onElementAttached(element);
      }
    });
  }

  void svgElementDetached(final SvgElement element) {
    myListeners.fire(new ListenerCaller<SvgContainerListener>() {
      @Override
      public void call(SvgContainerListener l) {
        l.onElementDetached(element);
      }
    });
  }

  public void mousePressed(MouseEvent e) {
    dispatchMouseEvent(SvgEvents.MOUSE_PRESSED, e);
  }

  public void mouseReleased(MouseEvent e) {
    dispatchMouseEvent(SvgEvents.MOUSE_RELEASED, e);
  }

  private void dispatchMouseEvent(SvgEventSpec<MouseEvent> spec, MouseEvent e) {
    mySvgRoot.get().dispatch(spec, e);
  }
}
