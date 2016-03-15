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

import jetbrains.jetpad.hybrid.parser.IdentifierToken;
import jetbrains.jetpad.hybrid.testapp.mapper.ExprContainerMapper;
import jetbrains.jetpad.hybrid.testapp.model.Expr;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class HybridEditorTest extends BaseHybridEditorTest<ExprContainerMapper> {
  @Override
  protected ExprContainerMapper createMapper() {
    return new ExprContainerMapper(container);
  }

  @Override
  protected BaseHybridSynchronizer<Expr, ?> getSync(ExprContainerMapper mapper) {
    return mapper.hybridSync;
  }

  @Test
  public void initial() {
    assertEquals(1, myTargetCell.children().size());
    assertEquals(Arrays.asList(new IdentifierToken("id")), sync.tokens());
  }
}
