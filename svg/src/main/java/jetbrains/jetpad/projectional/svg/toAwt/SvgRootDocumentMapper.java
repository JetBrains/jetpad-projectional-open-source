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
import jetbrains.jetpad.mapper.MappingContext;
import jetbrains.jetpad.projectional.svg.SvgSvgElement;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.svg.SVGOMSVGElement;
import org.w3c.dom.DOMImplementation;

public class SvgRootDocumentMapper extends Mapper<SvgSvgElement, SVGOMDocument> {
  private static SVGOMDocument createDocument() {
    DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
    String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
    return (SVGOMDocument) impl.createDocument(svgNS, "svg", null);
  }

  private SvgElementMapper<SvgSvgElement, SVGOMSVGElement> myElementMapper;

  public SvgRootDocumentMapper(SvgSvgElement source) {
    super(source, createDocument());
  }

  @Override
  protected void onAttach(MappingContext ctx) {
    super.onAttach(ctx);

    myElementMapper = new SvgElementMapper<>(getSource(), (SVGOMSVGElement) getTarget().getDocumentElement(), getTarget());
    getTarget().getDocumentElement().setAttribute("shape-rendering", "geometricPrecision");
    myElementMapper.attachRoot();
  }

  @Override
  protected void onDetach() {
    super.onDetach();

    myElementMapper.detachRoot();
    myElementMapper = null;
  }
}