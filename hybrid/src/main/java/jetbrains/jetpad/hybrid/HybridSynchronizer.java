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
package jetbrains.jetpad.hybrid;

import com.google.common.base.Predicates;
import com.google.common.collect.Range;
import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.event.*;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.Synchronizer;
import jetbrains.jetpad.mapper.SynchronizerContext;
import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.CollectionListener;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.event.CompositeRegistration;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.Registration;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyBinding;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.cell.action.CellAction;
import jetbrains.jetpad.cell.action.CellActions;
import jetbrains.jetpad.cell.completion.Completion;
import jetbrains.jetpad.cell.completion.CompletionSupport;
import jetbrains.jetpad.cell.position.Positions;
import jetbrains.jetpad.projectional.cell.*;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.cell.trait.BaseCellTrait;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.cell.util.*;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.hybrid.parser.ValueToken;
import jetbrains.jetpad.hybrid.parser.prettyprint.ParseNode;
import jetbrains.jetpad.hybrid.parser.prettyprint.ParseNodes;
import jetbrains.jetpad.values.Color;

import java.util.*;

import static jetbrains.jetpad.hybrid.SelectionPosition.FIRST;
import static jetbrains.jetpad.hybrid.SelectionPosition.LAST;
import static jetbrains.jetpad.model.composite.Composites.firstFocusableLeaf;
import static jetbrains.jetpad.model.composite.Composites.lastFocusableLeaf;

public class HybridSynchronizer<SourceT> implements Synchronizer {
  private static final ContentKind<List<Token>> TOKENS_CONTENT = new ContentKind<List<Token>>() {
  };

  static final CellTraitPropertySpec<HybridSynchronizer<?>> HYBRID_SYNCHRONIZER = new CellTraitPropertySpec<HybridSynchronizer<?>>("hybridSynchronizer");

  private Mapper<?, ?> myContextMapper;
  private Property<SourceT> myProperty;
  private HybridPositionSpec<SourceT> myPositionSpec;
  private TokenListEditor<SourceT> myTokenListEditor;
  private Registration myRegistration;
  private Cell myTarget;
  private List<Cell> myTargetList;
  private Set<Mapper<?, ? extends Cell>> myValueMappers;
  private Map<Cell, Mapper<?, ? extends Cell>> myValueCellToMapper = new HashMap<Cell, Mapper<?, ? extends Cell>>();
  private TextCell myPlaceholder;
  private MapperFactory<Object, ? extends Cell> myMapperFactory;
  private SelectionSupport<Cell> mySelectionSupport;
  private CellAction myLastItemDeleted;
  private Synchronizer[] mySynchronizers = Synchronizer.EMPTY_ARRAY;

  public HybridSynchronizer(Mapper<?, ?> contextMapper, Property<SourceT> prop, Cell target, HybridPositionSpec<SourceT> spec) {
    myContextMapper = contextMapper;
    myProperty = prop;
    myPositionSpec = spec;
    myTokenListEditor = new TokenListEditor<SourceT>(spec);
    myTarget = target;

    myValueMappers = myContextMapper.createChildSet();

    myTargetList = new SeparatedCellList(myTarget.children()) {
      @Override
      protected Cell createSeparator(Cell left, Cell right) {
        if (left.get(CellLists.NO_SPACE_TO_RIGHT) || right.get(CellLists.NO_SPACE_TO_LEFT)) {
          return new TextCell("");
        }

        return new TextCell(" ");
      }
    };
    myTarget.addTrait(createTargetTrait());

    addPlaceholder();

    myTokenListEditor.tokens.addHandler(new EventHandler<CollectionItemEvent<Token>>() {
      @Override
      public void onEvent(CollectionItemEvent<Token> event) {
        mySelectionSupport.clearSelection();
      }
    });

    mySelectionSupport = new SelectionSupport<Cell>(myTargetList, myTarget, myTargetList) {
      @Override
      public Cell currentCell() {
        if (placeholder() != null) return null;
        return super.currentCell();
      }

      @Override
      protected CellAction focusAndScrollTo(final int index, boolean first) {
        return CellActions.seq(
          tokenOperations().select(index, first ? FIRST : LAST),
          new CellAction() {
            @Override
            public void execute() {
              tokenCells().get(index).scrollTo();
            }
          }
        );
      }
    };
    final ObservableList<Cell> selection = mySelectionSupport.selection();
    selection.addListener(new CollectionAdapter<Cell>() {
      @Override
      public void onItemAdded(CollectionItemEvent<Cell> event) {
        event.getItem().selected().set(true);

        int index = myTargetList.indexOf(event.getItem());
        if (selection.size() > 1) {
          if (event.getIndex() == 0) {
            myTarget.children().get(index * 2 + 1).selected().set(true);
          } else { //last
            myTarget.children().get(index * 2 - 1).selected().set(true);
          }
        }
      }

      @Override
      public void onItemRemoved(CollectionItemEvent<Cell> event) {
        event.getItem().selected().set(false);

        int index = myTargetList.indexOf(event.getItem());
        if (index == -1) {
          throw new IllegalStateException();
        }

        if (!selection.isEmpty()) {
          if (event.getIndex() == 0) {
            myTarget.children().get(index * 2 + 1).selected().set(false);
          } else {
            myTarget.children().get(index * 2 - 1).selected().set(false);
          }
        }
      }
    });
  }

  private BaseCellTrait createTargetTrait() {
    return new BaseCellTrait() {
      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == HYBRID_SYNCHRONIZER) return HybridSynchronizer.this;
        if (spec == CellStateHandler.PROPERTY) return getCellStateHandler();

        return super.get(cell, spec);
      }


      @Override
      public void onKeyPressed(Cell cell, KeyEvent event) {
        if (!hasSelection()) {
          Cell currentCell = mySelectionSupport.currentCell();
          if (event.is(Key.UP, ModifierKey.ALT) && currentCell != null) {
            mySelectionSupport.select(currentCell, currentCell);
            event.consume();
          }
        } else {
          Range<Integer> currentRange = selection();
          if (event.is(Key.UP, ModifierKey.ALT)) {
            ParseNode parseNode = myTokenListEditor.parseNode();
            if (parseNode != null) {
              if (!currentRange.equals(parseNode.range())) {
                ParseNode node = ParseNodes.findForRange(parseNode, currentRange);
                ParseNode parentNode = ParseNodes.nonSameRangeParent(node);
                if (parentNode != null) {
                  select(parentNode.range());
                  event.consume();
                }
              }
            } else {
              if (!currentRange.equals(Range.closed(0, tokens().size()))) {
                select(Range.closed(0, tokens().size()));
                event.consume();
              }
            }
          }

          if (event.is(Key.DOWN, ModifierKey.ALT)) {
            ParseNode parseNode = myTokenListEditor.parseNode();
            if (parseNode != null) {
              ParseNode node = ParseNodes.findForRange(parseNode, currentRange);
              ParseNode childNode = ParseNodes.nonSameRangeChild(node, myTargetList.indexOf(mySelectionSupport.currentCell()));
              if (childNode != null) {
                select(childNode.range());
                event.consume();
              } else {
                mySelectionSupport.clearSelection();
                event.consume();
              }
            } else {
              mySelectionSupport.clearSelection();
              event.consume();
            }
          }
        }
        super.onKeyPressed(cell, event);
      }

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

      private boolean canPaste(ClipboardContent content) {
        return content.isSupported(TOKENS_CONTENT);
      }

      private void paste(ClipboardContent content) {
        List<Token> tokens = content.get(TOKENS_CONTENT);
        Cell currentCell = mySelectionSupport.currentCell();
        int currentCellIndex = myTargetList.indexOf(currentCell);
        int targetIndex = Positions.isHomePosition(currentCell) ? currentCellIndex : currentCellIndex + 1;
        tokens().addAll(targetIndex, tokens);
        tokenOperations().select(targetIndex + tokens.size() - 1, LAST).execute();
      }

      private boolean canCopy() {
        return hasSelection();
      }

      private ClipboardContent copy() {
        final Range<Integer> selection = selection();
        final List<Token> tokens = new ArrayList<Token>(tokens().subList(selection.lowerEndpoint(), selection.upperEndpoint()));
        return new ClipboardContent() {
          @Override
          public boolean isSupported(ContentKind<?> kind) {
            return kind == TOKENS_CONTENT;
          }

          @Override
          public <T> T get(ContentKind<T> kind) {
            if (kind == TOKENS_CONTENT) {
              return (T) Collections.unmodifiableList(tokens);
            }
            return null;
          }
        };
      }

      private boolean canCut() {
        return hasSelection();
      }

      private ClipboardContent cut() {
        ClipboardContent result = copy();
        clearSelection();
        return result;
      }
    };
  }

  private CellStateHandler<Cell, List<Token>> getCellStateHandler() {
    return new CellStateHandler<Cell, List<Token>>() {
      @Override
      public List<Token> saveState(Cell cell) {
        if (valid().get()) return null;
        return new ArrayList<Token>(tokens());
      }

      @Override
      public void restoreState(Cell cell, List<Token> state) {
        tokenListEditor().restoreState(state);
      }
    };
  }

  private CollectionListener<Token> createTokensListener() {
    return new CollectionListener<Token>() {
      @Override
      public void onItemAdded(CollectionItemEvent<Token> event) {
        if (myPlaceholder != null) {
          removePlaceholder();
        }

        final Token token = event.getItem();
        Cell tokenCell = createTokenCell(token);

        int index = event.getIndex();
        if (index == 0) {
          if (tokenCell instanceof TextTokenCell) {
            ((TextTokenCell) tokenCell).setFirst(true);
          }
          if (!myTargetList.isEmpty()) {
            updateTokenCell(0, new Handler<TextTokenCell>() {
              @Override
              public void handle(TextTokenCell item) {
                item.setFirst(false);
              }
            });
          }
        }

        if (index > 0) {
          updateTokenCell(index - 1, new Handler<TextTokenCell>() {
            @Override
            public void handle(TextTokenCell item) {
              item.setNextToken(token);
            }
          });
        }
        if (index + 1 < tokens().size() && tokenCell instanceof TextTokenCell) {
          ((TextTokenCell) tokenCell).setNextToken(tokens().get(index + 1));
        }

        myTargetList.add(index, tokenCell);

      }

      @Override
      public void onItemRemoved(CollectionItemEvent<Token> event) {
        final int index = event.getIndex();
        Cell removedCell = myTargetList.remove(index);

        if (myValueCellToMapper.containsKey(removedCell)) {
          Mapper<?, ? extends Cell> valueMapper = myValueCellToMapper.get(removedCell);
          myValueMappers.remove(valueMapper);
          myValueCellToMapper.remove(removedCell);
        }

        if (myTargetList.isEmpty()) {
          addPlaceholder();
        } else {
          if (index == 0 && myTargetList.get(0) instanceof TextTokenCell) {
            updateTokenCell(0, new Handler<TextTokenCell>() {
              @Override
              public void handle(TextTokenCell item) {
                item.setFirst(true);
              }
            });
          }
          if (index == myTargetList.size()) {
            updateTokenCell(myTargetList.size() - 1, new Handler<TextTokenCell>() {
              @Override
              public void handle(TextTokenCell item) {
                item.setNextToken(null);
              }
            });
          } else if (index > 0 && myTargetList.get(index - 1) instanceof TextTokenCell) {
            updateTokenCell(index - 1, new Handler<TextTokenCell>() {
              @Override
              public void handle(TextTokenCell item) {
                item.setNextToken(tokens().get(index));
              }
            });
          }
        }
      }

      private void updateTokenCell(int index, Handler<TextTokenCell> handler) {
        Cell cell = myTargetList.get(index);
        if (cell instanceof TextTokenCell) {
          TextTokenCell textTokenCell = (TextTokenCell) cell;

          boolean wasNoSpaceToLeft = textTokenCell.noSpaceToLeft();
          boolean wasNoSpaceToRight = textTokenCell.noSpaceToRight();

          handler.handle(textTokenCell);

          if ((textTokenCell.noSpaceToLeft() != wasNoSpaceToLeft) || (textTokenCell.noSpaceToRight() != wasNoSpaceToRight)) {
            myTargetList.remove(index);
            myTargetList.add(index, cell);
          }
        }
      }
    };
  }

  public void setMapperFactory(MapperFactory<Object, ? extends Cell> mapperFactory) {
    myMapperFactory = mapperFactory;
  }

  public void addPart(Synchronizer sync) {
    Synchronizer[] newSynchronizers = new Synchronizer[mySynchronizers.length + 1];
    System.arraycopy(mySynchronizers, 0, newSynchronizers, 0, mySynchronizers.length);
    newSynchronizers[newSynchronizers.length - 1] = sync;
    mySynchronizers = newSynchronizers;
  }

  private Cell createTokenCell(Token token) {
    if (token instanceof ValueToken) {
      ValueToken valueToken = (ValueToken) token;
      Object value = valueToken.value();
      Mapper<?, ? extends Cell> mapper = myMapperFactory != null ? myMapperFactory.createMapper(value) : null;
      if (mapper == null) {
        mapper = new Mapper<Object, Cell>(value, CellFactory.label("[No Mapper For " + value + "]")) {};
      }

      myValueMappers.add(mapper);
      Cell target = mapper.getTarget();
      myValueCellToMapper.put(target, mapper);

      target.addTrait(new TokenCellTraits.TokenCellTrait(true) {
        @Override
        protected CellTrait[] getBaseTraits(Cell cell) {
          return new CellTrait[] { CompletionSupport.trait() };
        }
      });

      firstFocusableLeaf(target).addTrait(new TokenCellTraits.LeftLeafTokenCellTrait());
      lastFocusableLeaf(target).addTrait(new TokenCellTraits.RightLeafTokenCellTrait());

      return target;
    }

    return new TextTokenCell(token);
  }

  private void addPlaceholder() {
    if (myPlaceholder != null) throw new IllegalStateException();
    myPlaceholder = createPlaceholder();
    myTargetList.add(CellFactory.horizontal(myPlaceholder, CellFactory.placeHolder(myPlaceholder, "empty")));
  }

  private void removePlaceholder() {
    if (myPlaceholder == null) throw new IllegalStateException();
    myTargetList.clear();
    myPlaceholder = null;
  }

  private TextCell createPlaceholder() {
    TextCell result = new TextCell();
    result.addTrait(new BaseCellTrait() {
      @Override
      protected CellTrait[] getBaseTraits(Cell cell) {
        return new CellTrait[] { TextEditing.validTextEditing(Predicates.equalTo("")) };
      }

      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == TextEditing.EAGER_COMPLETION) return true;

        if (spec == Completion.COMPLETION) return tokenCompletion().placeholderCompletion();

        return super.get(cell, spec);
      }
    });
    return result;
  }

  public ObservableList<Token> tokens() {
    return myTokenListEditor.tokens;
  }

  boolean hasSelection() {
    return !mySelectionSupport.selection().isEmpty();
  }

  Range<Integer> selection() {
    ObservableList<Cell> selection = mySelectionSupport.selection();
    if (selection.isEmpty()) throw new IllegalStateException();
    return Range.closed(myTargetList.indexOf(selection.get(0)), myTargetList.indexOf(selection.get(selection.size() - 1)) + 1);
  }

  void select(Range<Integer> sel) {
    mySelectionSupport.select(myTargetList.get(sel.lowerEndpoint()), myTargetList.get(sel.upperEndpoint() - 1));
  }

  public CellAction select(int index, SelectionPosition pos) {
    return tokenOperations().select(index, pos);
  }

  public CellAction selectOnCreation(int index, SelectionPosition pos) {
    return tokenOperations().selectOnCreation(index, pos);
  }

  public void setOnLastItemDeleted(CellAction action) {
    myLastItemDeleted = action;
  }

  void clearSelection() {
    ObservableList<Cell> selection = mySelectionSupport.selection();
    if (selection.isEmpty()) throw new IllegalStateException();

    int firstIndex = myTargetList.indexOf(selection.get(0));
    int lastIndex = firstIndex + selection.size();

    mySelectionSupport.clearSelection();

    tokens().subList(firstIndex, lastIndex).clear();

    if (tokens().isEmpty()) {
      lastItemDeleted().execute();
    } else {
      boolean isEnd = tokens().size() == firstIndex;
      tokenOperations().select(!isEnd ? firstIndex : firstIndex - 1, isEnd ? LAST :  FIRST).execute();
    }
  }

  CellAction lastItemDeleted() {
    if (myLastItemDeleted != null) {
      return myLastItemDeleted;
    } else {
      return CellActions.toCell(myPlaceholder);
    }
  }

  public ReadableProperty<Boolean> valid() {
    return myTokenListEditor.valid;
  }

  List<Cell> tokenCells() {
    return Collections.unmodifiableList(myTargetList);
  }

  HybridPositionSpec<SourceT> positionSpec() {
    return myPositionSpec;
  }

  public Cell placeholder() {
    return myPlaceholder;
  }

  Cell target() {
    return myTarget;
  }

  Property<SourceT> property() {
    return myProperty;
  }

  Mapper<?, ?> contextMapper() {
    return myContextMapper;
  }

  TokenOperations<SourceT> tokenOperations() {
    return new TokenOperations<SourceT>(this);
  }

  TokenCompletion tokenCompletion() {
    return new TokenCompletion(this);
  }

  TokenListEditor<SourceT> tokenListEditor() {
    return myTokenListEditor;
  }

  @Override
  public void attach(SynchronizerContext ctx) {
    for (Synchronizer sync : mySynchronizers) {
      sync.attach(ctx);
    }

    CollectionListener<Token> tokensListener = createTokensListener();
    myRegistration = new CompositeRegistration(
      PropertyBinding.bind(myProperty, myTokenListEditor.value),
      PropertyBinding.bind(myTokenListEditor.valid, new WritableProperty<Boolean>() {
        @Override
        public void set(Boolean value) {
          myTarget.background().set(value ? null : Color.LIGHT_PINK);
        }
      }),
      myTokenListEditor.tokens.addListener(tokensListener)
    );

    ObservableList<Token> tokens = myTokenListEditor.tokens;
    for (int i = 0; i < tokens.size(); i++) {
      Token t = tokens.get(i);
      tokensListener.onItemAdded(new CollectionItemEvent<Token>(t, i, true));
    }
  }

  @Override
  public void detach() {
    myRegistration.remove();

    for (Synchronizer sync : mySynchronizers) {
      sync.detach();
    }
  }

}