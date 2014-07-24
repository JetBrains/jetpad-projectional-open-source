package jetbrains.jetpad.projectional.svg.toAwt;

import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.svg.SvgRoot;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.SVGOMSVGElement;

public class SvgRootMapper extends SvgElementMapper<SvgRoot, SVGOMSVGElement> {
  public SvgRootMapper(SvgRoot source, SVGOMSVGElement target, AbstractDocument doc) {
    super(source, target, doc);
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forPropsOneWay(getSource().height, new WritableProperty<Integer>() {
      @Override
      public void set(Integer value) {
        getTarget().setAttribute("height", Integer.toString(value));
      }
    }));
    conf.add(Synchronizers.forPropsOneWay(getSource().width, new WritableProperty<Integer>() {
      @Override
      public void set(Integer value) {
        getTarget().setAttribute("width", Integer.toString(value));
      }
    }));
  }
}
