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
package jetbrains.jetpad.cell.position;

import com.google.common.base.Function;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.cell.text.TextCellPositionHandler;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;

public interface PositionHandler {
  public static PositionHandler EMPTY = new EmptyPositionHandler();

  public static final CellTraitPropertySpec<PositionHandler> PROPERTY = new CellTraitPropertySpec<>("positionHandler", new Function<Cell, PositionHandler>() {
    @Override
    public PositionHandler apply(Cell input) {
      if (input instanceof TextCell) {
        return new TextCellPositionHandler((TextCell) input);
      }
      return new DefaultPositionHandler();
    }
  });

  boolean isHome();
  boolean isEnd();

  void home();
  void end();

  Property<Integer> caretOffset();
}