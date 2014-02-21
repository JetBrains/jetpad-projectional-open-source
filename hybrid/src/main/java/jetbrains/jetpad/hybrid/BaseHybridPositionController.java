package jetbrains.jetpad.hybrid;

public abstract class BaseHybridPositionController<SourceT> implements HybridPositionController<SourceT> {
  private HybridSynchronizer<SourceT> mySync;

  protected HybridSynchronizer<SourceT> sync() {
    return mySync;
  }

  @Override
  public void attach(HybridSynchronizer<SourceT> sync) {
    mySync = sync;
  }

  @Override
  public void detach() {
    mySync = null;
  }
}
