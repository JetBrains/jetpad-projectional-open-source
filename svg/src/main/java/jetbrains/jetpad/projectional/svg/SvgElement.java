package jetbrains.jetpad.projectional.svg;

import jetbrains.jetpad.model.children.ChildList;
import jetbrains.jetpad.model.children.HasParent;
import jetbrains.jetpad.model.collections.list.ObservableList;

public class SvgElement extends HasParent<SvgElement, SvgElement> {
  public final ObservableList<SvgElement> elements = new ChildList<>(this);
}
