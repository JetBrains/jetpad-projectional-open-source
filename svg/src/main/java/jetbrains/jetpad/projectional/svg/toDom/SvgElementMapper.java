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

import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.svg.SvgElement;
import jetbrains.jetpad.projectional.svg.SvgEventHandler;
import jetbrains.jetpad.projectional.svg.SvgEvents;
import jetbrains.jetpad.projectional.svg.SvgTraitBuilder;
import jetbrains.jetpad.projectional.svg.event.SvgAttributeEvent;
import org.vectomatic.dom.svg.OMSVGElement;

public class SvgElementMapper<SourceT extends SvgElement, TargetT extends OMSVGElement> extends Mapper<SourceT, TargetT> {
  public SvgElementMapper(SourceT source, TargetT target) {
    super(source, target);
  }

  @Override
  protected void registerSynchronizers(final SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    // FIXME: O(n^2) time
    for (final String key : getSource().getAttributesKeys()) {
      conf.add(Synchronizers.forPropsOneWay(getSource().getAttr(key), new WritableProperty<String>() {
        @Override
        public void set(String value) {
          getTarget().setAttribute(key, value);
        }
      }));
    }

    getSource().addTrait(new SvgTraitBuilder().on(SvgEvents.ATTRIBUTE_CHANGED, new SvgEventHandler<SvgAttributeEvent>() {
      @Override
      public void handle(SvgElement element, final SvgAttributeEvent e) {
        if (e.getOldValue() == null) {
          conf.add(Synchronizers.forPropsOneWay(element.getAttr(e.getAttrName()), new WritableProperty<String>() {
            @Override
            public void set(String value) {
              getTarget().setAttribute(e.getAttrName(), value);
            }
          }));
        }
        getTarget().setAttribute(e.getAttrName(), e.getNewValue());
      }
    })
    .build());

    conf.add(Synchronizers.forObservableRole(this, getSource().children(), Utils.elementChildren(getTarget()), new SvgElementMapperFactory()));
  }
}
