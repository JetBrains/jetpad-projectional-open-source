package jetbrains.jetpad.projectional.svgDemo;

import com.google.gwt.core.client.EntryPoint;
import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.projectional.svg.SvgRoot;
import jetbrains.jetpad.projectional.view.*;
import jetbrains.jetpad.projectional.view.toGwt.ViewToDom;

import static com.google.gwt.query.client.GQuery.$;

public class GwtDemo implements EntryPoint {
  public void onModuleLoad() {
    SvgRoot svgRoot = DemoModel.createModel();
    final SvgView svgView = new SvgView(svgRoot);

    ViewContainer container = new ViewContainer();
    container.contentRoot().children().add(svgView);

    final Value<Boolean> state = new Value<>(true);

    container.root().addTrait(new ViewTraitBuilder().on(ViewEvents.MOUSE_PRESSED, new ViewEventHandler<MouseEvent>() {
      @Override
      public void handle(View view, MouseEvent e) {
        DemoModel.addCircle(svgView.svgRoot.get(), e.x(), e.y());
      }
    })
    .on(ViewEvents.KEY_PRESSED, new ViewEventHandler<KeyEvent>() {
      @Override
      public void handle(View view, KeyEvent e) {
        if (state.get()) {
          svgView.svgRoot.set(DemoModel.createAltModel());
        } else {
          svgView.svgRoot.set(DemoModel.createModel());
        }
        state.set(!state.get());
      }
    })
    .build());

    ViewToDom.map(container, $("#svg").get(0));
  }
}
