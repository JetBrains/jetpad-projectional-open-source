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
package jetbrains.jetpad.projectional.cell.support;

import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.Registration;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

class DefaultPositionHandler implements PositionHandler {
  @Override
  public boolean isHome() {
    return false;
  }

  @Override
  public boolean isEnd() {
    return false;
  }

  @Override
  public void home() {
  }

  @Override
  public void end() {
  }

  @Override
  public Property<Integer> caretOffset() {
    return new Property<Integer>() {
      @Override
      public Integer get() {
        return 0;
      }

      @Override
      public Registration addHandler(EventHandler<? super PropertyChangeEvent<Integer>> handler) {
        return Registration.EMPTY;
      }

      @Override
      public void set(Integer value) {
      }

      @Override
      public String getPropExpr() {
        return "defaultCaretOffset";
      }
    };
  }
}