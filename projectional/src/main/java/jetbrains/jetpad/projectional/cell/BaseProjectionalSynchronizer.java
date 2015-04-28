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
package jetbrains.jetpad.projectional.cell;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Supplier;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.Validators;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.completion.Completion;
import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.cell.position.Positions;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.cell.trait.CellTraits;
import jetbrains.jetpad.cell.trait.DerivedCellTrait;
import jetbrains.jetpad.cell.util.CellFactory;
import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.event.*;
import jetbrains.jetpad.mapper.*;
import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.event.CompositeRegistration;
import jetbrains.jetpad.projectional.generic.EmptyRoleCompletion;
import jetbrains.jetpad.projectional.generic.Role;
import jetbrains.jetpad.projectional.generic.RoleCompletion;

import java.util.*;

abstract class BaseProjectionalSynchronizer<SourceT, ContextT, SourceItemT> implements ProjectionalRoleSynchronizer<ContextT, SourceItemT> {
  private RoleSynchronizer<SourceItemT, Cell> myRoleSynchronizer;
  private SelectionSupport<SourceItemT> mySelectionSupport;
  private ObservableList<SourceItemT> mySelectedItems = new ObservableArrayList<>();
  private Mapper<? extends ContextT, ? extends Cell> myMapper;
  private Cell myTarget;
  private String myPlaceholderText;
  private TargetCellList myTargetCellList;
  private Supplier<SourceItemT> myItemFactory;
  private RoleCompletion<? super ContextT, SourceItemT> myCompletion = new EmptyRoleCompletion<>();
  private DeleteHandler myDeleteHandler = DeleteHandler.EMPTY;
  private ContentKind<SourceItemT> myItemKind;
  private Function<SourceItemT, SourceItemT> myCloner;
  private Runnable myOnLastItemDeleted;
  private List<Cell> myTargetList;
  private List<Registration> myRegistrations;
  private Character mySeparatorChar;

  BaseProjectionalSynchronizer(
      Mapper<? extends ContextT, ? extends Cell> mapper,
      SourceT source,
      Cell target,
      List<Cell> targetList,
      MapperFactory<SourceItemT, Cell> factory) {
    myMapper = mapper;
    myTarget = target;
    myTargetList = targetList;
    myRegistrations = new ArrayList<>(0);
    myTargetCellList = new TargetCellList();
    myRoleSynchronizer = createSubSynchronizer(myMapper, source, myTargetCellList, factory);

    mySelectionSupport = new SelectionSupport<>(new SourceList(), myTarget, myTargetList);
    mySelectedItems = mySelectionSupport.selection();

    mySelectedItems.addListener(new CollectionAdapter<SourceItemT>() {
      @Override
      public void onItemAdded(CollectionItemEvent<? extends SourceItemT> event) {
        getChildCells().get(BaseProjectionalSynchronizer.this.indexOf(event.getItem())).selected().set(true);
      }

      @Override
      public void onItemRemoved(CollectionItemEvent<? extends SourceItemT> event) {
        int itemIndex = BaseProjectionalSynchronizer.this.indexOf(event.getItem());
        if (itemIndex == -1) return;
        getChildCells().get(itemIndex).selected().set(false);
      }
    });
  }

  /**
   * This method is called from a constructor. Do not acess any fields of your class from here
   */
  protected abstract RoleSynchronizer<SourceItemT, Cell> createSubSynchronizer(Mapper<?, ?> mapper, SourceT source, List<Cell> target, MapperFactory<SourceItemT, Cell> factory);

  protected abstract Registration doRegisterChild(SourceItemT child, Cell childCell);

  protected abstract void clear(List<SourceItemT> items);

  protected abstract Runnable insertItems(List<SourceItemT> items);

  protected Runnable insertItem(SourceItemT item) {
    return insertItems(Arrays.asList(item));
  }

  private Registration registerChild(SourceItemT child, Cell childCell) {
    return new CompositeRegistration(
      CellTraits.captureTo(childCell, new CellTrait() {
        @Override
        public void onKeyPressed(Cell cell, KeyEvent event) {
          if (!getSelectedItems().isEmpty() && isDeleteEvent(event)) {
            clear(getSelectedItems());
            scrollToSelection();
            event.consume();
          }

          super.onKeyPressed(cell, event);
        }
      }),
      doRegisterChild(child, childCell)
    );
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
    final ContextT context = myMapper.getSource();
    return myCompletion.createRoleCompletion(myMapper, context, role);
  }

  protected List<Cell> getChildCells() {
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

  protected Runnable selectOnCreation(int index) {
    return getChildCells().get(index).get(ProjectionalSynchronizers.ON_CREATE);
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
  public void setOnLastItemDeleted(Runnable action) {
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

  @Override
  public void setSeparator(Character ch) {
    mySeparatorChar = ch;
  }

  Character getSeparator() {
    return mySeparatorChar;
  }

  protected Runnable getOnLastItemDeleted() {
    if (myOnLastItemDeleted == null) {
      return new Runnable() {
        @Override
        public void run() {
          myTargetCellList.getPlaceHolder().focus();
        }
      };
    }
    return myOnLastItemDeleted;
  }

  private SourceItemT currentItem() {
    Cell focused = myTarget.cellContainer().get().focusedCell.get();
    if (focused == null) return null;
    if (focused.getParent() == myTarget && !myTargetCellList.myHasPlaceholder) {
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

    myTarget.addTrait(new CellTrait() {
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
        final List<SourceItemT> copiedItems = new ArrayList<>();
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

            final List<SourceItemT> result = new ArrayList<>();
            for (SourceItemT item : new ArrayList<>(copiedItems)) {
              result.add(myCloner.apply(item));
            }
            return (T) result;
          }
        };
      }


      private List<SourceItemT> itemsToCopy() {
        final List<SourceItemT> items = new ArrayList<>();
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
          insertItems(content.get(ContentKinds.listOf(myItemKind))).run();
        } else {
          insertItem(content.get(myItemKind)).run();
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
    if (event.is(KeyStrokeSpecs.DELETE_CURRENT)) return true;

    if (event.is(Key.BACKSPACE) && !Positions.isHomePosition(cell)) return true;

    if (event.is(Key.DELETE) && !Positions.isEndPosition(cell)) return true;

    return false;
  }

  protected boolean isDeleteEvent(KeyEvent event) {
    return event.is(KeyStrokeSpecs.DELETE_CURRENT) || event.is(Key.BACKSPACE) || event.is(Key.DELETE);
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

  private void handlePlaceholderKeyPress(KeyEvent event) {
    if (canCreateNewItem() && (event.is(KeyStrokeSpecs.INSERT_AFTER) || event.is(KeyStrokeSpecs.INSERT_BEFORE))) {
      SourceItemT newItem = newItem();
      if (newItem != null) {
        insertItem(newItem).run();
        event.consume();
      }
    }

    if (isDeleteEvent(event) && myDeleteHandler.canDelete()) {
      myDeleteHandler.delete().run();
      event.consume();
    }
  }

  protected void scrollToSelection() {
    getTarget().cellContainer().get().focusedCell.get().scrollTo();
  }


  private class TargetCellList extends AbstractList<Cell> {
    private boolean myHasPlaceholder;

    private TargetCellList() {
    }

    void initList() {
      myHasPlaceholder = true;
      myTargetList.add(createPlaceholder());
    }

    private Cell createPlaceholder() {
      TextCell placeHolder = new TextCell();
      placeHolder.addTrait(new DerivedCellTrait() {
        @Override
        protected CellTrait getBase(Cell cell) {
          return TextEditing.validTextEditing(Validators.equalsTo(""));
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
            return createCompletion(new Role<SourceItemT>() {
              @Override
              public SourceItemT get() {
                return null;
              }

              @Override
              public Runnable set(SourceItemT target) {
                return insertItem(target);
              }
            });
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
      return (TextCell) myTargetList.get(0).children().get(0);
    }

    @Override
    public Cell get(int index) {
      if (myHasPlaceholder) {
        throw new IndexOutOfBoundsException();
      }
      return myTargetList.get(index);
    }

    @Override
    public int size() {
      if (myHasPlaceholder) return 0;
      return myTargetList.size();
    }

    @Override
    public void add(int index, Cell element) {
      if (myHasPlaceholder) {
        myTargetList.remove(0);
        myHasPlaceholder = false;
      }
      myTargetList.add(index, element);
      myRegistrations.add(index, registerChild(getSubMappers().get(index).getSource(), element));
    }

    @Override
    public Cell remove(int index) {
      Cell result = myTargetList.remove(index);
      myRegistrations.remove(index).remove();
      if (myTargetList.isEmpty()) {
        myTargetList.add(createPlaceholder());
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