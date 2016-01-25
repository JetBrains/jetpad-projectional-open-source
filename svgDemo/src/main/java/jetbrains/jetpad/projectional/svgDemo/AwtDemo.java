/*
 * Copyright 2012-2016 JetBrains s.r.o
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.jetpad.projectional.svgDemo;

import jetbrains.jetpad.projectional.svg.SvgSvgElement;
import jetbrains.jetpad.projectional.svg.toAwt.SvgRootDocumentMapper;
import jetbrains.jetpad.projectional.view.toAwt.AwtViewDemo;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.AbstractJSVGComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AwtDemo {
  public static void main(String[] args) {
    AwtViewDemo.show(DemoModel.demoViewContainer());
//    noViewDemo();
  }

  private static void noViewDemo() {
    JFrame frame = new JFrame("Svg Awt Demo");
    frame.setLayout(new BorderLayout());

    JSVGCanvas svgCanvas = new JSVGCanvas();
    svgCanvas.setDocumentState(AbstractJSVGComponent.ALWAYS_DYNAMIC);

    final SvgSvgElement svgRoot = DemoModel.createModel();
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