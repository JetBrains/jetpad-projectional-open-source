package jetbrains.jetpad.projectional.svg;

import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.values.Color;

public class SvgRect extends SvgElement {
  public final Property<Double> x = new ValueProperty<>(0.0);
  public final Property<Double> y = new ValueProperty<>(0.0);
  public final Property<Double> height = new ValueProperty<>(50.0);
  public final Property<Double> width = new ValueProperty<>(100.0);
  public final Property<Color> fill = new ValueProperty<>(Color.BLACK);
}
