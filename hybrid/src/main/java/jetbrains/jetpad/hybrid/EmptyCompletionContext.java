package jetbrains.jetpad.hybrid;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.mapper.Mapper;

import java.util.ArrayList;
import java.util.List;

class EmptyCompletionContext implements CompletionContext {
  @Override
  public int getTargetIndex() {
    return 0;
  }

  @Override
  public List<Token> getPrefix() {
    return new ArrayList<>();
  }

  @Override
  public List<Cell> getViews() {
    return new ArrayList<>();
  }

  @Override
  public List<Token> getTokens() {
    return new ArrayList<>();
  }

  @Override
  public List<Object> getObjects() {
    return new ArrayList<>();
  }

  @Override
  public Mapper<?, ?> getContextMapper() {
    return null;
  }

  @Override
  public Object getTarget() {
    return null;
  }
}
