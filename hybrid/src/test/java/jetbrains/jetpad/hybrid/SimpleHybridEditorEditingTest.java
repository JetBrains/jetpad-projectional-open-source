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
package jetbrains.jetpad.hybrid;

import jetbrains.jetpad.hybrid.testapp.mapper.SimpleExprContainerMapper;
import jetbrains.jetpad.hybrid.testapp.model.Expr;
import org.junit.Test;

import static org.junit.Assert.*;

public class SimpleHybridEditorEditingTest extends BaseHybridEditorEditingTest<SimpleExprContainerMapper> {
  @Override
  protected SimpleExprContainerMapper createMapper() {
    return new SimpleExprContainerMapper(container);
  }

  @Override
  protected BaseHybridSynchronizer<Expr> getSync(SimpleExprContainerMapper mapper) {
    return mapper.hybridSync;
  }

  @Test
  public void simpleTyping() {
    type("id");
    type("+");
    type("id");

    assertEquals(5, myTargetCell.children().size());
    assertEquals(3, sync.tokens().size());
  }

  @Test
  public void typeDelete() {
    type("id");
    del();

    assertEquals(1, myTargetCell.children().size());
    assertEquals(1, sync.tokens().size());
  }

  @Test
  public void typeBackspace() {
    type("id");
    type("+");
    backspace();
    backspace();

    assertEquals(1, myTargetCell.children().size());
    assertEquals(1, sync.tokens().size());
  }
}
