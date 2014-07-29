/*
 * Copyright 2012-2014 JetBrains s.r.o
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
import jetbrains.jetpad.base.Runnables;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.event.*;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.Synchronizer;
import jetbrains.jetpad.mapper.SynchronizerContext;
import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.CollectionListener;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.collections.list.UnmodifiableObservableList;
import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.model.event.CompositeRegistration;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyBinding;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.cell.action.CellActions;
import jetbrains.jetpad.cell.completion.Completion;
import jetbrains.jetpad.cell.completion.CompletionSupport;
import jetbrains.jetpad.cell.position.Positions;
import jetbrains.jetpad.projectional.cell.*;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.cell.util.*;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.hybrid.parser.ValueToken;
import jetbrains.jetpad.hybrid.parser.prettyprint.ParseNode;
import jetbrains.jetpad.hybrid.parser.prettyprint.ParseNodes;

import java.util.*;

import static jetbrains.jetpad.hybrid.SelectionPosition.FIRST;
import static jetbrains.jetpad.hybrid.SelectionPosition.LAST;
import static jetbrains.jetpad.model.composite.Composites.firstFocusable;
import static jetbrains.jetpad.model.composite.Composites.lastFocusable;

public class HybridSynchronizer<SourceT> implements Synchronizer {
  static final CellTraitPropertySpec<HybridSynchronizer<?>> HYBRID_SYNCHRONIZER = new CellTraitPropertySpec<>("hybridSynchronizer");

  private static final ContentKind<List<Token>> TOKENS_CONTENT = new ContentKind<List<Token>>() {};

  private Mapper<?, ?> myContextMapper;
  private Property<SourceT> myProperty;
  private HybridPositionSpec<SourceT> mySpec;
  private TokenListEditor<SourceT> myTokenListEditor;
  private Registration myRegistration;
  private Cell myTarget;
  private List<Cell> myTargetList;
  private Set<Mapper<?, ? extends Cell>> myValueMappers;
  private Map<Cell, Mapper<?, ? extends Cell>> myValueCellToMapper = new HashMap<>();
  private TextCell myPlaceholder;
  private MapperFactory<Object, ? extends Cell> myMapperFactory;
  private SelectionSupport<Cell> mySelectionSupport;
  private Runnable myLastItemDeleted;
  private Synchronizer[] mySynchronizers = Synchronizer.EMPTY_ARRAY;
  private String myPlaceHolderText = "empty";

  public HybridSynchronizer(Mapper<?, ?> contextMapper, Property<SourceT> prop, Cell target, HybridPositionSpec<SourceT> spec) {
    myContextMapper = contextMapper;
    myProperty = prop;
    mySpec = spec;
    myTokenListEditor = new TokenListEditor<>(spec);
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
      protected Runnable focusAndScrollTo(final int index, boolean first) {
        return Runnables.seq(
          tokenOperations().select(index, first ? FIRST : LAST),
          new Runnable() {
            @Override
            public void run() {
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

  private CellTrait createTargetTrait() {
    return new CellTrait() {
      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == HYBRID_SYNCHRONIZER) return HybridSynchronizer.this;
        if (spec == CellStateHandler.PROPERTY) return getCellStateHandler();

        return super.get(cell, spec);
      }

      @Override
      public void onKeyPressed(Cell cell, KeyEvent event) {
        Cell focusedCell = cell.cellContainer().get().focusedCell.get();
        if (myTargetList.contains(focusedCell)) {
          Cell currentCell = cell.cellContainer().get().focusedCell.get();
          if (!hasSelection()) {
            if (event.is(KeyStrokeSpecs.SELECT_UP) && currentCell != null) {
              mySelectionSupport.select(currentCell, currentCell);
              event.consume();
            }
          } else {
            Range<Integer> currentRange = selection();
            if (event.is(KeyStrokeSpecs.SELECT_UP)) {
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

            if (event.is(KeyStrokeSpecs.SELECT_DOWN)) {
              ParseNode parseNode = myTokenListEditor.parseNode();
              if (parseNode != null) {
                ParseNode node = ParseNodes.findForRange(parseNode, currentRange);
                ParseNode childNode = ParseNodes.nonSameRangeChild(node, myTargetList.indexOf(mySelectionSupport.currentCell()));
                if (childNode != null) {
                  select(childNode.range());
                  event.consume();
                  return;
                }
              }

              if (!mySelectionSupport.isCurrentCompletelySelected()) {
                mySelectionSupport.clearSelection();
                event.consume();
              }
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
        myTokenListEditor.tokens.addAll(targetIndex, tokens);
        myTokenListEditor.updateToPrintedTokens();
        tokenOperations().select(targetIndex + tokens.size() - 1, LAST).run();
      }

      private boolean canCopy() {
        return hasSelection();
      }

      private ClipboardContent copy() {
        final Range<Integer> selection = selection();
        final List<Token> tokens = new ArrayList<>(tokens().subList(selection.lowerEndpoint(), selection.upperEndpoint()));
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

        List<Token> result = new ArrayList<>();
        for (Token t : tokens()) {
          if (t instanceof ValueToken) {
            result.add(((ValueToken) t).copy());
          } else {
            result.add(t);
          }
        }
        return result;
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
        protected CellTrait getBaseTrait(Cell cell) {
          return CompletionSupport.trait();
        }
      });

      firstFocusable(target).addTrait(new TokenCellTraits.LeftLeafTokenCellTrait());
      lastFocusable(target).addTrait(new TokenCellTraits.RightLeafTokenCellTrait());

      return target;
    }

    return new TextTokenCell(token);
  }

  private void addPlaceholder() {
    if (myPlaceholder != null) {
      throw new IllegalStateException();
    }
    myPlaceholder = createPlaceholder();
    myTargetList.add(CellFactory.horizontal(myPlaceholder, CellFactory.placeHolder(myPlaceholder, myPlaceHolderText)));
  }

  private void removePlaceholder() {
    if (myPlaceholder == null) {
      throw new IllegalStateException();
    }
    myTargetList.clear();
    myPlaceholder = null;
  }

  private TextCell createPlaceholder() {
    TextCell result = new TextCell();
    result.addTrait(new CellTrait() {
      @Override
      protected CellTrait getBaseTrait(Cell cell) {
        return TextEditing.validTextEditing(Predicates.equalTo(""));
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

  public void setTokens(List<Token> tokens) {
    myTokenListEditor.tokens.clear();
    myTokenListEditor.tokens.addAll(tokens);
    myTokenListEditor.updateToPrintedTokens();
  }

  public ObservableList<Token> tokens() {
    return new UnmodifiableObservableList<>(myTokenListEditor.tokens);
  }

  boolean hasSelection() {
    return !mySelectionSupport.selection().isEmpty();
  }

  public Range<Integer> selection() {
    ObservableList<Cell> selection = mySelectionSupport.selection();
    if (selection.isEmpty()) {
      throw new IllegalStateException();
    }
    return Range.closed(myTargetList.indexOf(selection.get(0)), myTargetList.indexOf(selection.get(selection.size() - 1)) + 1);
  }

  public void select(Range<Integer> sel) {
    mySelectionSupport.select(myTargetList.get(sel.lowerEndpoint()), myTargetList.get(sel.upperEndpoint() - 1));
  }

  public Range<Integer> rangeFor(Object object) {
    ParseNode parseNode = tokenListEditor().parseNode();
    if (parseNode == null) {
      throw new IllegalStateException("Hybrid Synchronizer is in invalid state");
    }
    ParseNode result = ParseNodes.findNodeFor(parseNode, object);
    if (result == null) {
      throw new IllegalStateException("Can't find parse node for " + object);
    }
    return result.range();
  }

  public Runnable select(int index, SelectionPosition pos) {
    return tokenOperations().select(index, pos);
  }

  public Runnable selectOnCreation(int index, SelectionPosition pos) {
    return tokenOperations().selectOnCreation(index, pos);
  }

  public void setOnLastItemDeleted(Runnable action) {
    myLastItemDeleted = action;
  }

  public int focusedIndex() {
    Cell focusedCell = myTarget.cellContainer().get().focusedCell.get();
    if (focusedCell == null) return -1;
    for (int i = 0; i < myTargetList.size(); i++) {
      if (Composites.isDescendant(myTargetList.get(i), focusedCell)) return i;
    }
    return -1;
  }

  public Object objectAt(int index) {
    if (myTokenListEditor.objects().isEmpty()) return null;
    return myTokenListEditor.objects().get(index);
  }

  public void setPlaceHolderText(String text) {
    myPlaceHolderText = text;
    if (myPlaceholder != null) {
      TextCell placeHolder = (TextCell) Composites.<Cell>nextLeaf(myPlaceholder);
      placeHolder.text().set(text);
    }
  }

  void clearSelection() {
    ObservableList<Cell> selection = mySelectionSupport.selection();
    if (selection.isEmpty()) {
      throw new IllegalStateException();
    }

    int firstIndex = myTargetList.indexOf(selection.get(0));
    int lastIndex = firstIndex + selection.size();

    mySelectionSupport.clearSelection();

    myTokenListEditor.tokens.subList(firstIndex, lastIndex).clear();

    if (tokens().isEmpty()) {
      lastItemDeleted().run();
    } else {
      boolean isEnd = tokens().size() == firstIndex;
      tokenOperations().select(!isEnd ? firstIndex : firstIndex - 1, isEnd ? LAST :  FIRST).run();
    }
  }

  Runnable lastItemDeleted() {
    if (myLastItemDeleted != null) {
      return myLastItemDeleted;
    } else {
      return CellActions.toCell(myPlaceholder);
    }
  }

  public ReadableProperty<Boolean> valid() {
    return myTokenListEditor.valid;
  }

  public List<Cell> tokenCells() {
    return Collections.unmodifiableList(myTargetList);
  }

  HybridPositionSpec<SourceT> positionSpec() {
    return mySpec;
  }

  public Cell placeholder() {
    return myPlaceholder;
  }

  public Cell target() {
    return myTarget;
  }

  Property<SourceT> property() {
    return myProperty;
  }

  Mapper<?, ?> contextMapper() {
    return myContextMapper;
  }

  TokenOperations<SourceT> tokenOperations() {
    return new TokenOperations<>(this);
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
      PropertyBinding.bindTwoWay(myProperty, myTokenListEditor.value),
      myTokenListEditor.tokens.addListener(tokensListener)
    );

    ObservableList<Token> tokens = myTokenListEditor.tokens;
    for (int i = 0; i < tokens.size(); i++) {
      Token t = tokens.get(i);
      tokensListener.onItemAdded(new CollectionItemEvent<>(t, i, true));
    }
  }

  @Override
  public void detach() {
    myRegistration.remove();
    myRegistration = null;
    for (Synchronizer sync : mySynchronizers) {
      sync.detach();
    }
  }

  private boolean isAttached() {
    return myRegistration != null;
  }

}