package jetbrains.jetpad.cell.position;

class EmptyPositionHandler extends DefaultPositionHandler {
  @Override
  public boolean isHome() {
    return true;
  }

  @Override
  public boolean isEnd() {
    return true;
  }
}
