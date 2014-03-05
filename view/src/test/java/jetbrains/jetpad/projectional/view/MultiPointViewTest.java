package jetbrains.jetpad.projectional.view;

import jetbrains.jetpad.geometry.Vector;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class MultiPointViewTest {
  @Test
  public void addRemoveWithRelativeCoordinates() {
    GroupView container = new GroupView();
    PolygonView polygonView = new PolygonView();
    container.children().add(polygonView);

    polygonView.move(new Vector(10, 10));

    polygonView.points.add(new Vector(10, 10));

    assertEquals(1, polygonView.points.size());
    assertEquals(new Vector(10, 10), polygonView.points.get(0));
  }
}
