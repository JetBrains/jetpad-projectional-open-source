package jetbrains.jetpad.projectional.svg;

import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.values.Color;

public class SvgRect extends SvgElement {
  public final Property<Integer> x = new ValueProperty<>(0);
  public final Property<Integer> y = new ValueProperty<>(0);
  public final Property<Integer> height = new ValueProperty<>(50);
  public final Property<Integer> width = new ValueProperty<>(100);
  public final Property<Color> fill = new ValueProperty<>(Color.BLACK);
}
