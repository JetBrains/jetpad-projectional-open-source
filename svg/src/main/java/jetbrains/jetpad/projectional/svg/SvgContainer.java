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
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.projectional.svg.event.SvgAttributeEvent;
import jetbrains.jetpad.projectional.svg.event.SvgEventSpec;

public class SvgContainer {
  private Property<SvgSvgElement> mySvgRoot = new ValueProperty<SvgSvgElement>() {
    @Override
    public void set(SvgSvgElement value) {
      if (mySvgRoot.get() != null) {
        mySvgRoot.get().detach();
      }
      super.set(value);
      mySvgRoot.get().attach(SvgContainer.this);
    }
  };
  private Listeners<SvgContainerListener> myListeners = new Listeners<>();

  public SvgContainer(SvgSvgElement root) {
    mySvgRoot.set(root);
  }

  public Property<SvgSvgElement> root() {
    return mySvgRoot;
  }

  public Registration addListener(SvgContainerListener l) {
    return myListeners.add(l);
  }

  void attributeChanged(final SvgElement element, final SvgAttributeEvent<?> event) {
    myListeners.fire(new ListenerCaller<SvgContainerListener>() {
      @Override
      public void call(SvgContainerListener l) {
        l.onAttrSet(element, event);
      }
    });
  }

  void svgNodeAttached(final SvgNode node) {
    myListeners.fire(new ListenerCaller<SvgContainerListener>() {
      @Override
      public void call(SvgContainerListener l) {
        l.onNodeAttached(node);
      }
    });
  }

  void svgNodeDetached(final SvgNode node) {
    myListeners.fire(new ListenerCaller<SvgContainerListener>() {
      @Override
      public void call(SvgContainerListener l) {
        l.onNodeDetached(node);
      }
    });
  }

  public void mousePressed(MouseEvent e) {
    dispatchMouseEvent(SvgEventSpec.MOUSE_PRESSED, e);
  }

  public void mouseReleased(MouseEvent e) {
    dispatchMouseEvent(SvgEventSpec.MOUSE_RELEASED, e);
  }

  public void keyPressed(KeyEvent e) {
    dispatchKeyboardEvent(SvgEventSpec.KEY_PRESSED, e);
  }

  public void keyReleased(KeyEvent e) {
    dispatchKeyboardEvent(SvgEventSpec.KEY_RELEASED, e);
  }

  public void keyTyped(KeyEvent e) {
    dispatchKeyboardEvent(SvgEventSpec.KEY_TYPED, e);
  }

  private void dispatchMouseEvent(SvgEventSpec spec, MouseEvent e) {
//    mySvgRoot.get().dispatch(spec, e);
  }

  private void dispatchKeyboardEvent(SvgEventSpec spec, KeyEvent e) {
    mySvgRoot.get().dispatch(spec, e);
  }
}
