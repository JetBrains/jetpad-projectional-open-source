package jetbrains.jetpad.projectional.svg;

public class SvgPropertySpec<ValueT> {
  private String myName;
  private ValueT myDefaultValue;

  public SvgPropertySpec(String name, ValueT defaultValue) {
    myName = name;
    myDefaultValue = defaultValue;
  }

  public SvgPropertySpec(String name) {
    this(name, null);
  }

  public ValueT defaultValue() {
    return myDefaultValue;
  }

  @Override
  public String toString() {
    return myName;
  }
}
