/*
 * Copyright 2012-2016 JetBrains s.r.o
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

import elemental.client.Browser;
import elemental.dom.Document;
import elemental.dom.Node;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.projectional.svg.*;

class SvgNodeMapperFactory implements MapperFactory<SvgNode, Node> {
  private SvgGwtPeer myPeer;

  public SvgNodeMapperFactory(SvgGwtPeer peer) {
    myPeer = peer;
  }

  @Override
  public Mapper<? extends SvgNode, ? extends Node> createMapper(SvgNode source) {
    Document doc = Browser.getDocument();

    Mapper<? extends SvgNode, ? extends Node> result;
    if (source instanceof SvgEllipseElement) {
      result = new SvgElementMapper<>((SvgEllipseElement) source, doc.createSVGEllipseElement(), myPeer);
    } else if (source instanceof SvgCircleElement) {
      result = new SvgElementMapper<>((SvgCircleElement) source, doc.createSVGCircleElement(), myPeer);
    } else if (source instanceof SvgRectElement) {
      result = new SvgElementMapper<>((SvgRectElement) source, doc.createSVGRectElement(), myPeer);
    } else if (source instanceof SvgTextElement) {
      result = new SvgElementMapper<>((SvgTextElement) source, doc.createSVGTextElement(), myPeer);
    } else if (source instanceof SvgPathElement) {
      result = new SvgElementMapper<>((SvgPathElement) source, doc.createSVGPathElement(), myPeer);
    } else if (source instanceof SvgLineElement) {
      result = new SvgElementMapper<>((SvgLineElement) source, doc.createSVGLineElement(), myPeer);
    } else if (source instanceof SvgSvgElement) {
      result = new SvgElementMapper<>((SvgSvgElement) source, doc.createSVGElement(), myPeer);
    } else if (source instanceof SvgGElement) {
      result = new SvgElementMapper<>((SvgGElement) source, doc.createSVGGElement(), myPeer);
    } else if (source instanceof SvgStyleElement) {
      result = new SvgElementMapper<>((SvgStyleElement) source, doc.createSVGStyleElement(), myPeer);
    } else if (source instanceof SvgTextNode) {
      result = new SvgTextNodeMapper((SvgTextNode) source, doc.createTextNode(null), myPeer);
    } else if (source instanceof SvgTSpanElement) {
      result = new SvgElementMapper<>((SvgTSpanElement) source, doc.createSVGTSpanElement(), myPeer);
    } else if (source instanceof SvgDefsElement) {
      result = new SvgElementMapper<>((SvgDefsElement) source, doc.createSVGDefsElement(), myPeer);
    } else if (source instanceof SvgClipPathElement) {
      result = new SvgElementMapper<>((SvgClipPathElement) source, doc.createSVGClipPathElement(), myPeer);
    } else {
      throw new IllegalStateException("Unsupported SvgNode");
    }
    return result;
  }
}