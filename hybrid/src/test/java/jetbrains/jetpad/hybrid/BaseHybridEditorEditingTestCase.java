package jetbrains.jetpad.hybrid;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.EditingTestCase;
import jetbrains.jetpad.cell.action.CellActions;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.hybrid.testapp.mapper.ExprContainerMapper;
import jetbrains.jetpad.hybrid.testapp.model.Expr;
import jetbrains.jetpad.hybrid.testapp.model.ExprContainer;
import jetbrains.jetpad.projectional.util.RootController;
import org.junit.After;
import org.junit.Before;

import java.util.Arrays;

import static jetbrains.jetpad.hybrid.SelectionPosition.FIRST;
import static jetbrains.jetpad.hybrid.SelectionPosition.LAST;
import static org.junit.Assert.assertEquals;

class BaseHybridEditorEditingTestCase extends EditingTestCase {
  ExprContainer container = new ExprContainer();
  ExprContainerMapper mapper = new ExprContainerMapper(container);
  HybridSynchronizer<Expr> sync;
  Cell targetCell;

  private Registration registration;

  @Before
  public void init() {
    registration = RootController.install(myCellContainer);
    mapper.attachRoot();
    myCellContainer.root.children().add(targetCell = mapper.getTarget());
    CellActions.toFirstFocusable(mapper.getTarget()).run();
    sync = mapper.hybridSync;
  }

  @After
  public void dispose() {
    mapper.detachRoot();
    registration.remove();
  }

  void setTokens(Token... tokens) {
    sync.setTokens(Arrays.asList(tokens));
  }

  void assertTokens(Token... tokens) {
    assertEquals(Arrays.asList(tokens), sync.tokens());
  }

  void select(int index, boolean first) {
    sync.tokenOperations().select(index, first ? FIRST : LAST).run();
  }

  void select(int index, int pos) {
    sync.tokenOperations().select(index, pos).run();
  }
}
