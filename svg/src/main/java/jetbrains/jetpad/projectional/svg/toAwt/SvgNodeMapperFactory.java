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
import org.apache.batik.dom.svg.*;
import org.w3c.dom.Node;

class SvgNodeMapperFactory implements MapperFactory<SvgNode, Node> {
  private AbstractDocument myDoc;
  private SvgAwtPeer myPeer;

  SvgNodeMapperFactory(AbstractDocument doc, SvgAwtPeer peer) {
    myDoc = doc;
    myPeer = peer;
  }

  @Override
  public Mapper<? extends SvgNode, ? extends Node> createMapper(SvgNode source) {
    Mapper<? extends SvgNode, ? extends Node> result;
    if (source instanceof SvgEllipseElement) {
      result = new SvgElementMapper<>((SvgEllipseElement) source, new SVGOMEllipseElement(null, myDoc), myDoc, myPeer);
    } else if (source instanceof SvgCircleElement) {
      result = new SvgElementMapper<>((SvgCircleElement) source, new SVGOMCircleElement(null, myDoc), myDoc, myPeer);
    } else if (source instanceof SvgRectElement) {
      result = new SvgElementMapper<>((SvgRectElement) source, new SVGOMRectElement(null, myDoc), myDoc, myPeer);
    } else if (source instanceof SvgTextElement) {
      result = new SvgElementMapper<>((SvgTextElement) source, new SVGOMTextElement(null, myDoc), myDoc, myPeer);
    } else if (source instanceof SvgPathElement) {
      result = new SvgElementMapper<>((SvgPathElement) source, new SVGOMPathElement(null, myDoc), myDoc, myPeer);
    } else if (source instanceof SvgLineElement) {
      result = new SvgElementMapper<>((SvgLineElement) source, new SVGOMLineElement(null, myDoc), myDoc, myPeer);
    } else if (source instanceof SvgSvgElement) {
      result = new SvgElementMapper<>((SvgSvgElement) source, new SVGOMSVGElement(null, myDoc), myDoc, myPeer);
    } else if (source instanceof SvgGElement) {
      result = new SvgElementMapper<>((SvgGElement) source, new SVGOMGElement(null, myDoc), myDoc, myPeer);
    } else if (source instanceof SvgStyleElement) {
      result = new SvgElementMapper<>((SvgStyleElement) source, new SVGOMStyleElement(null, myDoc), myDoc, myPeer);
    } else if (source instanceof SvgTextNode) {
      result = new SvgTextNodeMapper((SvgTextNode) source, myDoc.createTextNode(null), myDoc, myPeer);
    } else if (source instanceof SvgTSpanElement) {
      result = new SvgElementMapper<>((SvgTSpanElement) source, new SVGOMTSpanElement(null, myDoc), myDoc, myPeer);
    } else {
      throw new IllegalStateException("Unsupported SvgElement");
    }
    return result;
  }
}