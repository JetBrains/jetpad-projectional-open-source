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
package jetbrains.jetpad.projectional.view.toAwt;

import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.projectional.view.*;
import jetbrains.jetpad.values.Color;

public class ViewDemo {
  public static void main(String[] args) {
    ViewContainer container = createConnectionDemo();

    AwtDemo.show(container);
  }

  private static ViewContainer createConnectionDemo() {
    ViewContainer container = new ViewContainer();

    GroupView gv = new GroupView();

    RectView r1 = new RectView();
    r1.background().set(Color.DARK_MAGENTA);
    r1.move(new Vector(50, 50));
    gv.children().add(r1);

    RectView r2 = new RectView();
    r2.background().set(Color.DARK_BLUE);
    r2.move(new Vector(80, 150));
    gv.children().add(r2);

    container.contentRoot().children().add(gv);
    gv.validate();

    LineView line = new LineView();
    gv.children().add(line);
    line.start().set(r1.bounds().get().center());
    line.end().set(r2.bounds().get().center());

    return container;
  }

  private static ViewContainer createRectDemo() {
    ViewContainer container = new ViewContainer();
    container.contentRoot().children().add(
      verticalView(
        horizontal(rect(Color.RED), rect(Color.BLACK), rect(Color.BLUE)),
        horizontal(rect(Color.BLUE), rect(Color.PINK), rect(Color.GRAY), rect(Color.DARK_GREEN))
      )
    );
    return container;
  }

  private static HorizontalView horizontal(View... views) {
    HorizontalView result = new HorizontalView();
    for (View v : views) {
      result.children().add(v);
    }
    return result;
  }

  private static VerticalView verticalView(View... views) {
    VerticalView result = new VerticalView();
    for (View v : views) {
      result.children().add(v);
    }
    return result;
  }

  private static RectView rect(Color color) {
    RectView result = new RectView();
    result.background().set(color);
    result.dimension().set(new Vector(80, 80));
    return result;
  }
}