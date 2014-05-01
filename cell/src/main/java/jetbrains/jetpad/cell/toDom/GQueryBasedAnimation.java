package jetbrains.jetpad.cell.toDom;

import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import jetbrains.jetpad.cell.animation.DefaultAnimation;

abstract class GQueryBasedAnimation extends DefaultAnimation {
  private GQuery myAnimation;

  protected GQueryBasedAnimation() {
    myAnimation = createAnimation(new Function() {
      @Override
      public void f() {
        done();
      }
    });
  }

  protected abstract GQuery createAnimation(Function callback);

  @Override
  protected void doStop() {
    myAnimation.stop(true, true);
  }
}
