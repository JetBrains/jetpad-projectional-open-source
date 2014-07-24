package jetbrains.jetpad.projectional.svg;

import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;

public class SvgRoot extends SvgElement {
  public final Property<Double> height = new ValueProperty<>(100.0);
  public final Property<Double> width = new ValueProperty<>(100.0);
}
