package jetbrains.jetpad.projectional.svg;

import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.values.Color;

public class SvgEllipse extends SvgElement {
  public final Property<Double> cx = new ValueProperty<>(0.0);
  public final Property<Double> cy = new ValueProperty<>(0.0);
  public final Property<Double> rx = new ValueProperty<>(10.0);
  public final Property<Double> ry = new ValueProperty<>(20.0);
  public final Property<Color> fill = new ValueProperty<>(Color.BLACK);
}
