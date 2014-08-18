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
import jetbrains.jetpad.projectional.svg.*;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.SVGOMEllipseElement;
import org.apache.batik.dom.svg.SVGOMPathElement;
import org.apache.batik.dom.svg.SVGOMRectElement;
import org.apache.batik.dom.svg.SVGOMTextElement;
import org.w3c.dom.Node;

public class SvgNodeMapperFactory implements MapperFactory<SvgNode, Node> {
  private AbstractDocument myDoc;

  public SvgNodeMapperFactory(AbstractDocument doc) {
    myDoc = doc;
  }

  @Override
  public Mapper<? extends SvgNode, ? extends Node> createMapper(SvgNode source) {
    Mapper<? extends SvgNode, ? extends Node> result;
    if (source instanceof SvgEllipseElement) {
      result = new SvgEllipseElementMapper( (SvgEllipseElement) source, new SVGOMEllipseElement(null, myDoc), myDoc);
    } else if (source instanceof SvgRectElement) {
      result = new SvgRectElementMapper( (SvgRectElement) source, new SVGOMRectElement(null, myDoc), myDoc);
    } else if (source instanceof SvgTextElement) {
      result = new SvgTextElementMapper((SvgTextElement) source, new SVGOMTextElement(null, myDoc), myDoc);
    } else if (source instanceof SvgTextNode) {
      result = new SvgTextNodeMapper((SvgTextNode) source, myDoc.createTextNode(null), myDoc);
    } else if (source instanceof SvgPathElement) {
      result = new SvgPathElementMapper((SvgPathElement) source, new SVGOMPathElement(null, myDoc), myDoc);
    } else if (source instanceof SvgSvgElement) {
      throw new IllegalStateException("Svg root element can't be embedded");
    } else {
      throw new IllegalStateException("Unsupported SvgElement");
    }
    return result;
  }
}
