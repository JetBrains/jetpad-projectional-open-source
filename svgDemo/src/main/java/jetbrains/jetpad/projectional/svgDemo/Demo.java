package jetbrains.jetpad.projectional.svgDemo;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.*;
import jetbrains.jetpad.projectional.svg.SvgRoot;
import jetbrains.jetpad.projectional.svg.toDom.SvgRootMapper;
import org.vectomatic.dom.svg.OMSVGSVGElement;

public class Demo implements EntryPoint {
  public void onModuleLoad() {
    SvgRoot svgRoot = DemoModel.createModel();

    SvgRootMapper mapper = new SvgRootMapper(svgRoot, new OMSVGSVGElement());
    mapper.attachRoot();

    RootPanel.get().getElement().appendChild(mapper.getTarget().getElement());
  }
}
