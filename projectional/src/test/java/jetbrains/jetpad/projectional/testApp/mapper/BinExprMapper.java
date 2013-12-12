/*
 * Copyright 2012-2013 JetBrains s.r.o
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.jetpad.projectional.testApp.mapper;

import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.action.CellAction;
import jetbrains.jetpad.cell.action.CellActions;
import jetbrains.jetpad.projectional.cell.DeleteHandler;
import jetbrains.jetpad.projectional.cell.ProjectionalRoleSynchronizer;
import jetbrains.jetpad.projectional.testApp.model.BinExpr;
import jetbrains.jetpad.projectional.testApp.model.Expr;
import jetbrains.jetpad.projectional.testApp.model.ExprNode;

public class BinExprMapper extends Mapper<BinExpr, BinExprCell> {
  BinExprMapper(String sign, BinExpr source) {
    super(source, new BinExprCell(sign));
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    ProjectionalRoleSynchronizer<ExprNode, Expr> leftSync = ExprSynchronizers.forSingleRole(this, getSource().left, getTarget().left);

    leftSync.setDeleteHandler(new DeleteHandler() {
      @Override
      public boolean canDelete() {
        return true;
      }

      @Override
      public CellAction delete() {
        Expr right = getSource().right.get();
        right.removeFromParent();
        Mapper<?, ?> parentMapper = getParent();
        getSource().replaceWith(right);
        Mapper<? super Expr, ? extends Cell> rightMapper = (Mapper<? super Expr, ? extends Cell>) parentMapper.getDescendantMapper(right);
        return CellActions.toFirstFocusable(rightMapper.getTarget());
      }
    });
    conf.add(leftSync);

    ProjectionalRoleSynchronizer<ExprNode, Expr> rightSync = ExprSynchronizers.forSingleRole(this, getSource().right, getTarget().right);
    rightSync.setDeleteHandler(new DeleteHandler() {
      @Override
      public boolean canDelete() {
        return true;
      }

      @Override
      public CellAction delete() {
        Expr left = getSource().left.get();
        left.removeFromParent();
        Mapper<?, ?> parentMapper = getParent();
        getSource().replaceWith(left);
        Mapper<? super Expr, ? extends Cell> leftMapper = (Mapper<? super Expr, ? extends Cell>) parentMapper.getDescendantMapper(left);
        return CellActions.toLastFocusable(leftMapper.getTarget());
      }
    });
    conf.add(rightSync);
  }
}