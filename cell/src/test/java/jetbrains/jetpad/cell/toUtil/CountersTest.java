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
package jetbrains.jetpad.cell.toUtil;

import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class CountersTest extends BaseTestCase {

  @Test
  public void incorrectState() {
    Counters counters = new Counters();

    counters.changeCounter(Counters.SELECT_COUNT, 1);
    counters.changeCounter(Counters.SELECT_COUNT, 1);
    counters.changeCounter(Counters.SELECT_COUNT, -1);
    counters.changeCounter(Counters.SELECT_COUNT, -1);
  }

  @Test
  public void moreThanOneValuedCounter() {
    Counters counters = new Counters();

    counters.changeCounter(Counters.SELECT_COUNT, 1);
    counters.changeCounter(Counters.SELECT_COUNT, 1);

    assertEquals(2, counters.getCounter(Counters.SELECT_COUNT));
  }


}
