package jetbrains.jetpad.projectional.view.toGwt;

import com.google.gwt.dom.client.Element;

class SvgUtil {
  static native Element createSvgElement(String name) /*-{
    return $doc.createElementNS('http://www.w3.org/2000/svg', name);
  }-*/;

  static Element createPath() {
    return createSvgElement("path");
  }

  static Element createPolyline() {
    return createSvgElement("polyline");
  }
}
