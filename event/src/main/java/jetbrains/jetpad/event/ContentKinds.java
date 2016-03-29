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

import java.util.List;

public class ContentKinds {
  public static final ContentKind<String> SINGLE_LINE_TEXT = create("singleLineText");
  public static final ContentKind<Iterable<String>> MULTILINE_TEXT = create("multilineText");
  public static final ContentKind<String> ANY_TEXT = create("anyText");

  public static <T> ContentKind<T> create(final String name) {
    return new ContentKind<T>() {
      @Override
      public String toString() {
        return name;
      }
    };
  }

  public static <T> ContentKind<List<T>> listOf(ContentKind<T> kind) {
    return new ListContentKind<>(kind);
  }

  static class ListContentKind<T> implements ContentKind<List<T>> {
    private ContentKind<T> myBaseKind;

    ListContentKind(ContentKind<T> baseKind) {
      myBaseKind = baseKind;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof ListContentKind)) return false;
      return ((ListContentKind) obj).myBaseKind.equals(myBaseKind);
    }

    @Override
    public int hashCode() {
      return myBaseKind.hashCode();
    }
  }
}