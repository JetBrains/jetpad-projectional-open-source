package jetbrains.jetpad.cell.trait;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.cell.event.CellEventHandler;
import jetbrains.jetpad.cell.event.CellEventSpec;
import jetbrains.jetpad.event.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CellTraitBuilder {
  private CellTrait myParent;
  private Map<CellPropertySpec<?>, Object> myProperties = new HashMap<>();
  private Map<CellEventSpec<?>, List<CellEventHandler>> myHandlers = new HashMap<>();

  public CellTraitBuilder() {
    this(null);
  }

  public CellTraitBuilder(CellTrait parent) {
    myParent = parent;
  }

  public <ValueT> CellTraitBuilder set(CellPropertySpec<ValueT> prop, ValueT value) {
    if (Cell.isPopupProp(prop)) {
      throw new IllegalArgumentException("You can't set popup props in traits");
    }

    myProperties.put(prop, value);
    return this;
  }

  public <EventT extends Event> CellTraitBuilder on(CellEventSpec<EventT> event, CellEventHandler<EventT> handler) {
    if (!myHandlers.containsKey(event)) {
      myHandlers.put(event, new ArrayList<CellEventHandler>());
    }
    myHandlers.get(event).add(handler);
    return this;
  }

  public CellTrait build() {
    return new CellTrait(myParent, myProperties, myHandlers);
  }
}