package jetbrains.jetpad.projectional.view.animation;

public interface Animation {
  void stop();

  void whenDone(Runnable r);
}
