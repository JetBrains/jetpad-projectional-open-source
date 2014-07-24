package jetbrains.jetpad.projectional.svg.toDom;

import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.svg.SvgRect;
import jetbrains.jetpad.values.Color;
import org.vectomatic.dom.svg.OMSVGRectElement;

public class SvgRectMapper extends SvgElementMapper<SvgRect, OMSVGRectElement> {
  public SvgRectMapper(SvgRect source, OMSVGRectElement target) {
    super(source, target);
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forPropsOneWay(getSource().x, new WritableProperty<Double>() {
      @Override
      public void set(Double value) {
        getTarget().setAttribute("x", Double.toString(value));
      }
    }));
    conf.add(Synchronizers.forPropsOneWay(getSource().y, new WritableProperty<Double>() {
      @Override
      public void set(Double value) {
        getTarget().setAttribute("y", Double.toString(value));
      }
    }));
    conf.add(Synchronizers.forPropsOneWay(getSource().height, new WritableProperty<Double>() {
      @Override
      public void set(Double value) {
        getTarget().setAttribute("height", Double.toString(value));
      }
    }));
    conf.add(Synchronizers.forPropsOneWay(getSource().width, new WritableProperty<Double>() {
      @Override
      public void set(Double value) {
        getTarget().setAttribute("width", Double.toString(value));
      }
    }));
    conf.add(Synchronizers.forPropsOneWay(getSource().fill, new WritableProperty<Color>() {
      @Override
      public void set(Color value) {
        getTarget().setAttribute("fill", value.toCssColor());
      }
    }));
  }
}
