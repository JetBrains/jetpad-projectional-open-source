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
package jetbrains.jetpad.projectional.svgDemo;

import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.event.Key;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.projectional.svg.*;
import jetbrains.jetpad.projectional.svg.event.SvgEventHandler;
import jetbrains.jetpad.projectional.svg.event.SvgEventSpec;
import jetbrains.jetpad.projectional.view.*;
import jetbrains.jetpad.values.Color;

public class DemoModel {
  public static SvgSvgElement createModel() {
    final SvgSvgElement svgRoot = new SvgSvgElement(400., 200.);
    svgRoot.setStyle(new CssRes());

    SvgEllipseElement ellipse = new SvgEllipseElement(200., 80., 170., 50.);
    ellipse.getAttribute("style").set("stroke:#006600;");
    ellipse.addClass("ellipse-yellow");
    ellipse.strokeOpacity().set(0.6);

    SvgTextElement text = new SvgTextElement(20., 20., "Example Text");

    SvgPathDataBuilder pathBuilder = new SvgPathDataBuilder(false);
    pathBuilder.moveTo(150., 175., true)
        .verticalLineTo(-100.)
        .ellipticalArc(100., 100., 0., false, false, -100., 100.)
        .closePath();

    SvgPathElement path = new SvgPathElement(pathBuilder.build());
    path.fill().set(SvgColor.RED);
    path.stroke().set(SvgColor.create(0, 0, 255));
    path.getAttribute("stroke-width").set("5");

    SvgEllipseElement ellipse2 = new SvgEllipseElement(250., 85., 40., 85.);
    ellipse2.fill().set(SvgColor.GREEN);

    SvgRectElement rect = new SvgRectElement(180., 50., 80., 50.);
    rect.fillColor().set(new Color(255, 0, 0, 100));

    svgRoot.children().add(ellipse);
    svgRoot.children().add(rect);
    svgRoot.children().add(ellipse2);
    svgRoot.children().add(text);
    svgRoot.children().add(path);

    ellipse.addEventHandler(SvgEventSpec.MOUSE_PRESSED, new SvgEventHandler<MouseEvent>() {
      @Override
      public void handle(SvgNode node, MouseEvent e) {
        DemoModel.addCircle(svgRoot, e.getX(), e.getY());
      }
    });

    return svgRoot;
  }

  public static SvgSvgElement createAltModel() {
    final SvgSvgElement svgRoot = new SvgSvgElement();
    svgRoot.height().set(400.);
    svgRoot.width().set(200.);

    SvgRectElement rect = new SvgRectElement();
    rect.x().set(10.);
    rect.y().set(100.);
    rect.height().set(180.);
    rect.width().set(180.);

    SvgEllipseElement ellipse = new SvgEllipseElement();
    ellipse.cx().set(100.);
    ellipse.cy().set(190.);
    ellipse.rx().set(50.);
    ellipse.ry().set(50.);
    ellipse.fill().set(SvgColor.RED);

    svgRoot.children().add(rect);
    svgRoot.children().add(ellipse);

    ellipse.addEventHandler(SvgEventSpec.MOUSE_PRESSED, new SvgEventHandler<MouseEvent>() {
      @Override
      public void handle(SvgNode node, MouseEvent e) {
        DemoModel.addCircle(svgRoot, e.getX(), e.getY());
      }
    });

    return svgRoot;
  }

  public static void addCircle(SvgSvgElement svgRoot, int x, int y) {
    SvgCircleElement circle = new SvgCircleElement((double) x, (double) y, 10.);
    circle.fillColor().set(Color.BLACK);

    svgRoot.children().add(circle);
  }

  public static ViewContainer demoViewContainer() {
    final SvgSvgElement model = createModel();
    final SvgSvgElement altModel = createAltModel();
    final SvgView svgView = new SvgView(model);
    svgView.border().set(Color.GRAY);

    ViewContainer container = new ViewContainer();
    final HorizontalView hView = new HorizontalView();
    VerticalView vView = new VerticalView();
    final TextView textView = new TextView("Press any key to change to alternative model");
    TextView textView2 = new TextView("Use mouse clicks to add some black circles");
    hView.children().add(svgView);
    hView.children().add(textView);
    vView.children().add(hView);
    vView.children().add(textView2);
    container.contentRoot().children().add(vView);

    final Value<Boolean> state = new Value<>(true);

    final Value<Boolean> viewState = new Value<>(true);
    container.root().addTrait(new ViewTraitBuilder()
    .on(ViewEvents.KEY_PRESSED, new ViewEventHandler<KeyEvent>() {
      @Override
      public void handle(View view, KeyEvent e) {
        if (e.key() == Key.SPACE) {
          ((SvgElement) model.children().get(1)).getAttribute("stroke-width").set("20");
          return;
        }

        if (e.key() == Key.P) {
          if (!viewState.get()) return;
          hView.children().remove(svgView);
          viewState.set(false);
          return;
        }

        if (e.key() == Key.O) {
          if (viewState.get()) return;
          hView.children().add(0, svgView);
          viewState.set(true);
          return;
        }

        if (!viewState.get()) return;

        if (state.get()) {
          svgView.root().set(altModel);
        } else {
          svgView.root().set(model);
        }
        state.set(!state.get());
      }
    })
    .build());

    return container;
  }
}