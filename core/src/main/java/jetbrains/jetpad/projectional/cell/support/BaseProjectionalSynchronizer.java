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
package jetbrains.jetpad.projectional.cell.support;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Supplier;
import jetbrains.jetpad.event.*;
import jetbrains.jetpad.mapper.*;
import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.event.Registration;
import jetbrains.jetpad.projectional.cell.*;
import jetbrains.jetpad.projectional.cell.action.CellAction;
import jetbrains.jetpad.projectional.cell.indent.IndentCell;
import jetbrains.jetpad.projectional.cell.trait.BaseCellTrait;
import jetbrains.jetpad.projectional.cell.trait.CellTrait;
import jetbrains.jetpad.projectional.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.projectional.cell.util.CellFactory;
import jetbrains.jetpad.projectional.util.Validators;

import java.util.*;

abstract class BaseProjectionalSynchronizer<SourceT, ContextT, SourceItemT> implements ProjectionalRoleSynchronizer<ContextT, SourceItemT> {
  private RoleSynchronizer<SourceItemT, Cell> myRoleSynchronizer;
  private SelectionSupport<SourceItemT> mySelectionSupport;
  private ObservableList<SourceItemT> mySelectedItems = new ObservableArrayList<SourceItemT>();
  private Mapper<? extends ContextT, ? extends Cell> myMapper;
  private Cell myTarget;
  private String myPlaceholderText;
  private TargetViewList myTargetCellList;
  private Supplier<SourceItemT> myItemFactory;
  private RoleCompletion<? super ContextT, SourceItemT> myCompletion;
  private DeleteHandler myDeleteHandler = DeleteHandler.EMPTY;
  private ContentKind<SourceItemT> myItemKind;
  private Function<SourceItemT, SourceItemT> myCloner;
  private CellAction myOnLastItemDeleted;
  private List<Cell> myTargetList;

  BaseProjectionalSynchronizer(
      Mapper<? extends ContextT, ? extends Cell> mapper,
      SourceT source,
      Cell target,
      List<Cell> targetList,
      MapperFactory<SourceItemT, Cell> factory) {
    myMapper = mapper;
    myTarget = target;
    myTargetList = targetList;
    myTargetCellList = new TargetViewList();
    myRoleSynchronizer = createSubSynchronizer(myMapper, source, myTargetCellList, factory);

    mySelectionSupport = new SelectionSupport<SourceItemT>(new SourceList(), myTarget, myTargetList);
    mySelectedItems = mySelectionSupport.selection();

    mySelectedItems.addListener(new CollectionAdapter<SourceItemT>() {
      @Override
      public void onItemAdded(CollectionItemEvent<SourceItemT> event) {
        childCells().get(BaseProjectionalSynchronizer.this.indexOf(event.getItem())).selected().set(true);
      }

      @Override
      public void onItemRemoved(CollectionItemEvent<SourceItemT> event) {
        int itemIndex = BaseProjectionalSynchronizer.this.indexOf(event.getItem());
        if (itemIndex == -1) return;
        childCells().get(itemIndex).selected().set(false);
      }
    });
  }

  /**
   * This method is called from a constructor. Do not acess any fields of your class from here
   */
  protected abstract RoleSynchronizer<SourceItemT, Cell> createSubSynchronizer(Mapper<?, ?> mapper, SourceT source, List<Cell> target, MapperFactory<SourceItemT, Cell> factory);

  protected abstract Registration registerChild(SourceItemT child, Cell childCell);

  protected abstract void clear(List<SourceItemT> items);

  protected abstract CellAction insertItems(List<SourceItemT> items);

  protected CellAction insertItem(SourceItemT item) {
    return insertItems(Arrays.asList(item));
  }

  protected boolean isMultiItemPasteSupported() {
    return true;
  }

  @Override
  public void setClipboardParameters(ContentKind<SourceItemT> kind, Function<SourceItemT, SourceItemT> cloner) {
    myItemKind = kind;
    myCloner = cloner;
  }

  protected Cell getTarget() {
    return myTarget;
  }

  protected boolean canCreateNewItem() {
    return myItemFactory != null;
  }

  protected SourceItemT newItem() {
    if (myItemFactory == null) {
      throw new IllegalStateException();
    }

    return myItemFactory.get();
  }

  protected CompletionSupplier createCompletion(final Role<SourceItemT> role) {
    return new CompletionSupplier() {
      @Override
      public List<CompletionItem> get(CompletionParameters cp) {
        if (myCompletion == null) return new ArrayList<CompletionItem>();
        ContextT context = myMapper.getSource();
        return myCompletion.createRoleCompletion(cp, myMapper, context, role);
      }
    };
  }

  protected List<Cell> childCells() {
    return myTargetCellList;
  }

  private List<Mapper<? extends SourceItemT, ? extends Cell>> getSubMappers() {
    return myRoleSynchronizer.getMappers();
  }

  private int indexOf(SourceItemT item) {
    List<Mapper<? extends SourceItemT, ? extends Cell>> subMappers = getSubMappers();
    for (int i = 0; i < subMappers.size(); i++) {
      Mapper<? extends SourceItemT, ? extends Cell> m = subMappers.get(i);
      if (m.getSource() == item) return i;
    }
    return -1;
  }

  private void initChildViews() {
  }

  protected CellAction selectOnCreation(int index) {
    return childCells().get(index).get(ProjectionalSynchronizers.ON_CREATE);
  }

  @Override
  public List<Mapper<? extends SourceItemT, ? extends Cell>> getMappers() {
    return myRoleSynchronizer.getMappers();
  }

  @Override
  public void addMapperFactory(MapperFactory<SourceItemT, Cell> factory) {
    myRoleSynchronizer.addMapperFactory(factory);
  }

  @Override
  public void addMapperProcessor(MapperProcessor<SourceItemT, Cell> processor) {
    myRoleSynchronizer.addMapperProcessor(processor);
  }

  @Override
  public void setOnLastItemDeleted(CellAction action) {
    myOnLastItemDeleted = action;
  }

  @Override
  public void setPlaceholderText(String text) {
    myPlaceholderText = text;
  }

  @Override
  public void setItemFactory(Supplier<SourceItemT> itemFactory) {
    myItemFactory = itemFactory;
  }

  protected CellAction getOnLastItemDeleted() {
    if (myOnLastItemDeleted == null) {
      return new CellAction() {
        @Override
        public void execute() {
          myTargetCellList.getPlaceHolder().focus();
        }
      };
    }
    return myOnLastItemDeleted;
  }

  private SourceItemT currentItem() {
    Cell focused = myTarget.container().focusedCell.get();
    if (focused == null) return null;
    if (focused.parent().get() == myTarget && !myTargetCellList.myHasPlaceholder) {
      int index = myTargetList.indexOf(focused);
      return myRoleSynchronizer.getMappers().get(index).getSource();
    } else {
      return null;
    }
  }

  @Override
  public void attach(SynchronizerContext ctx) {
    myTargetCellList.initList();

    initChildViews();

    myRoleSynchronizer.attach(ctx);

    myTarget.addTrait(new BaseCellTrait() {
      @Override
      public void onCopy(Cell cell, CopyCutEvent event) {
        if (canCopy()) {
          event.consume(copy());
          return;
        }
        super.onCopy(cell, event);
      }

      @Override
      public void onCut(Cell cell, CopyCutEvent event) {
        if (canCut()) {
          event.consume(cut());
          return;
        }
        super.onCut(cell, event);
      }

      @Override
      public void onPaste(Cell cell, PasteEvent event) {
        if (canPaste(event.getContent())) {
          paste(event.getContent());
          event.consume();
          return;
        }
        super.onPaste(cell, event);
      }

      private boolean canCopy() {
        if (myItemKind == null) return false;
        if (!getSelectedItems().isEmpty()) return true;
        return currentItem() != null;
      }

      private ClipboardContent copy() {
        return createClipboardContent(itemsToCopy());
      }

      private ClipboardContent createClipboardContent(List<SourceItemT> items) {
        final List<SourceItemT> copiedItems = new ArrayList<SourceItemT>();
        for (SourceItemT item : items) {
          copiedItems.add(myCloner.apply(item));
        }
        return new ClipboardContent() {

          @Override
          public boolean isSupported(ContentKind<?> kind) {
            if (Objects.equal(kind, myItemKind)) {
              return copiedItems.size() <= 1;
            }

            return Objects.equal(kind, ContentKinds.listOf(myItemKind));
          }

          @Override
          public <T> T get(ContentKind<T> kind) {
            if (Objects.equal(kind, myItemKind)) {
              return (T) myCloner.apply(copiedItems.get(0));
            }

            final List<SourceItemT> result = new ArrayList<SourceItemT>();
            for (SourceItemT item : new ArrayList<SourceItemT>(copiedItems)) {
              result.add(myCloner.apply(item));
            }
            return (T) result;
          }
        };
      }


      private List<SourceItemT> itemsToCopy() {
        final List<SourceItemT> items = new ArrayList<SourceItemT>();
        if (!getSelectedItems().isEmpty()) {
          for (SourceItemT item : getSelectedItems()) {
            items.add(item);
          }
        } else {
          items.add(currentItem());
        }
        return items;
      }

      private boolean canCut() {
        return canCopy();
      }

      private ClipboardContent cut() {
        List<SourceItemT> toCopy = itemsToCopy();
        ClipboardContent result = createClipboardContent(toCopy);
        clear(toCopy);
        return result;
      }

      private boolean canPaste(ClipboardContent content) {
        if (myItemKind == null) return false;
        if (content.isSupported(myItemKind)) return true;
        return isMultiItemPasteSupported() && content.isSupported(ContentKinds.listOf(myItemKind));
      }

      private void paste(ClipboardContent content) {
        if (isMultiItemPasteSupported() && content.isSupported(ContentKinds.listOf(myItemKind))) {
          insertItems(content.get(ContentKinds.listOf(myItemKind))).execute();
        } else {
          insertItem(content.get(myItemKind)).execute();
        }
      }
    });
  }

  @Override
  public void detach() {
    myRoleSynchronizer.detach();
  }

  @Override
  public void setCompletion(RoleCompletion<? super ContextT, SourceItemT> completion) {
    myCompletion = completion;
  }

  @Override
  public void setDeleteHandler(DeleteHandler handler) {
    myDeleteHandler = handler;
  }

  @Override
  public List<SourceItemT> getSelectedItems() {
    return Collections.unmodifiableList(mySelectedItems);
  }

  @Override
  public void select(SourceItemT from, SourceItemT to) {
    mySelectionSupport.select(from, to);
  }

  protected boolean isDeleteEvent(KeyEvent event, Cell cell) {
    if (isAnyPositionDeleteEvent(event)) return true;

    if (event.is(Key.BACKSPACE) && !Positions.isHomePosition(cell)) return true;

    if (event.is(Key.DELETE) && !Positions.isEndPosition(cell)) return true;

    return false;
  }

  protected boolean isDeleteEvent(KeyEvent event) {
    return isAnyPositionDeleteEvent(event) || event.is(Key.BACKSPACE) || event.is(Key.DELETE);
  }

  private boolean isAnyPositionDeleteEvent(KeyEvent event) {
    return event.is(Key.BACKSPACE, ModifierKey.META) || event.is(Key.DELETE, ModifierKey.META) ||
      event.is(Key.DELETE, ModifierKey.CONTROL) || event.is(Key.BACKSPACE, ModifierKey.CONTROL);
  }

  protected Cell currentCell() {
    return mySelectionSupport.currentCell();
  }

  @Override
  public SourceItemT getFocusedItem() {
    Cell currentCell = currentCell();
    if (currentCell == null) return null;
    int index = myTargetCellList.indexOf(currentCell);
    return myRoleSynchronizer.getMappers().get(index).getSource();
  }

  private CompletionSupplier getPlaceholderCompletion() {
    return new CompletionSupplier() {
      @Override
      public List<CompletionItem> get(CompletionParameters cp) {
        if (myCompletion == null) return new ArrayList<CompletionItem>();
        return myCompletion.createRoleCompletion(cp, myMapper, myMapper.getSource(), new Role<SourceItemT>() {
          @Override
          public SourceItemT get() {
            return null;
          }

          @Override
          public CellAction set(SourceItemT target) {
            return insertItem(target);
          }
        });
      }
    };
  }

  private void handlePlaceholderKeyPress(KeyEvent event) {
    if (canCreateNewItem() && (event.is(Key.INSERT) || event.is(Key.ENTER) || event.is(Key.ENTER, ModifierKey.SHIFT))) {
      SourceItemT newItem = newItem();
      if (newItem != null) {
        insertItem(newItem).execute();
        event.consume();
      }
    }

    if (isDeleteEvent(event) && myDeleteHandler.canDelete()) {
      myDeleteHandler.delete().execute();
      event.consume();
    }
  }

  private class TargetViewList extends AbstractList<Cell> {
    private boolean myHasPlaceholder;

    private TargetViewList() {
    }

    void initList() {
      myHasPlaceholder = true;
      getList().add(createPlaceholder());
    }

    private Cell createPlaceholder() {
      TextCell placeHolder = new TextCell();
      placeHolder.addTrait(new BaseCellTrait() {
        @Override
        protected CellTrait[] getBaseTraits(Cell cell) {
          return new CellTrait[] { TextEditing.validTextEditing(Validators.equalsTo("")) };
        }

        @Override
        public void onKeyPressedLowPriority(Cell cell, KeyEvent event) {
          handlePlaceholderKeyPress(event);
          if (event.isConsumed()) return;

          super.onKeyPressedLowPriority(cell, event);
        }

        @Override
        public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
          if (spec == Completion.COMPLETION) {
            return getPlaceholderCompletion();
          }

          return super.get(cell, spec);
        }
      });
      String placeholderText =  myPlaceholderText != null ? myPlaceholderText : "<empty>";

      //todo this is tmp hack
      if (myTarget instanceof IndentCell) {
        return CellFactory.indent(placeHolder, CellFactory.placeHolder(placeHolder, placeholderText));
      } else {
        return CellFactory.horizontal(placeHolder, CellFactory.placeHolder(placeHolder, placeholderText));
      }
    }

    private TextCell getPlaceHolder() {
      if (!myHasPlaceholder) return null;
      return (TextCell) getList().get(0).children().get(0);
    }

    private List<Cell> getList() {
      return myTargetList;
    }

    @Override
    public Cell get(int index) {
      if (myHasPlaceholder) throw new IndexOutOfBoundsException();
      return getList().get(index);
    }

    @Override
    public int size() {
      if (myHasPlaceholder) return 0;
      return getList().size();
    }

    @Override
    public void add(int index, Cell element) {
      if (myHasPlaceholder) {
        getList().remove(0);
        myHasPlaceholder = false;
      }
      getList().add(index, element);

      registerChild(getSubMappers().get(index).getSource(), element);
    }

    @Override
    public Cell remove(int index) {
      Cell result = getList().remove(index);
      if (getList().isEmpty()) {
        getList().add(createPlaceholder());
        myHasPlaceholder = true;
      }
      return result;
    }

    @Override
    public Cell set(int index, Cell element) {
      Cell result = remove(index);
      add(index, element);
      return result;
    }
  }

  private class SourceList extends AbstractList<SourceItemT> {
    @Override
    public SourceItemT get(int index) {
      return myRoleSynchronizer.getMappers().get(index).getSource();
    }

    @Override
    public int size() {
      return myRoleSynchronizer.getMappers().size();
    }
  }
}