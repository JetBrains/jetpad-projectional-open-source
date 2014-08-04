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

    conf.add(Synchronizers.forPropsOneWay(getSource().getProp(SvgRoot.HEIGHT), new WritableProperty<Double>() {
      @Override
      public void set(Double value) {
        getTarget().setAttribute("height", Double.toString(value));
      }
    }));
    conf.add(Synchronizers.forPropsOneWay(getSource().getProp(SvgRoot.WIDTH), new WritableProperty<Double>() {
      @Override
      public void set(Double value) {
        getTarget().setAttribute("width", Double.toString(value));
      }
    }));
  }
}
