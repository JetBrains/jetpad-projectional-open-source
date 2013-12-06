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
package jetbrains.mps.diagram.dataflow.view;

import jetbrains.jetpad.projectional.diagram.view.DiagramView;
import jetbrains.jetpad.projectional.diagram.view.decoration.DecorationContainer;
import jetbrains.jetpad.projectional.view.GroupView;
import jetbrains.jetpad.projectional.view.View;

public class LabelsView extends GroupView {
  public LabelsView(DecorationContainer<DiagramView> container) {
    container.addDecoration(this);
  }

  @Override
  protected void doValidate(ValidationContext ctx) {
    for(View v: children()) {
      v.invalidate();
    }
    super.doValidate(ctx);
  }
}