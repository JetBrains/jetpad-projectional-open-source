package jetbrains.jetpad.animation;

import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.base.SimpleAsync;

public abstract class DefaultAnimation implements Animation {
  private SimpleAsync<Object> myWhenDone = new SimpleAsync<>();
  private boolean myDone;

  protected abstract void doStop();

  public void done() {
    if (myDone) {
      throw new IllegalStateException();
    }
    myWhenDone.onSuccess(null);
    myDone = true;
  }

  @Override
  public void stop() {
    doStop();
    done();
  }


  @Override
  public void whenDone(final Runnable r) {
    myWhenDone.onSuccess(new Handler<Object>() {
      @Override
      public void handle(Object item) {
        r.run();
      }
    });
  }
}
