package jetbrains.jetpad.projectional.generic;

import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.mapper.Mapper;

public final class EmptyRoleCompletion<ContextT, TargetT> implements RoleCompletion<ContextT, TargetT> {
  @Override
  public CompletionSupplier createRoleCompletion(Mapper<?, ?> mapper, ContextT contextNode, Role<TargetT> target) {
    return CompletionSupplier.EMPTY;
  }
}
