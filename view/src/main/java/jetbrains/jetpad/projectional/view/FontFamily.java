package jetbrains.jetpad.projectional.view;

public class FontFamily {
  public static final FontFamily MONOSPACED = new MonospacedFontFamily();

  public static FontFamily forName(String name) {
    return new NamedFontFamily(name);
  }

  private FontFamily() {
  }

  static class NamedFontFamily extends FontFamily {
    private String myName;

    NamedFontFamily(String name) {
      myName = name;
    }
  }

  private static class MonospacedFontFamily extends FontFamily {
    @Override
    public String toString() {
      return "Monospaced";
    }
  }
}
