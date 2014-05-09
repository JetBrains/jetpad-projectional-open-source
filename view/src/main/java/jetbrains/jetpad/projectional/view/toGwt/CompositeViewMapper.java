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

import com.google.common.base.Function;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.mapper.gwt.DomUtil;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.property.DerivedProperty;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.model.transform.Transformer;
import jetbrains.jetpad.model.transform.Transformers;
import jetbrains.jetpad.projectional.view.View;

import java.util.List;

class CompositeViewMapper<ViewT extends View, ElementT extends Element> extends BaseViewMapper<ViewT, ElementT> {
  CompositeViewMapper(ViewToDomContext ctx, ViewT source, ElementT target) {
    super(ctx, source, target);
  }

  protected boolean clipChildren() {
    return false;
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    List<Node> nodes = DomUtil.elementChildren(getTarget());
    if (clipChildren()) {
      Transformer<ObservableList<View>, ObservableList<View>> transformer = Transformers.listFilter(new Function<View, ReadableProperty<Boolean>>() {
        @Override
        public ReadableProperty<Boolean> apply(final View input) {
          return new DerivedProperty<Boolean>(input.bounds(), context().visibleArea()) {
            @Override
            public Boolean get() {
              return input.bounds().get().intersects(context().visibleArea().get());
            }
          };
        }
      });
      conf.add(Synchronizers.<ObservableList<View>, View, Node, Element>forObservableRole(
        this,
        getSource().children(),
        transformer,
        nodes,
        context().getFactory()));
    } else {
      conf.add(Synchronizers.forObservableRole(this, getSource().children(), nodes, context().getFactory()));
    }
  }
}