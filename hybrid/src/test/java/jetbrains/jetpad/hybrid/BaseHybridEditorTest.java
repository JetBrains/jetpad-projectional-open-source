package jetbrains.jetpad.hybrid;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.hybrid.testapp.model.Expr;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.util.RootController;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.After;
import org.junit.Before;

public abstract class BaseHybridEditorTest<ContainerT, MapperT extends Mapper<ContainerT, ? extends Cell>> extends BaseTestCase {
  protected Registration registration;
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

  @After
  public void dispose() {
    registration.remove();
    mapper.detachRoot();
  }

  protected void initEditor() {
    mapper = createMapper();
    mapper.attachRoot();
    myTargetCell = mapper.getTarget();
    cellContainer.root.children().add(myTargetCell);
    sync = getSync(mapper);
  }
}
