package jetbrains.jetpad.hybrid;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.hybrid.testapp.mapper.Tokens;
import jetbrains.jetpad.hybrid.testapp.model.Expr;
import jetbrains.jetpad.hybrid.testapp.model.PlusExpr;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.util.RootController;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public abstract class BaseHybridEditorTest<ContainerT, MapperT extends Mapper<ContainerT, ? extends Cell>> extends BaseTestCase {
  private Registration registration;
  protected ContainerT container;
  protected MapperT mapper;
  protected BaseHybridSynchronizer<Expr, ?> sync;
  protected Cell myTargetCell;
  protected CellContainer cellContainer;

  @Before
  public void setUp() {
    cellContainer = new CellContainer();
    registration = RootController.install(cellContainer);
    container = createContainer();
  }

  protected abstract ContainerT createContainer();
  protected abstract MapperT createMapper();
  protected abstract BaseHybridSynchronizer<Expr, ?> getSync(MapperT mapper);
  protected abstract Expr getExpr();

  @After
  public void dispose() {
    registration.remove();
    mapper.detachRoot();
  }

  @Test
  public void testCellMapping() {
    initEditor();
    sync.tokens().addAll(Arrays.asList(Tokens.ID, Tokens.PLUS, Tokens.ID));

    PlusExpr plus = (PlusExpr)getExpr();
    Assert.assertFalse(sync.getCells(plus).isEmpty());
    Assert.assertFalse(sync.getCells(plus.left.get()).isEmpty());
    Assert.assertFalse(sync.getCells(plus.right.get()).isEmpty());

    for (Cell c : sync.tokenCells()) {
      Assert.assertNotNull(sync.getSource(c));
    }
  }

  protected void initEditor() {
    mapper = createMapper();
    mapper.attachRoot();
    myTargetCell = mapper.getTarget();
    cellContainer.root.children().add(myTargetCell);
    sync = getSync(mapper);
  }
}
