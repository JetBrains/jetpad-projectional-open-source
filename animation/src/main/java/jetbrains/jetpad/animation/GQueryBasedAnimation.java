package jetbrains.jetpad.animation;

import com.google.gwt.query.client.GQuery;

abstract class GQueryBasedAnimation extends DefaultAnimation {
  private GQuery myAnimation;

  protected GQueryBasedAnimation() {
    myAnimation = createAnimation(new Runnable() {
      @Override
      public void run() {
        done();
      }
    });
  }

  protected abstract GQuery createAnimation(Runnable callback);

  @Override
  protected void doStop() {
    myAnimation.stop(true, true);
  }
}
