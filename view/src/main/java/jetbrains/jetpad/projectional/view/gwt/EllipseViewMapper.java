package jetbrains.jetpad.projectional.view.gwt;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.DerivedProperty;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.view.EllipseView;
import jetbrains.jetpad.values.Color;
import org.vectomatic.dom.svg.OMSVGDocument;
import org.vectomatic.dom.svg.OMSVGEllipseElement;
import org.vectomatic.dom.svg.OMSVGLineElement;
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

    final OMSVGEllipseElement ellipse = new OMSVGEllipseElement();
    svg.appendChild(ellipse);

    conf.add(GwtViewSynchronizers.svgBoundsSyncrhonizer(getSource(), svg));

    conf.add(Synchronizers.forProperty(getSource().bounds(), new WritableProperty<Rectangle>() {
      @Override
      public void set(Rectangle value) {
        ellipse.getCx().getBaseVal().setValue(value.dimension.x / 2);
        ellipse.getCy().getBaseVal().setValue(value.dimension.y / 2);

        ellipse.getRx().getBaseVal().setValue(value.dimension.x / 2);
        ellipse.getRy().getBaseVal().setValue(value.dimension.y / 2);
      }
    }));


    conf.add(Synchronizers.forProperty(getSource().background(), new WritableProperty<Color>() {
      @Override
      public void set(Color value) {
        ellipse.getStyle().setSVGProperty(SVGConstants.SVG_FILL_ATTRIBUTE, value.toCssColor());
      }
    }));
  }

  @Override
  protected boolean isCustomBackgroundSync() {
    return true;
  }
}
