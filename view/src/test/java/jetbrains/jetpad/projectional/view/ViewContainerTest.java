/*
 * Copyright 2012-2015 JetBrains s.r.o
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
package jetbrains.jetpad.projectional.view;

import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.geometry.Vector;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ViewContainerTest {
  ViewContainer container = new ViewContainer();
  TestView view1 = new TestView();
  TestView view2 = new TestView();

  @Before
  public void init() {
    HorizontalView hContainer = new HorizontalView();
    hContainer.children().addAll(Arrays.asList(view1, view2));
    container.contentRoot().children().add(hContainer);
    container.root().validate();
  }

  @Test
  public void mouseEnter() {
    container.mouseEntered(new MouseEvent(view1.getBounds().center()));

    assertTrue(view1.myMouseIn);
    assertFalse(view2.myMouseIn);
  }

  @Test
  public void mouseMove() {
    container.mouseEntered(new MouseEvent(view1.getBounds().center()));
    container.mouseMoved(new MouseEvent(view1.getBounds().center().add(new Vector(1, 1))));
    container.mouseMoved(new MouseEvent(view2.getBounds().center()));

    assertFalse(view1.myMouseIn);
    assertTrue(view2.myMouseIn);
  }

  @Test
  public void mouseLeft() {
    container.mouseEntered(new MouseEvent(view1.getBounds().center()));
    container.mouseLeft(new MouseEvent(new Vector(0, 0)));

    assertFalse(view1.myMouseIn);
    assertFalse(view2.myMouseIn);
  }

  private class TestView extends TextView {
    private boolean myMouseIn;


    {
      text().set("TestView");

      addTrait(new ViewTraitBuilder()
        .on(ViewEvents.MOUSE_ENTERED, new ViewEventHandler<MouseEvent>() {
          @Override
          public void handle(View view, MouseEvent e) {
            myMouseIn = true;
          }
        })
        .on(ViewEvents.MOUSE_LEFT, new ViewEventHandler<MouseEvent>() {
          @Override
          public void handle(View view, MouseEvent e) {
            myMouseIn = false;
          }
        })
        .build());
    }

  }
}