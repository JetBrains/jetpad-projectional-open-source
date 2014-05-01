package jetbrains.jetpad.animation;

public class Animations {
  public static Animation finishedAnimation() {
    DefaultAnimation result = new DefaultAnimation() {
      @Override
      protected void doStop() {
      }
    };
    result.done();
    return result;
  }
}
