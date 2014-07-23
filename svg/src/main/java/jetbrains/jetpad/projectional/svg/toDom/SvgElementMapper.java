package jetbrains.jetpad.projectional.svg.toDom;

import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.projectional.svg.SvgElement;
import jetbrains.jetpad.projectional.svg.SvgEllipse;
import jetbrains.jetpad.projectional.svg.SvgRoot;
import org.vectomatic.dom.svg.OMSVGElement;
import org.vectomatic.dom.svg.OMSVGEllipseElement;

public class SvgElementMapper<SourceT extends SvgElement, TargetT extends OMSVGElement> extends Mapper<SourceT, TargetT> {
  public SvgElementMapper(SourceT source, TargetT target) {
    super(source, target);
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forObservableRole(this, getSource().elements, Utils.elementChildren(getTarget()), new MapperFactory<SvgElement, OMSVGElement>() {
      @Override
      public Mapper<? extends SvgElement, ? extends OMSVGElement> createMapper(SvgElement source) {
        Mapper<? extends SvgElement, ? extends OMSVGElement> result;
        if (source instanceof SvgEllipse) {
          result = new SvgEllipseMapper( (SvgEllipse) source, new OMSVGEllipseElement());
        } else if (source instanceof SvgRoot) {
          throw new IllegalStateException("Svg root can't be embedded inside svg");
        } else {
          throw new IllegalStateException("Unsupported SvgElement");
        }
        return result;
      }
    }));
  }
}
