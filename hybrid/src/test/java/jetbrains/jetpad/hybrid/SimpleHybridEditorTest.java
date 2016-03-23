package jetbrains.jetpad.hybrid;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.message.MessageController;
import jetbrains.jetpad.hybrid.testapp.mapper.SimpleExprContainerMapper;
import jetbrains.jetpad.hybrid.testapp.mapper.Tokens;
import jetbrains.jetpad.hybrid.testapp.model.Expr;
import jetbrains.jetpad.hybrid.testapp.model.SimpleExprContainer;
import jetbrains.jetpad.projectional.cell.mapping.ToCellMapping;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class SimpleHybridEditorTest extends BaseHybridEditorTest<SimpleExprContainer, SimpleExprContainerMapper> {
  @Override
  protected SimpleExprContainer createContainer() {
    return new SimpleExprContainer();
  }

  @Override
  protected SimpleExprContainerMapper createMapper() {
    return new SimpleExprContainerMapper(container);
  }

  @Override
  protected BaseHybridSynchronizer<Expr, ?> getSync(SimpleExprContainerMapper mapper) {
    return mapper.hybridSync;
  }

  @Override
  protected Expr getExpr() {
    return container.expr.get();
  }

  @Test
  public void cellMessage() {
    initEditor();

    assertTrue(MessageController.hasError(myTargetCell));

    sync.tokens().add(Tokens.ID);
    assertFalse(MessageController.hasError(myTargetCell));

    sync.tokens().clear();
    assertTrue(MessageController.hasError(myTargetCell));
  }

  @Test
  public void cellMapping() {
    initEditor();
    sync.tokens().addAll(Arrays.asList(Tokens.ID, Tokens.PLUS, Tokens.ID));

    ToCellMapping mapping = (ToCellMapping)sync;
    assertEquals(Collections.singletonList(sync.tokenCells().get(0)), mapping.getCells("0"));
    assertEquals(Collections.singletonList(sync.tokenCells().get(1)), mapping.getCells("1"));
    assertEquals(Collections.singletonList(sync.tokenCells().get(2)), mapping.getCells("2"));

    assertTrue(mapping.getCells("3").isEmpty());

    for (Cell c : sync.tokenCells()) {
      assertNotNull(mapping.getSource(c));
    }
  }
}
