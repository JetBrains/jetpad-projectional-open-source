package jetbrains.jetpad.cell.animation;

import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.base.SimpleAsync;

public abstract class DefaultAnimation implements Animation {
  private SimpleAsync<Object> myFinish = new SimpleAsync<>();
  private SimpleAsync<Object> myStop = new SimpleAsync<>();
  private boolean myFinished;
  private boolean myStopped;

  protected abstract void doStop();

  @Override
  public void stop() {
    if (myFinished || myStopped) {
      throw new IllegalStateException();
    }
    doStop();
    myStop.success(null);
    myStopped = true;
  }

  public void finish() {
    if (myFinished || myStopped) {
      throw new IllegalStateException();
    }
    myFinish.success(null);
    myFinished = true;
  }

  @Override
  public void whenFinished(final Runnable r) {
    myFinish.onSuccess(new Handler<Object>() {
      @Override
      public void handle(Object item) {
        r.run();
      }
    });
  }

  @Override
  public void whenStopped(final Runnable r) {
    myStop.onSuccess(new Handler<Object>() {
      @Override
      public void handle(Object item) {
        r.run();
      }
    });
  }
}
