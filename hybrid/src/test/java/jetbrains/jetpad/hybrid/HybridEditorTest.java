package jetbrains.jetpad.hybrid;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.hybrid.parser.IdentifierToken;
import jetbrains.jetpad.hybrid.testapp.mapper.ExprContainerMapper;
import jetbrains.jetpad.hybrid.testapp.model.Expr;
import jetbrains.jetpad.hybrid.testapp.model.ExprContainer;
import jetbrains.jetpad.hybrid.testapp.model.VarExpr;
import jetbrains.jetpad.projectional.util.RootController;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class HybridEditorTest extends BaseTestCase {
  private ExprContainer container;
  private Registration registration;
  private ExprContainerMapper mapper;
  private HybridSynchronizer<Expr> sync;
  private Cell myTargetCell;

  @Before
  public void init() {
    CellContainer cc = new CellContainer();
    registration = RootController.install(cc);

    container = new ExprContainer();
    VarExpr expr = new VarExpr();
    expr.name.set("id");
    container.expr.set(expr);

    mapper = new ExprContainerMapper(container);
    mapper.attachRoot();
    myTargetCell = mapper.getTarget();
    cc.root.children().add(myTargetCell);
    sync = mapper.hybridSync;
  }

  @After
  public void dispose() {
    registration.remove();
  }

  @Test
  public void initial() {
    assertEquals(1, myTargetCell.children().size());
    assertEquals(Arrays.asList(new IdentifierToken("id")), sync.tokens());
  }
}
