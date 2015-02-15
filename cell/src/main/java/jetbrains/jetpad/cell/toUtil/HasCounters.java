package jetbrains.jetpad.cell.toUtil;

public interface HasCounters {
  int getCounter(CounterSpec spec);
  void changeCounter(CounterSpec spec, int delta);
}
