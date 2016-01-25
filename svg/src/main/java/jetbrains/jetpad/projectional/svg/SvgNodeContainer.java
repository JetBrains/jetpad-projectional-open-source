/*
 * Copyright 2012-2016 JetBrains s.r.o
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
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.projectional.svg.event.SvgAttributeEvent;

public class SvgNodeContainer {
  private Property<SvgSvgElement> mySvgRoot = new ValueProperty<SvgSvgElement>() {
    @Override
    public void set(SvgSvgElement value) {
      if (mySvgRoot.get() != null) {
        mySvgRoot.get().detach();
      }
      super.set(value);
      mySvgRoot.get().attach(SvgNodeContainer.this);
    }
  };
  private Listeners<SvgNodeContainerListener> myListeners = new Listeners<>();
  private SvgPlatformPeer myPeer;

  public SvgNodeContainer(SvgSvgElement root) {
    mySvgRoot.set(root);
  }

  public Property<SvgSvgElement> root() {
    return mySvgRoot;
  }

  public void setPeer(SvgPlatformPeer peer) {
    myPeer = peer;
  }

  public SvgPlatformPeer getPeer() {
    return myPeer;
  }

  public Registration addListener(SvgNodeContainerListener l) {
    return myListeners.add(l);
  }

  void attributeChanged(final SvgElement element, final SvgAttributeEvent<?> event) {
    myListeners.fire(new ListenerCaller<SvgNodeContainerListener>() {
      @Override
      public void call(SvgNodeContainerListener l) {
        l.onAttributeSet(element, event);
      }
    });
  }

  void svgNodeAttached(final SvgNode node) {
    myListeners.fire(new ListenerCaller<SvgNodeContainerListener>() {
      @Override
      public void call(SvgNodeContainerListener l) {
        l.onNodeAttached(node);
      }
    });
  }

  void svgNodeDetached(final SvgNode node) {
    myListeners.fire(new ListenerCaller<SvgNodeContainerListener>() {
      @Override
      public void call(SvgNodeContainerListener l) {
        l.onNodeDetached(node);
      }
    });
  }
}