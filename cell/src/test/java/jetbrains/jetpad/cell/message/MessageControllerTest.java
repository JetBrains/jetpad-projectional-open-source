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
package jetbrains.jetpad.cell.message;

import jetbrains.jetpad.cell.HorizontalCell;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class MessageControllerTest extends MessageControllerTestCase {

  @Test
  public void setErrorBeforeAttach() {
    HorizontalCell parent = new HorizontalCell();
    HorizontalCell child = new HorizontalCell();
    parent.children().add(child);
    MessageController.setError(child, "1");

    myCellContainer.root.children().add(parent);

    assertTrue(MessageController.hasError(child));
  }

  @Test
  public void setNotBrokenBeforeAttach() {
    HorizontalCell child = new HorizontalCell();
    MessageController.setBroken(child, "a");
    assertTrue(MessageController.isBroken(child));

    cell.children().add(child);
    MessageController.setBroken(child, null);
  }
}