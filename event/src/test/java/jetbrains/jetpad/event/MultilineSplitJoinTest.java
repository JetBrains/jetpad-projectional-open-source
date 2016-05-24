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
package jetbrains.jetpad.event;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static org.junit.Assert.assertEquals;

public class MultilineSplitJoinTest {
  private static final List<Sample> SAMPLES = of(
      new Sample(""),
      new Sample("", ""),
      new Sample("", "b"),
      new Sample("a", "b"),
      new Sample("a", "", "b")
  );

  @Test
  public void testAllSamples() {
    for (Sample sample : SAMPLES) {
      for (String multiline : sample.getDifferentSeparatorsMultilines()) {
        testSplit(multiline, sample.lines);
      }
      testJoin(sample.getStandardMultiline(), sample.lines);
    }
  }

  private void testSplit(String multiline, List<String> expectedParts) {
    Iterable<String> actualParts = TextContentHelper.splitByNewline(multiline);
    assertEquals(expectedParts, Lists.newArrayList(actualParts));
  }

  private void testJoin(String expectedMultiline, List<String> parts) {
    String actualMultiline = TextContentHelper.joinLines(parts);
    assertEquals(expectedMultiline, actualMultiline);
  }

  private static class Sample {
    private static final String STANDARD_SEPARATOR = "\n";
    private static final List<String> SEPARATORS = of(STANDARD_SEPARATOR, "\r", "\r\n", "\n\r");

    private final List<String> lines;

    private Sample(String ... lines) {
      this.lines = Arrays.asList(lines);
    }

    private String getStandardMultiline() {
      return Joiner.on(STANDARD_SEPARATOR).join(lines);
    }

    private List<String> getDifferentSeparatorsMultilines() {
      return FluentIterable.from(SEPARATORS)
          .transform(new Function<String, String>() {
            @Override
            public String apply(String separator) {
              return Joiner.on(separator).join(lines);
            }
          }).toList();
    }
  }
}
