package jetbrains.jetpad.cell.toUtil;

import jetbrains.jetpad.model.util.ListMap;

public class Counters implements HasCounters {
  public static final CounterSpec HIGHLIGHT_COUNT = new CounterSpec("focusHighlight");
  public static final CounterSpec SELECT_COUNT = new CounterSpec("selectCount");
  public static final CounterSpec ERROR_COUNT = new CounterSpec("errors");
  public static final CounterSpec WARNING_COUNT = new CounterSpec("warning");

  private ListMap<CounterSpec, Integer> myCounters;

  public int getCounter(CounterSpec spec) {
    if (myCounters == null || !myCounters.containsKey(spec)) return 0;
    return myCounters.get(spec);
  }

  public void changeCounter(CounterSpec spec, int delta) {
    if (delta == 0) return;
    int oldVal = getCounter(spec);
    int newVal = oldVal + delta;

    if (newVal < 0) {
      throw new IllegalStateException("Counter " + spec + " decreased to the value less than zero.");
    }

    if (newVal != 0) {
      if (myCounters == null) {
        myCounters = new ListMap<>();
      }
      myCounters.put(spec, newVal);
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
