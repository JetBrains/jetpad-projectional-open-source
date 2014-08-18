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
import jetbrains.jetpad.projectional.view.*;
import jetbrains.jetpad.values.Color;

public class DemoModel {
  public static SvgSvgElement createModel() {
    SvgSvgElement svgRoot = new SvgSvgElement();
    svgRoot.getProp(SvgSvgElement.HEIGHT).set(200.0);
    svgRoot.getProp(SvgSvgElement.WIDTH).set(400.0);

    SvgEllipseElement ellipse = new SvgEllipseElement();
    ellipse.getProp(SvgEllipseElement.CX).set(200.0);
    ellipse.getProp(SvgEllipseElement.CY).set(80.0);
    ellipse.getProp(SvgEllipseElement.RX).set(170.0);
    ellipse.getProp(SvgEllipseElement.RY).set(50.0);
//    ellipse.getXmlAttr("fill").set(Color.YELLOW.toCssColor());
    ellipse.setXmlAttr("fill", Color.YELLOW.toCssColor());
    ellipse.setXmlAttr("style", "stroke:#006600;");
    ellipse.getProp(SvgStylableElement.CLASS).set("ellipse-yellow");

    SvgTextElement text = new SvgTextElement();
    text.addTextNode("Example Text");
    text.getX().set(20.);
    text.getY().set(20.);

    SvgPathDataBuilder pathBuilder = new SvgPathDataBuilder(false);
    pathBuilder.moveTo(150., 175., true)
        .verticalLineTo(-100.)
        .ellipticalArc(100., 100., 0., false, false, -100., 100.)
        .closePath();

    SvgPathElement path = new SvgPathElement();
    path.getProp(SvgPathElement.D).set(pathBuilder.build());
    path.setXmlAttr("fill", "red");
    path.setXmlAttr("stroke", "blue");
    path.setXmlAttr("stroke-width", "5");

    SvgEllipseElement ellipse2 = new SvgEllipseElement();
    ellipse2.getProp(SvgEllipseElement.CX).set(250.0);
    ellipse2.getProp(SvgEllipseElement.CY).set(85.0);
    ellipse2.getProp(SvgEllipseElement.RX).set(40.0);
    ellipse2.getProp(SvgEllipseElement.RY).set(85.0);
    ellipse2.setXmlAttr("fill", Color.GREEN.toCssColor());

    SvgRectElement rect = new SvgRectElement();
    rect.getProp(SvgRectElement.X).set(180.0);
    rect.getProp(SvgRectElement.Y).set(50.0);
    rect.getProp(SvgRectElement.WIDTH).set(80.0);
    rect.getProp(SvgRectElement.HEIGHT).set(50.0);
    rect.setXmlAttr("fill", Color.RED.toCssColor());

    svgRoot.children().add(ellipse);
    svgRoot.children().add(rect);
    svgRoot.children().add(ellipse2);
    svgRoot.children().add(text);
    svgRoot.children().add(path);

    return svgRoot;
  }

  public static SvgSvgElement createAltModel() {
    SvgSvgElement svgRoot = new SvgSvgElement();
    svgRoot.getProp(SvgSvgElement.HEIGHT).set(400.0);
    svgRoot.getProp(SvgSvgElement.WIDTH).set(200.0);

    SvgRectElement rect = new SvgRectElement();
    rect.getProp(SvgRectElement.X).set(10.0);
    rect.getProp(SvgRectElement.Y).set(100.0);
    rect.getProp(SvgRectElement.HEIGHT).set(180.0);
    rect.getProp(SvgRectElement.WIDTH).set(180.0);

    SvgEllipseElement ellipse = new SvgEllipseElement();
    ellipse.getProp(SvgEllipseElement.CX).set(100.0);
    ellipse.getProp(SvgEllipseElement.CY).set(190.0);
    ellipse.getProp(SvgEllipseElement.RX).set(50.0);
    ellipse.getProp(SvgEllipseElement.RY).set(50.0);
    ellipse.setXmlAttr("fill", Color.RED.toCssColor());

    svgRoot.children().add(rect);
    svgRoot.children().add(ellipse);

    return svgRoot;
  }

  public static void addCircle(SvgSvgElement svgRoot, int x, int y) {
    SvgEllipseElement circle = new SvgEllipseElement();
    circle.getProp(SvgEllipseElement.CX).set((double) x);
    circle.getProp(SvgEllipseElement.CY).set((double) y);
    circle.getProp(SvgEllipseElement.RX).set(10.0);
    circle.getProp(SvgEllipseElement.RY).set(10.0);
    circle.setXmlAttr("fill", Color.BLACK.toCssColor());

    svgRoot.children().add(circle);
  }

  public static ViewContainer demoViewContainer() {
    final SvgSvgElement model = createModel();
    final SvgSvgElement altModel = createAltModel();
    final SvgView svgView = new SvgView(model);
    svgView.border().set(Color.GRAY);

    ViewContainer container = new ViewContainer();
    HorizontalView hView = new HorizontalView();
    VerticalView vView = new VerticalView();
    final TextView textView = new TextView("Press any key to change to alternative model");
    TextView textView2 = new TextView("Use mouse clicks to add some black circles");
    hView.children().add(svgView);
    hView.children().add(textView);
    vView.children().add(hView);
    vView.children().add(textView2);
    container.contentRoot().children().add(vView);

    final Value<Boolean> state = new Value<>(true);

    model.addTrait(new SvgTraitBuilder().on(SvgEvents.MOUSE_PRESSED, new SvgEventHandler<MouseEvent>() {
      @Override
      public void handle(SvgNode node, MouseEvent e) {
        DemoModel.addCircle(model, e.x(), e.y());
      }
    })
    .build());

    altModel.addTrait(new SvgTraitBuilder().on(SvgEvents.MOUSE_PRESSED, new SvgEventHandler<MouseEvent>() {
      @Override
      public void handle(SvgNode node, MouseEvent e) {
        DemoModel.addCircle(altModel, e.x(), e.y());
      }
    })
    .build());

    container.root().addTrait(new ViewTraitBuilder()
    .on(ViewEvents.KEY_PRESSED, new ViewEventHandler<KeyEvent>() {
      @Override
      public void handle(View view, KeyEvent e) {
        if (e.key() == Key.SPACE) {
          ((SvgElement) model.children().get(0)).setXmlAttr("stroke-width", "7");
          return;
        }
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
