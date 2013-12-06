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
package jetbrains.jetpad.projectional.view.gwt;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.DerivedProperty;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.view.VerticalView;

class VerticalViewMapper extends CompositeViewMapper<VerticalView, Element> {
  VerticalViewMapper(View2DomContext ctx, VerticalView source) {
    super(ctx, source, DOM.createDiv());
  }

  @Override
  protected boolean isDomLayout() {
    return true;
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forProperty(new DerivedProperty<Boolean>(getSource().bounds(), context().visibleArea()) {
        @Override
        public Boolean get() {
          return getSource().bounds().get().intersects(context().visibleArea().get());
        }
      },
      new WritableProperty<Boolean>() {
        @Override
        public void set(Boolean value) {
          getTarget().getStyle().setVisibility(value ? Style.Visibility.VISIBLE : Style.Visibility.HIDDEN);
        }
      }
    ));

    conf.add(Synchronizers.forProperty(getSource().indentWidth(), new WritableProperty<Integer>() {
      @Override
      public void set(Integer value) {
        getTarget().getStyle().setPaddingLeft(value, Style.Unit.PX);
      }
    }));
  }
}