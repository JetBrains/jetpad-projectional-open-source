package client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.*;
import jetbrains.jetpad.projectional.svg.SvgEllipse;
import jetbrains.jetpad.projectional.svg.SvgRoot;
import jetbrains.jetpad.projectional.svg.toDom.SvgRootMapper;
import jetbrains.jetpad.values.Color;
import org.vectomatic.dom.svg.OMSVGSVGElement;

public class Demo implements EntryPoint {
  public void onModuleLoad() {
    SvgRoot svgRoot = new SvgRoot();
    svgRoot.height.set(140);
    svgRoot.width.set(400);

    SvgEllipse ellipse = new SvgEllipse();
    ellipse.cx.set(200);
    ellipse.cy.set(80);
    ellipse.rx.set(100);
    ellipse.ry.set(50);
    ellipse.fill.set(Color.YELLOW);

    SvgEllipse ellipse2 = new SvgEllipse();
    ellipse2.cx.set(250);
    ellipse2.cy.set(85);
    ellipse2.rx.set(40);
    ellipse2.ry.set(85);
    ellipse2.fill.set(Color.GREEN);

    svgRoot.elements.add(ellipse);
    svgRoot.elements.add(ellipse2);

    SvgRootMapper mapper = new SvgRootMapper(svgRoot, new OMSVGSVGElement());
    mapper.attachRoot();

    RootPanel.get().getElement().appendChild(mapper.getTarget().getElement());
  }
}
