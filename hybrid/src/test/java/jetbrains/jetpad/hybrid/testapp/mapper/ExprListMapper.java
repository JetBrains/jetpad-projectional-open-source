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
import jetbrains.jetpad.mapper.RoleSynchronizer;
import jetbrains.jetpad.mapper.RoleToPropertyAdapter;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.property.NullSubstitutingProperty;
import jetbrains.jetpad.model.property.NullSubstitutionSpec;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.projectional.cell.ProjectionalRoleSynchronizer;
import jetbrains.jetpad.projectional.cell.ProjectionalSynchronizers;

import javax.annotation.Nullable;
import java.util.List;

public class ExprListMapper extends Mapper<ExprList, Cell> {

  private RoleToPropertyAdapter<Expr, Cell> myRoleAdapter;

  public ExprListMapper(ExprList source) {
    super(source, new VerticalCell());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);
    myRoleAdapter = new RoleToPropertyAdapter<Expr, Cell>(getSource().expr) {
      @Override
      public RoleSynchronizer<Property<Expr>, Cell> createRoleSynchronizer(ObservableList<Property<Expr>> propsSourceList) {
        ProjectionalRoleSynchronizer<ExprList, Property<Expr>> roleSync = ProjectionalSynchronizers.forRole(
            ExprListMapper.this, propsSourceList, getTarget(), new MapperFactory<Property<Expr>, Cell>() {
              @Override
              public Mapper<? extends Property<Expr>, ? extends Cell> createMapper(Property<Expr> source) {
                return new ExprMapper(source);
              }
            });
        roleSync.setItemFactory(new Supplier<Property<Expr>>() {
          @Override
          public Property<Expr> get() {
            return new ValueProperty<Expr>(new EmptyExpr());
          }
        });
        return roleSync;
      }
    };
    conf.add(myRoleAdapter);
  }

  public List<HybridSynchronizer<Expr>> getItemsSynchronizers() {
    return Lists.transform(myRoleAdapter.getRoleSynchronizer().getMappers(),
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
      myHybridSync = new HybridSynchronizer<>(this,
          new NullSubstitutingProperty<>(getSource(), new NullExprSubstitutionSpec()),
          getTarget(),
          new ExprHybridEditorSpec());
      myHybridSync.setMapperFactory(new ExprMapperFactory());
      conf.add(myHybridSync);
    }
  }

  private static class NullExprSubstitutionSpec implements NullSubstitutionSpec<Expr> {
    @Override
    public Expr createSubstitute() {
      return new EmptyExpr();
    }
    @Override
    public boolean isSubstitute(Expr value) {
      return value instanceof EmptyExpr;
    }
  }
}
