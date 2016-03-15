package jetbrains.jetpad.hybrid;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.hybrid.testapp.model.Expr;
import jetbrains.jetpad.hybrid.testapp.model.ExprContainer;
import jetbrains.jetpad.hybrid.testapp.model.VarExpr;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.util.RootController;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.After;
import org.junit.Before;

public abstract class BaseHybridEditorTest<MapperT extends Mapper<ExprContainer, ? extends Cell>> extends BaseTestCase {
  protected ExprContainer container;
  protected Registration registration;
  protected MapperT mapper;
  protected BaseHybridSynchronizer<Expr, ?> sync;
  protected Cell myTargetCell;

  @Before
  public void init() {
    CellContainer cc = new CellContainer();
    registration = RootController.install(cc);

    container = new ExprContainer();
    VarExpr expr = new VarExpr();
    expr.name.set("id");
    container.expr.set(expr);

    mapper = createMapper();
    mapper.attachRoot();
    myTargetCell = mapper.getTarget();
    cc.root.children().add(myTargetCell);
    sync = getSync(mapper);
  }

  protected abstract MapperT createMapper();
  protected abstract BaseHybridSynchronizer<Expr, ?> getSync(MapperT mapper);

  @After
  public void dispose() {
    registration.remove();
    mapper.detachRoot();
  }
}
