package jetbrains.jetpad.projectional.svg.toAwt;

import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.projectional.svg.SvgElement;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.SVGOMElement;

public class SvgElementMapper<SourceT extends SvgElement, TargetT extends SVGOMElement> extends Mapper<SourceT, TargetT> {
  private AbstractDocument myDoc;

  public SvgElementMapper(SourceT source, TargetT target, AbstractDocument doc) {
    super(source, target);
    myDoc = doc;
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forObservableRole(this, getSource().children(), Utils.elementChildren(getTarget()), new SvgElementMappingFactory(myDoc)));
  }
}
