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
package jetbrains.mps.diagram.contentDemo.view;

import jetbrains.jetpad.projectional.diagram.view.layout.CenterVerticalLayoutView;
import jetbrains.jetpad.projectional.diagram.view.layout.IndentHorizontalLayoutView;
import jetbrains.jetpad.projectional.view.TextView;
import jetbrains.jetpad.values.Color;

public class ContentDemoView extends CenterVerticalLayoutView {
  public final TextView nameView = new TextView();
  public final IndentHorizontalLayoutView itemsView = new IndentHorizontalLayoutView();

  public ContentDemoView() {
    initName();
    initItems();
  }

  private void initName() {
    children().add(nameView);
  }

  private void initItems() {
    itemsView.padding().set(10);
    itemsView.indent().set(5);
    itemsView.border().set(Color.BLACK);
    children().add(itemsView);
  }
}