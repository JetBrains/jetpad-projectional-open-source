package jetbrains.jetpad.projectional.svgDemo;

import jetbrains.jetpad.projectional.svg.SvgRoot;
import jetbrains.jetpad.projectional.svg.toAwt.SvgRootDocumentMapper;
import org.apache.batik.swing.JSVGCanvas;

import javax.swing.*;
import java.awt.*;

public class AwtDemo {
  public static void main(String[] args) {
    JFrame frame = new JFrame("Svg Awt Demo");
    frame.setLayout(new BorderLayout());

    JSVGCanvas svgCanvas = new JSVGCanvas();

    SvgRoot svgRoot = DemoModel.createModel();
    SvgRootDocumentMapper mapper = new SvgRootDocumentMapper(svgRoot, SvgRootDocumentMapper.createDoc());
    mapper.attachRoot();

    svgCanvas.setSVGDocument(mapper.getTarget());

    frame.add(svgCanvas);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setSize(800, 600);
    frame.setVisible(true);
  }
}
