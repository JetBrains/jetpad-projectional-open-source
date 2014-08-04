package jetbrains.jetpad.projectional.svg;

import jetbrains.jetpad.values.Color;

public class SvgRect extends SvgElement {
  public static final SvgPropertySpec<Double> X = new SvgPropertySpec<>("x", 0.0);
  public static final SvgPropertySpec<Double> Y = new SvgPropertySpec<>("y", 0.0);
  public static final SvgPropertySpec<Double> HEIGHT = new SvgPropertySpec<>("height", 10.0);
  public static final SvgPropertySpec<Double> WIDTH = new SvgPropertySpec<>("width", 10.0);
  public static final SvgPropertySpec<Color> FILL = new SvgPropertySpec<>("fill", Color.BLACK);
}
