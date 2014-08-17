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
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.svg.SvgAttrSpec;
import jetbrains.jetpad.projectional.svg.SvgElement;
import jetbrains.jetpad.projectional.svg.event.SvgAttributeEvent;
import org.vectomatic.dom.svg.OMSVGElement;

public class SvgElementMapper<SourceT extends SvgElement, TargetT extends OMSVGElement> extends SvgNodeMapper<SourceT, TargetT> {
  public SvgElementMapper(SourceT source, TargetT target) {
    super(source, target);
  }

  @Override
  protected void registerSynchronizers(final SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    for (String key : getSource().attrKeys()) {
      final SvgAttrSpec spec = getSource().getSpecByName(key);
      conf.add(Utils.attrSynchronizer(getSource().getProp(spec), new WritableProperty<Object>() {
        @Override
        public void set(Object value) {
          getTarget().setAttribute(spec.toString(), value.toString());
        }
      }, getSource().propertyPresent(spec)));
    }

    conf.add(new Synchronizer() {
      private Registration myReg;
      @Override
      public void attach(SynchronizerContext ctx) {
        // FIXME: O(n^2) time
        for (String key : getSource().getPresentXmlAttributesKeys()) {
          getTarget().setAttribute(key, getSource().getXmlAttr(key).get());
        }

        myReg = getSource().xmlAttributes().addHandler(new EventHandler<SvgAttributeEvent>() {
          @Override
          public void onEvent(SvgAttributeEvent event) {
            getTarget().setAttribute(event.getAttrName(), event.getNewValue());
          }
        });
      }

      @Override
      public void detach() {
        myReg.remove();
      }
    });
  }
}
