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
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.svg.SvgSvgElement;
import jetbrains.jetpad.projectional.svg.toDom.SvgRootDocumentMapper;
import jetbrains.jetpad.projectional.view.SvgView;
import org.vectomatic.dom.svg.OMSVGSVGElement;

public class SvgViewMapper extends BaseViewMapper<SvgView, Element> {
  private static Registration map(SvgSvgElement root, OMSVGSVGElement element) {
    final SvgRootDocumentMapper mapper = new SvgRootDocumentMapper(root, element);
    mapper.attachRoot();
    return new Registration() {
      @Override
      public void remove() {
        mapper.detachRoot();
      }
    };
  }

  private Registration myReg = Registration.EMPTY;

  public SvgViewMapper(ViewToDomContext ctx, SvgView source) {
    super(ctx, source, DOM.createDiv());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forPropsOneWay(getSource().root(), new WritableProperty<SvgSvgElement>() {
      @Override
      public void set(SvgSvgElement value) {
        OMSVGSVGElement element = new OMSVGSVGElement();

        getTarget().removeAllChildren();
        getTarget().appendChild(element.getElement());

        myReg.remove();
        myReg = map(value, element);
      }
    }));
  }

  @Override
  protected void onDetach() {
    super.onDetach();

    myReg.remove();
  }
}