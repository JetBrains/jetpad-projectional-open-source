package jetbrains.jetpad.cell.animation;

public interface Animation {
  void stop();

  void whenDone(Runnable r);
}
