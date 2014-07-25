package jetbrains.jetpad.projectional.svg.toAwt;

import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MappingContext;
import jetbrains.jetpad.projectional.svg.SvgRoot;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.svg.SVGOMSVGElement;
import org.w3c.dom.DOMImplementation;

public class SvgRootDocumentMapper extends Mapper<SvgRoot, SVGOMDocument> {
  public static SVGOMDocument createDoc() {
    DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
    String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
    return (SVGOMDocument) impl.createDocument(svgNS, "svg", null);
  }

  public SvgRootDocumentMapper(SvgRoot source, SVGOMDocument target) {
    super(source, target);
  }

  @Override
  protected void onAttach(MappingContext ctx) {
    super.onAttach(ctx);

    SvgRootMapper elementMapper = new SvgRootMapper(getSource(), (SVGOMSVGElement) getTarget().getDocumentElement(), getTarget());
    elementMapper.attachRoot();
  }
}
