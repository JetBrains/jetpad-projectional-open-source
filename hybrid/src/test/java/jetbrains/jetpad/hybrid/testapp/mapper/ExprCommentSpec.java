package jetbrains.jetpad.hybrid.testapp.mapper;

import jetbrains.jetpad.hybrid.CommentSpec;

final class ExprCommentSpec implements CommentSpec {

  ExprCommentSpec() {
  }

  @Override
  public String getCommentPrefix() {
    return "#";
  }

}
