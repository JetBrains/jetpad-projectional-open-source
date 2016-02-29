package jetbrains.jetpad.hybrid;

import jetbrains.jetpad.base.Handler;

public class HybridEditorBehavior<SourceT> {
  public static <SourceT> HybridEditorBehavior<SourceT> simple() {
    return new HybridEditorBehavior<>(true, true, new Handler<HybridSynchronizer<SourceT>>() {
      @Override
      public void handle(HybridSynchronizer<SourceT> item) { }
    });
  }

  public final boolean reprintOnChange;
  public final boolean reparseOnChange;
  public final Handler<HybridSynchronizer<SourceT>> attachHandler;

  public HybridEditorBehavior(boolean reprintOnChange, boolean reparseOnChange, Handler<HybridSynchronizer<SourceT>> attachHandler) {
    this.reprintOnChange = reprintOnChange;
    this.reparseOnChange = reparseOnChange;
    this.attachHandler = attachHandler;
  }
}
