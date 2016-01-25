/*
 * Copyright 2012-2016 JetBrains s.r.o
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

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.HorizontalCell;
import jetbrains.jetpad.cell.VerticalCell;
import jetbrains.jetpad.hybrid.HybridSynchronizer;
import jetbrains.jetpad.hybrid.testapp.model.EmptyExpr;
import jetbrains.jetpad.hybrid.testapp.model.Expr;
import jetbrains.jetpad.hybrid.testapp.model.ExprList;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.MappingContext;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.property.Properties;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.model.transform.Transformation;
import jetbrains.jetpad.model.transform.Transformers;
import jetbrains.jetpad.projectional.cell.ProjectionalRoleSynchronizer;
import jetbrains.jetpad.projectional.cell.ProjectionalSynchronizers;

import javax.annotation.Nullable;
import java.util.List;

public class ExprListMapper extends Mapper<ExprList, Cell> {
  private Transformation<ObservableList<Expr>, ObservableList<Property<Expr>>> myTransformation;
  private ProjectionalRoleSynchronizer<ExprList, Property<Expr>> myRoleSync;

  public ExprListMapper(ExprList source) {
    super(source, new VerticalCell());
  }

  @Override
  protected void onBeforeAttach(MappingContext ctx) {
    super.onBeforeAttach(ctx);
    myTransformation = Transformers.<Expr>toPropsListTwoWay().transform(getSource().expr);
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);
    myRoleSync = ProjectionalSynchronizers.forRole(this, myTransformation.getTarget(), getTarget(), new MapperFactory<Property<Expr>, Cell>() {
      @Override
      public Mapper<? extends Property<Expr>, ? extends Cell> createMapper(Property<Expr> exprAsProperty) {
        return new ExprMapper(exprAsProperty);
      }
    });
    myRoleSync.setItemFactory(new Supplier<Property<Expr>>() {
      @Override
      public Property<Expr> get() {
        return new ValueProperty<Expr>(new EmptyExpr());
      }
    });
    conf.add(myRoleSync);
  }

  @Override
  protected void onDetach() {
    myTransformation.dispose();
    super.onDetach();
  }

  public List<HybridSynchronizer<Expr>> getItemsSynchronizers() {
    return Lists.transform(myRoleSync.getMappers(),
        new Function<Mapper<? extends Property<Expr>, ? extends Cell>, HybridSynchronizer<Expr>>() {
          @Nullable
          @Override
          public HybridSynchronizer<Expr> apply(@Nullable Mapper<? extends Property<Expr>, ? extends Cell> exprPropMapper) {
            return ((ExprMapper) exprPropMapper).myHybridSync;
          }
        });
  }

  private static class ExprMapper extends Mapper<Property<Expr>, Cell> {
    private HybridSynchronizer<Expr> myHybridSync;

    public ExprMapper(Property<Expr> source) {
      super(source, new HorizontalCell());
    }

    @Override
    public void registerSynchronizers(SynchronizersConfiguration conf) {
      super.registerSynchronizers(conf);
      Property<Expr> nullSubstituting = Properties.map(getSource(),
          new Function<Expr, Expr>() {
            @Nullable
            @Override
            public Expr apply(@Nullable Expr expr) {
              return expr instanceof EmptyExpr ? null : expr;
            }
          }, new Function<Expr, Expr>() {
            @Nullable
            @Override
            public Expr apply(@Nullable Expr expr) {
              return expr == null ? new EmptyExpr() : expr;
            }
          });
      myHybridSync = new HybridSynchronizer<>(this, nullSubstituting, getTarget(), new ExprHybridEditorSpec());
      myHybridSync.setMapperFactory(new ExprMapperFactory());
      conf.add(myHybridSync);
    }
  }
}