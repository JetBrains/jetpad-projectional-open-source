package jetbrains.jetpad.projectional.view.gwt;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.event.CompositeEventSource;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.view.EllipseView;
import jetbrains.jetpad.values.Color;
import org.vectomatic.dom.svg.OMSVGDocument;
import org.vectomatic.dom.svg.OMSVGPathElement;
import org.vectomatic.dom.svg.OMSVGPathSegList;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.utils.OMSVGParser;
import org.vectomatic.dom.svg.utils.SVGConstants;

class EllipseViewMapper extends BaseViewMapper<EllipseView, Element> {
  EllipseViewMapper(View2DomContext ctx, EllipseView source) {
    super(ctx, source, DOM.createDiv());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    OMSVGDocument doc = OMSVGParser.createDocument();
    final OMSVGSVGElement svg = createSVG(doc);
    getTarget().appendChild(svg.getElement());

    final OMSVGPathElement path = new OMSVGPathElement();
    svg.appendChild(path);

    conf.add(GwtViewSynchronizers.svgBoundsSyncrhonizer(getSource(), svg));

    conf.add(Synchronizers.forEventSource(new CompositeEventSource<>(getSource().center(), getSource().radius(), getSource().from(), getSource().to()), new Runnable() {
      @Override
      public void run() {
        updatePath(path);
      }
    }));

    conf.add(Synchronizers.forProperty(getSource().background(), new WritableProperty<Color>() {
      @Override
      public void set(Color value) {
        path.getStyle().setSVGProperty(SVGConstants.SVG_FILL_ATTRIBUTE, value.toCssColor());
      }
    }));
  }

  private void updatePath(OMSVGPathElement path) {
    OMSVGPathSegList segList = path.getPathSegList();
    segList.clear();
    Vector radius = getSource().radius().get();

    double from = getSource().from().get();
    double to = getSource().to().get();
    double eps = 0.0001;

    if (Math.abs(2 * Math.PI - (to - from)) < eps) {
      segList.appendItem(path.createSVGPathSegMovetoAbs(radius.x, 0));
      segList.appendItem(path.createSVGPathSegArcAbs(radius.x, 2 * radius.y, radius.x, radius.y, 180, false, true));
      segList.appendItem(path.createSVGPathSegArcAbs(radius.x, 0, radius.x, radius.y, 180, false, true));
    } else {
      segList.appendItem(path.createSVGPathSegMovetoAbs(radius.x, radius.y));
      float sx = (float) (1 + Math.cos(from)) * radius.x;
      float sy = (float) (1 + Math.sin(from)) * radius.y;

      segList.appendItem(path.createSVGPathSegLinetoAbs(sx, sy));
      float tx = (float) (1 + Math.cos(to)) * radius.x;
      float ty = (float) (1 + Math.sin(to)) * radius.y;

      segList.appendItem(path.createSVGPathSegArcAbs(tx, ty, radius.x, radius.y, 0, (to - from) >= Math.PI, true));
      segList.appendItem(path.createSVGPathSegLinetoAbs(radius.x, radius.y));
    }
  }

  @Override
  protected boolean isCustomBackgroundSync() {
    return true;
  }
}
