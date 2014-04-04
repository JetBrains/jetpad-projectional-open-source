package jetbrains.jetpad.projectional.view;

import jetbrains.jetpad.geometry.Vector;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EllipseViewTest {

  @Test
  public void simpleContains() {

    EllipseView ellipse = new EllipseView();
    ellipse.radius().set(new Vector(50, 10));
    ellipse.validate();

    assertTrue(ellipse.contains(new Vector(0, 0)));
    assertTrue(ellipse.contains(new Vector(50, 0)));
    assertTrue(ellipse.contains(new Vector(0, 10)));

    assertFalse(ellipse.contains(new Vector(50, 10)));
  }

  @Test
  public void sectorContains() {
    EllipseView ellipse = new EllipseView();
    ellipse.radius().set(new Vector(10, 10));
    ellipse.from().set(Math.PI / 4);
    ellipse.to().set(Math.PI * 3.0 / 4.0);

    assertTrue(ellipse.contains(new Vector(0, 5)));
    assertFalse(ellipse.contains(new Vector(0, -5)));
    assertFalse(ellipse.contains(new Vector(-5, 0)));
    assertFalse(ellipse.contains(new Vector(5, 0)));
  }
}
