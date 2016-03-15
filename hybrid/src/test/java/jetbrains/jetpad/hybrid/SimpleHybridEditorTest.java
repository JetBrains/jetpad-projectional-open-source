package jetbrains.jetpad.hybrid;

import jetbrains.jetpad.cell.message.MessageController;
import jetbrains.jetpad.hybrid.testapp.mapper.SimpleExprContainerMapper;
import jetbrains.jetpad.hybrid.testapp.model.Expr;
import jetbrains.jetpad.hybrid.testapp.model.SimpleExprContainer;
import org.junit.Test;

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

  @Test
  public void cellMessage() {
    assertFalse(MessageController.hasError(myTargetCell));
    // TODO DP-920
//    mapper.getSource().expr.set(null);
    assertTrue(MessageController.hasError(myTargetCell));

//    mapper.getSource().expr.set(new IdExpr());
    assertFalse(MessageController.hasError(myTargetCell));
  }
}
