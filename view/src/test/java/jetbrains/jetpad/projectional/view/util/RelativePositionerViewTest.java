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
package jetbrains.jetpad.projectional.view.util;

import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.projectional.view.View;
import org.junit.Assert;
import org.junit.Test;

public class RelativePositionerViewTest {
  private RelativePositionerView positionerView = new RelativePositionerView(new TestView());

  @Test
  public void horizontalLeft() {
    positionerView.horizontalAnchor().set(RelativePositionerView.HorizontalAnchor.LEFT);
    assertBounds(0, 15, 10, 20);
  }

  @Test
  public void horizontalRight() {
    positionerView.horizontalAnchor().set(RelativePositionerView.HorizontalAnchor.RIGHT);
    assertBounds(-10, 15, 10, 20);
  }

  @Test
  public void horizontalCenter() {
    positionerView.horizontalAnchor().set(RelativePositionerView.HorizontalAnchor.CENTER);
    assertBounds(-5, 15, 10, 20);
  }

  @Test
  public void verticalBaseLine() {
    positionerView.verticalAnchor().set(RelativePositionerView.VerticalAnchor.BASELINE);
    assertBounds(0, 15, 10, 20);
  }

  @Test
  public void verticalTop() {
    positionerView.verticalAnchor().set(RelativePositionerView.VerticalAnchor.TOP);
    assertBounds(0, 0, 10, 20);
  }

  @Test
  public void verticalBottom() {
    positionerView.verticalAnchor().set(RelativePositionerView.VerticalAnchor.BOTTOM);
    assertBounds(0, -20, 10, 20);
  }

  @Test
  public void verticalCenter() {
    positionerView.verticalAnchor().set(RelativePositionerView.VerticalAnchor.CENTER);
    assertBounds(0, -10, 10, 20);
  }

  public void assertBounds(int x, int y, int w, int h) {
    positionerView.validate();
    Assert.assertEquals(new Rectangle(x, y, w, h), positionerView.bounds().get());
  }

  private class TestView extends View {
    @Override
    protected void doValidate(ValidationContext ctx) {
      super.doValidate(ctx);
      ctx.bounds(new Rectangle(0, 0, 10, 20), 15);
    }
  }


}