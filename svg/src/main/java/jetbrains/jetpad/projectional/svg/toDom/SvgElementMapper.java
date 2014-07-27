package jetbrains.jetpad.projectional.svg.toDom;

import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.projectional.svg.SvgElement;
import org.vectomatic.dom.svg.OMSVGElement;

public class SvgElementMapper<SourceT extends SvgElement, TargetT extends OMSVGElement> extends Mapper<SourceT, TargetT> {
  public SvgElementMapper(SourceT source, TargetT target) {
    super(source, target);
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forObservableRole(this, getSource().elements, Utils.elementChildren(getTarget()), new SvgElementMapperFactory()));
  }
}
