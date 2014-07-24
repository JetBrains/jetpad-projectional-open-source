package jetbrains.jetpad.projectional.svgDemo;

import jetbrains.jetpad.projectional.svg.SvgRoot;
import jetbrains.jetpad.projectional.svg.toAwt.SvgRootMapper;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.svg.SVGOMSVGElement;
import org.apache.batik.swing.JSVGCanvas;
import org.w3c.dom.DOMImplementation;

import javax.swing.*;
import java.awt.*;

public class AwtDemo {
  public static void main(String[] args) {
    JFrame frame = new JFrame("Svg Awt Demo");
    frame.setLayout(new BorderLayout());

    JSVGCanvas svgCanvas = new JSVGCanvas();

    DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
    String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
    SVGOMDocument doc = (SVGOMDocument) impl.createDocument(svgNS, "svg", null);

    SvgRoot svgRoot = DemoModel.createModel();

    SvgRootMapper mapper = new SvgRootMapper(svgRoot, (SVGOMSVGElement) doc.getDocumentElement(), doc);
    mapper.attachRoot();

    svgCanvas.setSVGDocument(doc);

    frame.add(svgCanvas);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setSize(800, 600);
    frame.setVisible(true);
  }
}
