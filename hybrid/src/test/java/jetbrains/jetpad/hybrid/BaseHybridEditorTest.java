package jetbrains.jetpad.hybrid;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.message.MessageController;
import jetbrains.jetpad.hybrid.parser.CommentToken;
import jetbrains.jetpad.hybrid.testapp.mapper.Tokens;
import jetbrains.jetpad.hybrid.testapp.model.Expr;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.util.RootController;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
  public void validity() {
    initEditor();
    assertTrue(sync.valid().get());

    sync.tokens().add(Tokens.PLUS);
    assertFalse(sync.valid().get());

    sync.tokens().clear();
    assertTrue(sync.valid().get());

    sync.tokens().add(Tokens.ID);
    assertTrue(sync.valid().get());
  }

  @Test
  public void commentValidity() {
    initEditor();
    assertTrue(sync.valid().get());
    sync.tokens().add(new CommentToken("#", "comment"));

    assertTrue(sync.valid().get());
  }

  @Test
  public void cellMessage() {
    initEditor();
    assertFalse(MessageController.hasError(myTargetCell));

    sync.tokens().add(Tokens.PLUS);
    assertTrue(MessageController.hasError(myTargetCell));

    sync.tokens().clear();
    assertFalse(MessageController.hasError(myTargetCell));

    sync.tokens().add(Tokens.ID);
    assertFalse(MessageController.hasError(myTargetCell));
  }

  protected void initEditor() {
    mapper = createMapper();
    mapper.attachRoot();
    myTargetCell = mapper.getTarget();
    cellContainer.root.children().add(myTargetCell);
    sync = getSync(mapper);
  }
}
