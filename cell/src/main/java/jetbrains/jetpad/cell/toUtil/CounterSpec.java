package jetbrains.jetpad.cell.toUtil;

public class CounterSpec {
  private String myName;

  public CounterSpec(String name) {
    myName = name;
  }

  @Override
  public int hashCode() {
    return myName.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof CounterSpec)) {
      return false;
    }
    return myName.equals(((CounterSpec) obj).myName);
  }
}
