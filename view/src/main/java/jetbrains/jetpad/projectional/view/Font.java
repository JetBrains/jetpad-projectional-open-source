package jetbrains.jetpad.projectional.view;

public class Font {
  private FontFamily myFamily;
  private int mySize;
  private boolean myBold;
  private boolean myItalic;

  public Font(FontFamily family, int size) {
    this(family, size, false, false);
  }

  public Font(FontFamily family, int size, boolean bold, boolean italic) {
    myFamily = family;
    mySize = size;
    myBold = bold;
    myItalic = italic;
  }

  public FontFamily getFamily() {
    return myFamily;
  }

  public int getSize() {
    return mySize;
  }

  public boolean isBold() {
    return myBold;
  }

  public boolean isItalic() {
    return myItalic;
  }
}
