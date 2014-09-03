/*
 * Copyright 2012-2014 JetBrains s.r.o
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
package jetbrains.jetpad.projectional.svg;

public final class SvgAttributeSpec<ValueT> {
  public static <ValueT> SvgAttributeSpec<ValueT> createSpec(String name) {
    return new SvgAttributeSpec<>(name);
  }

  private String myName;

  protected SvgAttributeSpec(String name) {
    myName = name;
  }

  public String getName() {
    return myName;
  }

  @Override
  public String toString() {
    return myName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SvgAttributeSpec)) return false;

    SvgAttributeSpec that = (SvgAttributeSpec) o;

    if (!myName.equals(that.myName)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return myName.hashCode();
  }
}
