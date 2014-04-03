package jetbrains.jetpad.projectional.view;

import jetbrains.jetpad.geometry.Vector;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EllipseViewTest {

  @Test
  public void contains() {

    EllipseView ellipse = new EllipseView();
    ellipse.radius().set(new Vector(50, 10));
    ellipse.validate();

    assertTrue(ellipse.contains(new Vector(0, 0)));
    assertTrue(ellipse.contains(new Vector(50, 0)));
    assertTrue(ellipse.contains(new Vector(0, 10)));

    assertFalse(ellipse.contains(new Vector(50, 10)));
  }
}
