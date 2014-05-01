package jetbrains.jetpad.cell.toDom;

import com.google.gwt.query.client.GQuery;
import jetbrains.jetpad.cell.animation.DefaultAnimation;

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
