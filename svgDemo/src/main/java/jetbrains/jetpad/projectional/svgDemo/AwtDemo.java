package jetbrains.jetpad.projectional.svgDemo;

import jetbrains.jetpad.projectional.svg.SvgRoot;
import jetbrains.jetpad.projectional.svg.toAwt.SvgRootDocumentMapper;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.AbstractJSVGComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class AwtDemo {
  public static void main(String[] args) {
    JFrame frame = new JFrame("Svg Awt Demo");
    frame.setLayout(new BorderLayout());

    JSVGCanvas svgCanvas = new JSVGCanvas();
    svgCanvas.setDocumentState(AbstractJSVGComponent.ALWAYS_DYNAMIC);

    final SvgRoot svgRoot = DemoModel.createModel();
    SvgRootDocumentMapper mapper = new SvgRootDocumentMapper(svgRoot, SvgRootDocumentMapper.createDoc());
    mapper.attachRoot();

    svgCanvas.setSVGDocument(mapper.getTarget());

    svgCanvas.addMouseListener(new MouseListener() {
      @Override
      public void mouseClicked(MouseEvent e) {
        DemoModel.addCircle(svgRoot, e.getX(), e.getY());
      }

      @Override
      public void mousePressed(MouseEvent e) {
      }

      @Override
      public void mouseReleased(MouseEvent e) {

      }

      @Override
      public void mouseEntered(MouseEvent e) {

      }

      @Override
      public void mouseExited(MouseEvent e) {

      }
    });

    frame.add(svgCanvas);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setSize(800, 600);
    frame.setVisible(true);
  }
}
