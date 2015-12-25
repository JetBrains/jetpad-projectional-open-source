/*
 * Copyright 2012-2015 JetBrains s.r.o
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.jetpad.cell.mappersUtil;

import jetbrains.jetpad.model.util.ListMap;

public class Counters implements HasCounters {
  public static final CounterSpec HIGHLIGHT_COUNT = new CounterSpec("focusHighlight");
  public static final CounterSpec SELECT_COUNT = new CounterSpec("selectCount");

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
