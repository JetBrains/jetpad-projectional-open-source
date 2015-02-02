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
package jetbrains.jetpad.projectional.view.toGwt;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.mapper.gwt.DomUtil;
import jetbrains.jetpad.model.property.Properties;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.model.property.Selector;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.view.dom.DomView;

import static com.google.gwt.query.client.GQuery.$;

class DomViewMapper extends BaseViewMapper<DomView, Element> {
  DomViewMapper(ViewToDomContext ctx, DomView source) {
    super(ctx, source, DOM.createDiv());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    ReadableProperty<Vector> bounds = Properties.select(getSource().element, new Selector<Element, ReadableProperty<Vector>>() {
      @Override
      public ReadableProperty<Vector> select(Element source) {
        return DomUtil.dimension(source);
      }
    }, Vector.ZERO);

    conf.add(Synchronizers.forEventSource(bounds, new Runnable() {
      @Override
      public void run() {
        getSource().validate();
      }
    }));

    conf.add(Synchronizers.forPropsOneWay(getSource().element, new WritableProperty<Element>() {
      @Override
      public void set(Element value) {
        getTarget().setInnerHTML("");
        if (value != null) {
          getTarget().appendChild(value);
        }
      }
    }));

    conf.add(Synchronizers.forPropsOneWay(getSource().focused(), new WritableProperty<Boolean>() {
      @Override
      public void set(Boolean value) {
        if (value && getSource().element.get() != null) {
          $(getSource().element.get()).focus();
        }
      }
    }));
  }
}