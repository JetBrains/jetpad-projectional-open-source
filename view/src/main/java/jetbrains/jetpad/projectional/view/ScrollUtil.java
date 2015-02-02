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
package jetbrains.jetpad.projectional.view;

import com.google.common.collect.Range;

class ScrollUtil {
  static int moveDelta(Range<Integer> container, Range<Integer> range) {
    if (container.encloses(range)) return 0;
    if (container.upperEndpoint() - container.lowerEndpoint() < range.upperEndpoint() - range.lowerEndpoint()) {
      return container.lowerEndpoint() - range.lowerEndpoint();
    }

    if (container.contains(range.upperEndpoint())) {
      return container.lowerEndpoint() - range.lowerEndpoint();
    }

    if (container.contains(range.lowerEndpoint())) {
      return container.upperEndpoint() - range.upperEndpoint();
    }

    if (container.upperEndpoint() < range.lowerEndpoint()) {
      return container.upperEndpoint() - range.upperEndpoint();
    } else if (container.lowerEndpoint() > range.upperEndpoint()) {
      return container.lowerEndpoint() - range.lowerEndpoint();
    } else {
      throw new IllegalStateException("This can't happen");
    }
  }
}