package jetbrains.jetpad.projectional.util;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.completion.Completion;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

public class AutoPopupController {
  public static Registration install(final CellContainer container) {
    return container.focusedCell.addHandler(new EventHandler<PropertyChangeEvent<Cell>>() {
      private int eventCount;

      @Override
      public void onEvent(PropertyChangeEvent<Cell> event) {
        eventCount++;
        final Cell newFocus = event.getNewValue();
        final int currentEvent = eventCount;
        if (newFocus instanceof TextCell) {
          container.getEdt().schedule(1500, new Runnable() {
            @Override
            public void run() {
              if (eventCount == currentEvent && newFocus.get(Completion.COMPLETION_CONTROLLER) != null && !newFocus.get(Completion.COMPLETION_CONTROLLER).isActive()) {
                newFocus.get(Completion.COMPLETION_CONTROLLER).activate();
              }
            }
          });
        }
      }
    });
  }
}
