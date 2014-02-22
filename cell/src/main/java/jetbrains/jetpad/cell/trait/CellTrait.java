package jetbrains.jetpad.cell.trait;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.cell.event.CellEventHandler;
import jetbrains.jetpad.cell.event.CellEventSpec;
import jetbrains.jetpad.event.Event;
import jetbrains.jetpad.model.util.ListMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CellTrait {
  private CellTrait myParent;
  private ListMap<CellPropertySpec<?>, Object> myProperties;
  private ListMap<CellEventSpec<?>, List<CellEventHandler>> myHandlers;

  CellTrait(CellTrait parent, Map<CellPropertySpec<?>, Object> props, Map<CellEventSpec<?>, List<CellEventHandler>> handlers) {
    myParent = parent;
    for (Map.Entry<CellPropertySpec<?>, Object> entry : props.entrySet()) {
      if (myProperties == null) {
        myProperties = new ListMap<>();
      }
      myProperties.put(entry.getKey(), entry.getValue());
    }

    for (Map.Entry<CellEventSpec<?>, List<CellEventHandler>> entry : handlers.entrySet()) {
      if (myHandlers == null) {
        myHandlers = new ListMap<>();
      }
      myHandlers.put(entry.getKey(), entry.getValue());
    }
  }

  public CellTrait parent() {
    return myParent;
  }

  public Set<CellPropertySpec<?>> properties() {
    if (myProperties == null) return Collections.emptySet();
    return Collections.unmodifiableSet(myProperties.keySet());
  }

  public boolean hasValue(CellPropertySpec<?> prop) {
    if (myProperties == null) return false;
    return myProperties.containsKey(prop);
  }

  public <ValueT> ValueT get(CellPropertySpec<ValueT> prop) {
    return (ValueT) myProperties.get(prop);
  }

  public <EventT extends Event> void dispatch(Cell cell, CellEventSpec<EventT> spec, EventT event) {
    if (myHandlers != null && myHandlers.containsKey(spec)) {
      for (CellEventHandler handler : myHandlers.get(spec)) {
        handler.handle(cell, event);
        if (event.isConsumed()) return;
      }
    }
  }


}
