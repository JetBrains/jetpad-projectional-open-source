package jetbrains.jetpad.projectional.svg.toDom;

import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.projectional.svg.SvgElement;
import jetbrains.jetpad.projectional.svg.SvgEllipse;
import jetbrains.jetpad.projectional.svg.SvgRect;
import jetbrains.jetpad.projectional.svg.SvgRoot;
import org.vectomatic.dom.svg.OMSVGElement;
import org.vectomatic.dom.svg.OMSVGEllipseElement;
import org.vectomatic.dom.svg.OMSVGRectElement;

public class SvgElementMapperFactory implements MapperFactory<SvgElement, OMSVGElement> {
  @Override
  public Mapper<? extends SvgElement, ? extends OMSVGElement> createMapper(SvgElement source) {
    Mapper<? extends SvgElement, ? extends OMSVGElement> result;
    if (source instanceof SvgEllipse) {
      result = new SvgEllipseMapper( (SvgEllipse) source, new OMSVGEllipseElement());
    } else if (source instanceof SvgRect) {
      result = new SvgRectMapper( (SvgRect) source, new OMSVGRectElement());
    } else if (source instanceof SvgRoot) {
      throw new IllegalStateException("Svg root element can't be embedded");
    } else {
      throw new IllegalStateException("Unsupported SvgElement");
    }
    return result;
  }
}
