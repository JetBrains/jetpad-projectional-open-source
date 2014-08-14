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
package jetbrains.jetpad.projectional.svg.toAwt;

import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.svg.SvgPathElement;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.SVGOMPathElement;

public class SvgPathElementMapper extends SvgElementMapper<SvgPathElement, SVGOMPathElement> {
  public SvgPathElementMapper(SvgPathElement source, SVGOMPathElement target, AbstractDocument doc) {
    super(source, target, doc);
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forPropsOneWay(getSource().getProp(SvgPathElement.D), new WritableProperty<String>() {
      @Override
      public void set(String value) {
        getTarget().setAttribute("d", value);
      }
    }));
  }
}
