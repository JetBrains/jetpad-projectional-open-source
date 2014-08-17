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
package jetbrains.jetpad.projectional.svg.toDom;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.mapper.Synchronizer;
import jetbrains.jetpad.mapper.SynchronizerContext;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.model.property.WritableProperty;
import org.vectomatic.dom.svg.OMElement;
import org.vectomatic.dom.svg.OMNode;

import java.util.AbstractList;
import java.util.List;

public class Utils {
  public static List<OMNode> elementChildren(final OMNode e) {
    return new AbstractList<OMNode>() {
      @Override
      public OMNode get(int index) {
        return e.getChildNodes().getItem(index);
      }

      @Override
      public OMNode set(int index, OMNode element) {
        if (element.getParentNode() != null) {
          throw new IllegalStateException();
        }

        OMNode child = get(index);
        e.replaceChild(child, element);
        return child;
      }

      @Override
      public void add(int index, OMNode element) {
        if (element.getParentNode() != null) {
          throw new IllegalStateException();
        }

        if (index == size()) {
          e.insertBefore(element, null);
        } else {
          e.insertBefore(element, get(index));
        }
      }

      @Override
      public OMNode remove(int index) {
        OMNode child = get(index);
        e.removeChild(child);
        return child;
      }

      @Override
      public int size() {
        return e.getChildNodes().getLength();
      }
    };
  }

  public static <ValueT> Synchronizer attrSynchronizer(final ReadableProperty<ValueT> source, final WritableProperty<ValueT> target, boolean init) {
    if (init) {
      return Synchronizers.forPropsOneWay(source, target);
    }
    return new Synchronizer() {
      private Registration myReg;

      @Override
      public void attach(SynchronizerContext ctx) {
        myReg = source.addHandler(new EventHandler<PropertyChangeEvent<ValueT>>() {
          @Override
          public void onEvent(PropertyChangeEvent<ValueT> event) {
            target.set(event.getNewValue());
          }
        });
      }

      @Override
      public void detach() {
        myReg.remove();
      }
    };
  }
}
