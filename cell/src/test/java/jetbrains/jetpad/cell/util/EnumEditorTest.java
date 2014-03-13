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
package jetbrains.jetpad.cell.util;

import jetbrains.jetpad.cell.EditingTestCase;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.event.KeyStrokeSpecs;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyBinding;
import jetbrains.jetpad.model.property.ValueProperty;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EnumEditorTest extends EditingTestCase {
  private Property<TestEnum> property = new ValueProperty<>();
  private TextCell cell = new TextCell();

  @Before
  public void init() {
    myCellContainer.root.children().add(cell);
    PropertyBinding.bind(property, ValueEditors.enumProperty(cell, TestEnum.class));
  }

  @Test
  public void modelChange() {
    property.set(TestEnum.A);
    assertEquals("A", cell.text().get());
  }

  @Test
  public void textChange() {
    cell.text().set("B");
    assertEquals(TestEnum.B, property.get());
  }

  @Test
  public void clearWithDeleteKeys() {
    property.set(TestEnum.C);
    cell.focus();
    press(KeyStrokeSpecs.DELETE_CURRENT);

    assertNull(property.get());
  }

  enum TestEnum {
    A, B, C
  }
}
