package jetbrains.jetpad.projectional.svg;

import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.values.Color;

public class SvgEllipse extends SvgElement {
  public final Property<Integer> cx = new ValueProperty<>(0);
  public final Property<Integer> cy = new ValueProperty<>(0);
  public final Property<Integer> rx = new ValueProperty<>(10);
  public final Property<Integer> ry = new ValueProperty<>(20);
  public final Property<Color> fill = new ValueProperty<>(Color.BLACK);
}
