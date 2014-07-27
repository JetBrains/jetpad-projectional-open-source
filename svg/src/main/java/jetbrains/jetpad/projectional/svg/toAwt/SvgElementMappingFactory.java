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
