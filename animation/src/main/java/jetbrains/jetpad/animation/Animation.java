package jetbrains.jetpad.animation;

public interface Animation {
  void stop();

  void whenDone(Runnable r);
}
