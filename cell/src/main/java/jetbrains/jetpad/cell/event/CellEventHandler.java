package jetbrains.jetpad.cell.event;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.event.Event;

public interface CellEventHandler<EventT extends Event> {
  void handle(Cell cell, EventT e);
}
