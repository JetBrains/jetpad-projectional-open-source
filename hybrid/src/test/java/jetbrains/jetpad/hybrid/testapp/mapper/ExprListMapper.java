/*
 * Copyright 2012-2015 JetBrains s.r.o
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
package jetbrains.jetpad.hybrid.testapp.mapper;

import com.google.common.base.Supplier;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.HorizontalCell;
import jetbrains.jetpad.cell.VerticalCell;
import jetbrains.jetpad.hybrid.*;
import jetbrains.jetpad.hybrid.testapp.model.EmptyExpr;
import jetbrains.jetpad.hybrid.testapp.model.Expr;
import jetbrains.jetpad.hybrid.testapp.model.ExprList;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.projectional.cell.ProjectionalRoleSynchronizer;

public class ExprListMapper extends Mapper<ExprList, Cell> {
  private RoleHybridSynchronizer<Expr, ExprList> myRoleHybridSync;
  private boolean myRoleHybridSyncInitialized = false;

  public ExprListMapper(ExprList exprList) {
    super(exprList, new VerticalCell());
  }

  public RoleHybridSynchronizer<Expr, ExprList> getRoleHybridSync() {
    if (!myRoleHybridSyncInitialized) {
      throw new IllegalStateException("Not initialized yet");
    }
    return myRoleHybridSync;
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    myRoleHybridSync = new RoleHybridSynchronizer<>(
        this, getSource().expr, getTarget(), new RoleHybridSynchronizerSpec<Expr>() {
      @Override
      public Expr createEmptyItem() {
        return new EmptyExpr();
      }

      @Override
      public boolean isEmptyItem(Expr item) {
        return item instanceof EmptyExpr;
      }

      @Override
      public Cell createItemTarget() {
        return new HorizontalCell();
      }

      @Override
      public Cell findEditedInItemTarget(Cell itemTarget) {
        return itemTarget;
      }

      @Override
      public HybridEditorSpec<Expr> createHybridEditorSpecForItem(Expr item) {
        return new ExprHybridEditorSpec();
      }

      @Override
      public void afterRoleSynchronizerCreated(ProjectionalRoleSynchronizer<?, Property<Expr>> sync) {
        sync.setItemFactory(new Supplier<Property<Expr>>() {
          @Override
          public Property<Expr> get() {
            return new ValueProperty<Expr>(new EmptyExpr());
          }
        });
      }

      @Override
      public void afterHybridSynchronizerCreated(HybridSynchronizer<Expr> sync) {
        sync.setMapperFactory(new ExprMapperFactory());
      }
    });
    myRoleHybridSyncInitialized = true;
    conf.add(myRoleHybridSync);
  }
}
