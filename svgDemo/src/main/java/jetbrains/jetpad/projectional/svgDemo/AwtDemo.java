package jetbrains.jetpad.projectional.svgDemo;

import jetbrains.jetpad.projectional.svg.SvgRoot;
import jetbrains.jetpad.projectional.svg.toAwt.SvgRootDocumentMapper;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.AbstractJSVGComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AwtDemo {
  public static void main(String[] args) {
    jetbrains.jetpad.projectional.view.toAwt.AwtDemo.show(DemoModel.demoViewContainer());
  }

  private static void noViewDemo() {
    JFrame frame = new JFrame("Svg Awt Demo");
    frame.setLayout(new BorderLayout());

    JSVGCanvas svgCanvas = new JSVGCanvas();
    svgCanvas.setDocumentState(AbstractJSVGComponent.ALWAYS_DYNAMIC);

    final SvgRoot svgRoot = DemoModel.createModel();
    SvgRootDocumentMapper mapper = new SvgRootDocumentMapper(svgRoot);
    mapper.attachRoot();

    svgCanvas.setSVGDocument(mapper.getTarget());

    svgCanvas.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        DemoModel.addCircle(svgRoot, e.getX(), e.getY());
      }
    });

    frame.add(svgCanvas);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setSize(800, 600);
    frame.setVisible(true);
  }
}
