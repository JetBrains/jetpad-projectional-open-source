package jetbrains.jetpad.cell.action;

public class Runnables {
  public static final Runnable EMPTY = new Runnable() {
    @Override
    public void run() {
    }
  };

  public static Runnable seq(final Runnable... actions) {
    return new Runnable() {
      @Override
      public void run() {
        for (Runnable a : actions) {
          a.run();
        }
      }
    };
  }
}
