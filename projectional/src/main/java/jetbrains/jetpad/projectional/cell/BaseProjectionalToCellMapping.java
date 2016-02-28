package jetbrains.jetpad.projectional.cell;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.Synchronizer;
import jetbrains.jetpad.mapper.SynchronizerContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class BaseProjectionalToCellMapping implements ToCellMapping<Object>, Synchronizer {
  private ProjectionalRoleSynchronizer<?, ?> mySynchronizer;

  BaseProjectionalToCellMapping(ProjectionalRoleSynchronizer<?, ?> synchronizer) {
    mySynchronizer = synchronizer;
  }

  @Override
  public Object getSource(Cell cell) {
    for (Mapper<?, ? extends Cell> mapper : mySynchronizer.getMappers()) {
      Object source = CellProviders.doGetSource(mapper, cell);
      if (source != null) return source;
    }
    return null;
  }

  @Override
  public List<Cell> getCells(Object source) {
    List<Cell> result = null;
    for (Mapper<?, ? extends Cell> mapper : mySynchronizer.getMappers()) {
      List<Cell> cells = CellProviders.doGetCells(mapper, source);
      if (!cells.isEmpty()) {
        if (result == null) {
          result = new ArrayList<>(cells.size());
        }
        result.addAll(cells);
      }
    }
    return result == null ? Collections.<Cell>emptyList() : result;
  }

  @Override
  public void attach(SynchronizerContext ctx) {
    mySynchronizer.attach(ctx);
  }

  @Override
  public void detach() {
    mySynchronizer.detach();
  }
}
