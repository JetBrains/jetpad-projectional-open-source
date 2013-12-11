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
package jetbrains.jetpad.projectional.cell.hybrid;

import jetbrains.jetpad.projectional.parser.Token;
import jetbrains.jetpad.projectional.cell.action.CellAction;

public interface Completer {
  CellAction complete(Token token);
  CellAction complete(Token... token);

  /**
   * @param selectionIndex of the passed token to be selected after the action
   */
  CellAction complete(int selectionIndex, Token... tokens);
}