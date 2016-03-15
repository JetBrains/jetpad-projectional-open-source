package jetbrains.jetpad.hybrid;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.hybrid.testapp.model.Expr;
import jetbrains.jetpad.hybrid.testapp.model.VarExpr;
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

  @Before
  public void init() {
    CellContainer cc = new CellContainer();
    registration = RootController.install(cc);

    container = createContainer();
    VarExpr expr = new VarExpr();
    // TODO DP-920
    expr.name.set("id");
//    container.expr.set(expr);

    mapper = createMapper();
    mapper.attachRoot();
    myTargetCell = mapper.getTarget();
    cc.root.children().add(myTargetCell);
    sync = getSync(mapper);
  }

  protected abstract ContainerT createContainer();
  protected abstract MapperT createMapper();
  protected abstract BaseHybridSynchronizer<Expr, ?> getSync(MapperT mapper);

  @After
  public void dispose() {
    registration.remove();
    mapper.detachRoot();
  }
}
