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

import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.projectional.svg.SvgElement;
import jetbrains.jetpad.projectional.svg.SvgEllipse;
import jetbrains.jetpad.projectional.svg.SvgRect;
import jetbrains.jetpad.projectional.svg.SvgRoot;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.SVGOMElement;
import org.apache.batik.dom.svg.SVGOMEllipseElement;
import org.apache.batik.dom.svg.SVGOMRectElement;

public class SvgElementMappingFactory implements MapperFactory<SvgElement, SVGOMElement> {
  private AbstractDocument myDoc;

  public SvgElementMappingFactory(AbstractDocument doc) {
    myDoc = doc;
  }

  @Override
  public Mapper<? extends SvgElement, ? extends SVGOMElement> createMapper(SvgElement source) {
    Mapper<? extends SvgElement, ? extends SVGOMElement> result;
    if (source instanceof SvgEllipse) {
      result = new SvgEllipseMapper( (SvgEllipse) source, new SVGOMEllipseElement(null, myDoc), myDoc);
    } else if (source instanceof SvgRect) {
      result = new SvgRectMapper( (SvgRect) source, new SVGOMRectElement(null, myDoc), myDoc);
    } else if (source instanceof SvgRoot) {
      throw new IllegalStateException("Svg root element can't be embedded");
    } else {
      throw new IllegalStateException("Unsupported SvgElement");
    }
    return result;
  }
}
