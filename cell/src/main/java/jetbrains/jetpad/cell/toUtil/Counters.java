package jetbrains.jetpad.cell.toUtil;

import jetbrains.jetpad.model.util.ListMap;

public class Counters {
  private ListMap<CounterSpec, Integer> myCounters;

  public int getCounter(CounterSpec spec) {
    if (myCounters == null || !myCounters.containsKey(spec)) return 0;
    return myCounters.get(spec);
  }

  public void changeCounter(CounterSpec spec, int delta) {
    if (delta == 0) return;
    int oldVal = getCounter(spec);
    int newVal = oldVal + delta;

    if (newVal != 0) {
      if (myCounters == null) {
        myCounters = new ListMap<>();
      }
      myCounters.put(spec, delta);
    } else {
      if (myCounters != null) {
        myCounters.remove(spec);
        if (myCounters.isEmpty()) {
          myCounters = null;
        }
      }
    }
  }

  public boolean isEmpty() {
    return myCounters == null;
  }
}
