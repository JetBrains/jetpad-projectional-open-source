package jetbrains.jetpad.hybrid.parser.prettyprint;

public class TokenProperty<ValueT> {
  private String myName;
  private ValueT myDefaultValue;

  public TokenProperty(String name) {
    this(name, null);
  }

  public TokenProperty(String name, ValueT defaultValue) {
    myName = name;
    myDefaultValue = defaultValue;
  }

  public ValueT getDefaultValue() {
    return myDefaultValue;
  }

  @Override
  public String toString() {
    return myName;
  }
}
