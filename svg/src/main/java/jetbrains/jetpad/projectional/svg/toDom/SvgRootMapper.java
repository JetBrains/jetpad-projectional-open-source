package jetbrains.jetpad.projectional.svg.toDom;

import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.svg.SvgRoot;
import org.vectomatic.dom.svg.OMSVGSVGElement;

public class SvgRootMapper extends SvgElementMapper<SvgRoot, OMSVGSVGElement> {
  public SvgRootMapper(SvgRoot source, OMSVGSVGElement target) {
    super(source, target);
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
