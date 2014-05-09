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
package jetbrains.jetpad.projectional.view.toGwt;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.mapper.gwt.DomUtil;
import jetbrains.jetpad.projectional.view.HorizontalView;
import jetbrains.jetpad.projectional.view.View;

class HorizontalViewMapper extends BaseViewMapper<HorizontalView, Element> {
  private Element myContainer;

  HorizontalViewMapper(ViewToDomContext ctx, HorizontalView source) {
    super(ctx, source, DOM.createDiv());
    myContainer = DOM.createDiv();
    getTarget().appendChild(myContainer);

    Element clear = DOM.createDiv();
    clear.getStyle().setClear(Style.Clear.BOTH);
    getTarget().appendChild(clear);
  }

  @Override
  protected boolean isDomLayout() {
    return true;
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forObservableRole(this, getSource().children(), DomUtil.elementChildren(myContainer), new MapperFactory<View, Node>() {
      @Override
      public Mapper<? extends View, ? extends Node> createMapper(View source) {
        Mapper<? extends View, ? extends Element> result = context().getFactory().createMapper(source);
        result.getTarget().getStyle().setFloat(Style.Float.LEFT);
        return result;
      }
    }));
  }
}