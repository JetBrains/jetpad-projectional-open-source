package jetbrains.jetpad.projectional.svg.toDom;

import org.vectomatic.dom.svg.OMElement;
import org.vectomatic.dom.svg.OMNode;

import java.util.AbstractList;
import java.util.List;

public class Utils {
  public static List<OMNode> elementChildren(final OMElement e) {
    return new AbstractList<OMNode>() {
      @Override
      public OMNode get(int index) {
        return e.getChildNodes().getItem(index);
      }

      @Override
      public OMNode set(int index, OMNode element) {
        if (element.getParentNode() != null) {
          throw new IllegalStateException();
        }

        OMNode child = get(index);
        e.replaceChild(child, element);
        return child;
      }

      @Override
      public void add(int index, OMNode element) {
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
      public OMNode remove(int index) {
        OMNode child = get(index);
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
