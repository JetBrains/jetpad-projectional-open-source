package jetbrains.jetpad.views;

import jetbrains.jetpad.model.children.Composite;

import java.util.List;

public class Views {
  public static <ViewT extends Composite<ViewT> & HasVisibility>
  boolean isLastChild(ViewT v) {
    ViewT parent = v.parent().get();
    if (parent == null) return false;
    List<ViewT> siblings = parent.children();
    int index = siblings.indexOf(v);
    for (ViewT cv : siblings.subList(index + 1, siblings.size())) {
      if (cv.visible().get()) return false;
    }
    return true;
  }

  public static <ViewT extends Composite<ViewT> & HasVisibility>
  boolean isFirstChild(ViewT cell) {
    ViewT parent = cell.parent().get();
    if (parent == null) return false;
    List<ViewT> siblings = parent.children();
    int index = siblings.indexOf(cell);

    for (ViewT cv : siblings.subList(0, index)) {
      if (cv.visible().get()) return false;
    }
    return true;
  }

  public static <ViewT extends Composite<ViewT> & HasFocusability & HasVisibility>
  ViewT firstFocusable(ViewT v) {
    return firstFocusable(v, true);
  }

  public static <ViewT extends Composite<ViewT> & HasFocusability & HasVisibility>
  ViewT firstFocusable(ViewT v, boolean deepest) {
    for (ViewT cv : v.children()) {
      if (!cv.visible().get()) continue;
      if (!deepest && cv.focusable().get()) return cv;

      ViewT result = firstFocusable(cv);
      if (result != null) return result;
    }

    if (v.focusable().get()) return v;

    return null;
  }

  public static <ViewT extends Composite<ViewT> & HasFocusability & HasVisibility>
  ViewT lastFocusable(ViewT c) {
    return lastFocusable(c, true);
  }

  public static <ViewT extends Composite<ViewT> & HasFocusability & HasVisibility>
  ViewT lastFocusable(ViewT v, boolean deepest) {
    List<ViewT> children = v.children();
    for (int i = children.size() - 1; i >= 0; i--) {
      ViewT cv = children.get(i);

      if (!cv.visible().get()) continue;
      if (!deepest && cv.focusable().get()) return cv;

      ViewT result = lastFocusable(cv, deepest);
      if (result != null) return result;
    }

    if (v.focusable().get()) return v;
    return null;
  }
}
