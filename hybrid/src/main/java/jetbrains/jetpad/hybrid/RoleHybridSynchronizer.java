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
package jetbrains.jetpad.hybrid;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.Synchronizer;
import jetbrains.jetpad.mapper.SynchronizerContext;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.property.DelegateProperty;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.transform.Transformation;
import jetbrains.jetpad.model.transform.Transformers;
import jetbrains.jetpad.projectional.cell.ProjectionalRoleSynchronizer;
import jetbrains.jetpad.projectional.cell.ProjectionalSynchronizers;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class RoleHybridSynchronizer<ItemT, ContextT> implements Synchronizer {

  private final Mapper<? extends ContextT, ? extends Cell> myMapper;
  private final ObservableList<ItemT> mySourceList;
  private final Cell myTarget;
  private final List<Cell> myTargetList;
  private final RoleHybridSynchronizerSpec<ItemT> myRoleHybridSyncSpec;
  private Transformation<ObservableList<ItemT>, ObservableList<Property<ItemT>>> myTransformation;
  private ProjectionalRoleSynchronizer<ContextT, Property<ItemT>> myRoleSync;

  public RoleHybridSynchronizer(Mapper<? extends ContextT, ? extends Cell> mapper,
      ObservableList<ItemT> sourceList, Cell target,
      RoleHybridSynchronizerSpec<ItemT> roleHybridSyncSpec) {
    this(mapper, sourceList, target, target.children(), roleHybridSyncSpec);
  }

  public RoleHybridSynchronizer(Mapper<? extends ContextT, ? extends Cell> mapper,
      ObservableList<ItemT> sourceList, Cell target, List<Cell> targetList,
      RoleHybridSynchronizerSpec<ItemT> roleHybridSyncSpec) {
    myMapper = mapper;
    mySourceList = sourceList;
    myTarget = target;
    myTargetList = targetList;
    myRoleHybridSyncSpec = roleHybridSyncSpec;
  }

  @Override
  public void attach(SynchronizerContext ctx) {
    myTransformation = Transformers.<ItemT>toPropsListTwoWay().transform(mySourceList);
    myRoleSync = ProjectionalSynchronizers.forRole(myMapper, myTransformation.getTarget(), myTarget, myTargetList,
        new MapperFactory<Property<ItemT>, Cell>() {
          @Override
          public Mapper<? extends Property<ItemT>, ? extends Cell> createMapper(Property<ItemT> source) {
            return new ItemMapper(source, myRoleHybridSyncSpec.createItemTarget());
          }
        });
    myRoleHybridSyncSpec.afterRoleSynchronizerCreated(myRoleSync);
    myRoleSync.attach(ctx);
  }

  @Override
  public void detach() {
    myRoleSync.detach();
    myTransformation.dispose();
  }

  public List<HybridSynchronizer<ItemT>> getItemsSynchronizers() {
    List<HybridSynchronizer<ItemT>> syncsList = Lists.transform(myRoleSync.getMappers(),
        new Function<Mapper<? extends Property<ItemT>, ? extends Cell>, HybridSynchronizer<ItemT>>() {
          @Nullable
          @Override
          public HybridSynchronizer<ItemT> apply(@Nullable Mapper<? extends Property<ItemT>, ? extends Cell> itemMapper) {
            return ((ItemMapper) itemMapper).myHybridSync;
          }
        });
    return Collections.unmodifiableList(syncsList);
  }

  private class ItemMapper extends Mapper<Property<ItemT>, Cell> {
    private HybridSynchronizer<ItemT> myHybridSync;
    private ItemMapper(Property<ItemT> source, Cell target) {
      super(source, target);
    }
    @Override
    protected void registerSynchronizers(SynchronizersConfiguration conf) {
      super.registerSynchronizers(conf);

      Property<ItemT> proxyProperty = new ProxyPropertySubstitutingNull(getSource());
      Cell editedCell = myRoleHybridSyncSpec.findEditedInItemTarget(getTarget());
      HybridEditorSpec<ItemT> spec = myRoleHybridSyncSpec.createHybridEditorSpecForItem(getSource().get());

      myHybridSync = new HybridSynchronizer<>(this, proxyProperty, editedCell, spec);
      myRoleHybridSyncSpec.afterHybridSynchronizerCreated(myHybridSync);
      conf.add(myHybridSync);
    }
  }

  private class ProxyPropertySubstitutingNull extends DelegateProperty<ItemT> {
    public ProxyPropertySubstitutingNull(Property<ItemT> property) {
      super(property);
    }
    @Override
    public ItemT get() {
      ItemT val = super.get();
      if (myRoleHybridSyncSpec.isEmptyItem(val)) {
        return null;
      } else {
        return val;
      }
    }
    @Override
    public void set(ItemT val) {
      if (val == null) {
        super.set(myRoleHybridSyncSpec.createEmptyItem());
      } else {
        super.set(val);
      }
    }
  };
}
