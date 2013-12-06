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
package jetbrains.mps.diagram.contentDemo;

import jetbrains.jetpad.projectional.view.awt.AwtDemo;
import jetbrains.mps.diagram.contentDemo.mapper.ContentRootMapper;
import jetbrains.mps.diagram.contentDemo.model.Content;
import jetbrains.mps.diagram.contentDemo.model.ContentItem;
import jetbrains.mps.diagram.dataflow.Demo;
import jetbrains.mps.diagram.dataflow.model.Diagram;

public class ContentDemoMain {
  public static void main(String[] args) {
    Diagram model = Demo.createDemoModel();
    Content content = createDemo();
    ContentRootMapper mapper = new ContentRootMapper(model, content);
    mapper.attachRoot();

    AwtDemo.show(mapper.getTarget());
  }

  private static Content createDemo() {
    Content demo = new Content();
    demo.name.set("test");
    demo.items.add(new ContentItem());
    demo.items.add(new ContentItem());
    return demo;
  }
}