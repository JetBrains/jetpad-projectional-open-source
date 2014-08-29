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
package jetbrains.jetpad.cell.completion;

import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.completion.CompletionController;
import jetbrains.jetpad.completion.CompletionSupplier;

public class Completion {
  public static final CellTraitPropertySpec<CompletionSupplier> COMPLETION = new CellTraitPropertySpec<>("completion", CompletionSupplier.EMPTY);
  public static final CellTraitPropertySpec<CompletionSupplier> LEFT_TRANSFORM = new CellTraitPropertySpec<>("leftTransform", CompletionSupplier.EMPTY);
  public static final CellTraitPropertySpec<CompletionSupplier> RIGHT_TRANSFORM = new CellTraitPropertySpec<>("rightTransform", CompletionSupplier.EMPTY);
  public static final CellTraitPropertySpec<CompletionController> COMPLETION_CONTROLLER = new CellTraitPropertySpec<>("completionController");
}