package jetbrains.jetpad.projectional.svg.toAwt;

import org.apache.batik.dom.svg.SVGOMElement;
import org.w3c.dom.Node;

import java.util.AbstractList;
import java.util.List;

public class Utils {
  public static List<Node> elementChildren(final SVGOMElement e) {
    return new AbstractList<Node>() {
      @Override
      public Node get(int index) {
        return e.getChildNodes().item(index);
      }

      @Override
      public Node set(int index, Node element) {
        if (element.getParentNode() != null) {
          throw new IllegalStateException();
        }

        Node child = get(index);
        e.replaceChild(element, child);
        return child;
      }

      @Override
      public void add(int index, Node element) {
        if (element.getParentNode() != null) {
          throw new IllegalStateException();
        }

        if (index == size()) {
          e.insertBefore(element, null);
        } else {
          e.insertBefore(element, get(index));
        }
      }

      @Override
      public Node remove(int index) {
        Node child = get(index);
        e.removeChild(child);
        return child;
      }

      @Override
      public int size() {
        return e.getChildNodes().getLength();
      }
    };
  }
}
