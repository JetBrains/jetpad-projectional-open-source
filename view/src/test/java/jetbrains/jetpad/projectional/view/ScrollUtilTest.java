/*
 * Copyright 2012-2016 JetBrains s.r.o
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
package jetbrains.jetpad.projectional.view;

import com.google.common.collect.Range;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ScrollUtilTest {
  @Test
  public void rangeContainment() {
    assertDelta(0, 0, 100, 1, 2);
  }

  @Test
  public void rangeLargeThanContainer() {
    assertDelta(-1, 0, 1, 1, 100);
  }

  @Test
  public void containerContainsEnd() {
    assertDelta(1, 10, 12, 9, 11);
  }

  @Test
  public void containerContainsStart() {
    assertDelta(-1, 10, 12, 11, 13);
  }

  @Test
  public void rangeLeftToContainer() {
    assertDelta(9, 10, 12, 1, 2);
  }

  @Test
  public void rangeRightToContainer() {
    assertDelta(-4, 10, 12, 15, 16);
  }

  private void assertDelta(int expectedDelta, int r1s, int r1e, int r2s, int r2e) {
    int delta = ScrollUtil.moveDelta(Range.closed(r1s, r1e), Range.closed(r2s, r2e));
    assertEquals(expectedDelta, delta);
  }
}