/*
 * Copyright 2012-2013 JetBrains s.r.o
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
package jetbrains.jetpad.cell;

import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.property.Property;

public class ScrollCell extends Cell {
  public static final CellPropertySpec<Vector> MAX_DIMENSION = new CellPropertySpec<Vector>("max-dimension", Vector.ZERO);
  public static final CellPropertySpec<Boolean> SCROLL = new CellPropertySpec<Boolean>("scroll", false);

  public Property<Vector> maxDimension() {
    return getProp(MAX_DIMENSION);
  }

  public Property<Boolean> scroll() {
    return getProp(SCROLL);
  }
}