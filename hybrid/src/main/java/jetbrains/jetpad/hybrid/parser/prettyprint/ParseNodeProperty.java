package jetbrains.jetpad.hybrid.parser.prettyprint;

public class ParseNodeProperty<ValueT> {
  private String myName;
  private ValueT myValue;

  public ParseNodeProperty(String name) {
    this(name, null);
  }

  public ParseNodeProperty(String name, ValueT value) {
    myName = name;
    myValue = value;
  }

  public ValueT getDefaultValue() {
    return myValue;
  }

  @Override
  public String toString() {
    return myName;
  }
}
