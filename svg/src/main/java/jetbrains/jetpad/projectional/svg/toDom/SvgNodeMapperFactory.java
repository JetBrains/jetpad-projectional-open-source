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
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.projectional.svg.*;
import org.vectomatic.dom.svg.*;

class SvgNodeMapperFactory implements MapperFactory<SvgNode, OMNode> {
  @Override
  public Mapper<? extends SvgNode, ? extends OMNode> createMapper(SvgNode source) {
    Mapper<? extends SvgNode, ? extends OMNode> result;
    if (source instanceof SvgEllipseElement) {
      result = new SvgElementMapper<>((SvgEllipseElement) source, new OMSVGEllipseElement());
    } else if (source instanceof SvgCircleElement) {
      result = new SvgElementMapper<>((SvgCircleElement) source, new OMSVGCircleElement());
    } else if (source instanceof SvgRectElement) {
      result = new SvgElementMapper<>((SvgRectElement) source, new OMSVGRectElement());
    } else if (source instanceof SvgTextElement) {
      result = new SvgElementMapper<>((SvgTextElement) source, new OMSVGTextElement());
    } else if (source instanceof SvgPathElement) {
      result = new SvgElementMapper<>((SvgPathElement) source, new OMSVGPathElement());
    } else if (source instanceof SvgLineElement) {
      result = new SvgElementMapper<>((SvgLineElement) source, new OMSVGLineElement());
    } else if (source instanceof SvgSvgElement) {
      result = new SvgElementMapper<>((SvgSvgElement) source, new OMSVGSVGElement());
    } else if (source instanceof SvgGElement) {
      result = new SvgElementMapper<>((SvgGElement) source, new OMSVGGElement());
    } else if (source instanceof SvgStyleElement) {
      result = new SvgElementMapper<>((SvgStyleElement) source, new OMSVGStyleElement());
    } else if (source instanceof SvgTextNode) {
      result = new SvgTextNodeMapper((SvgTextNode) source, new OMText(null));
    } else {
      throw new IllegalStateException("Unsupported SvgNode");
    }
    return result;
  }
}