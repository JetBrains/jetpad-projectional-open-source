package jetbrains.jetpad.cell.animation;

public interface Animation {
  void stop();
  void whenFinished(Runnable r);
  void whenStopped(Runnable r);
}
