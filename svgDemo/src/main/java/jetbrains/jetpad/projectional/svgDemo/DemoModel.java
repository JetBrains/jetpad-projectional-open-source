package jetbrains.jetpad.projectional.svgDemo;

import jetbrains.jetpad.projectional.svg.SvgEllipse;
import jetbrains.jetpad.projectional.svg.SvgRect;
import jetbrains.jetpad.projectional.svg.SvgRoot;

public class DemoModel {
  public static SvgRoot createModel() {
    SvgRoot svgRoot = new SvgRoot();
    svgRoot.height.set(200.0);
    svgRoot.width.set(400.0);

    SvgEllipse ellipse = new SvgEllipse();
    ellipse.cx.set(200.0);
    ellipse.cy.set(80.0);
    ellipse.rx.set(100.0);
    ellipse.ry.set(50.0);
    ellipse.fill.set(jetbrains.jetpad.values.Color.YELLOW);

    SvgEllipse ellipse2 = new SvgEllipse();
    ellipse2.cx.set(250.0);
    ellipse2.cy.set(85.0);
    ellipse2.rx.set(40.0);
    ellipse2.ry.set(85.0);
    ellipse2.fill.set(jetbrains.jetpad.values.Color.GREEN);

    SvgRect rect = new SvgRect();
    rect.x.set(180.0);
    rect.y.set(50.0);
    rect.width.set(80.0);
    rect.height.set(50.0);
    rect.fill.set(jetbrains.jetpad.values.Color.RED);

    svgRoot.elements.add(ellipse);
    svgRoot.elements.add(rect);
    svgRoot.elements.add(ellipse2);

    return svgRoot;
  }
}
