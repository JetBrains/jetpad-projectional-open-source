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
      public Mapper<? extends Property<Expr>, ? extends Cell> createMapper(Property<Expr> source) {
        return new ExprMapper(source);
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
    super.onDetach();
    myTransformation.dispose();
  }

  public List<HybridSynchronizer<Expr>> getItemsSynchronizers() {
    return Lists.transform(myRoleSync.getMappers(),
        new Function<Mapper<? extends Property<Expr>, ? extends Cell>, HybridSynchronizer<Expr>>() {
          @Nullable
          @Override
          public HybridSynchronizer<Expr> apply(@Nullable Mapper<? extends Property<Expr>, ? extends Cell> itemMapper) {
            return ((ExprMapper) itemMapper).myHybridSync;
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
