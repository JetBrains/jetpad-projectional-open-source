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
package jetbrains.jetpad.hybrid;

import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.collect.Range;
import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.Runnables;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.action.CellActions;
import jetbrains.jetpad.cell.completion.Completion;
import jetbrains.jetpad.cell.completion.CompletionSupport;
import jetbrains.jetpad.cell.position.Positions;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.cell.trait.DerivedCellTrait;
import jetbrains.jetpad.cell.util.CellFactory;
import jetbrains.jetpad.cell.util.CellLists;
import jetbrains.jetpad.cell.util.CellState;
import jetbrains.jetpad.cell.util.CellStateHandler;
import jetbrains.jetpad.event.*;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.hybrid.parser.TokenUtil;
import jetbrains.jetpad.hybrid.parser.ValueToken;
import jetbrains.jetpad.hybrid.parser.prettyprint.ParseNode;
import jetbrains.jetpad.hybrid.parser.prettyprint.ParseNodes;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.Synchronizer;
import jetbrains.jetpad.mapper.SynchronizerContext;
import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.CollectionListener;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.model.event.CompositeRegistration;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.model.util.ListMap;
import jetbrains.jetpad.projectional.cell.SelectionSupport;

import java.util.*;

import static jetbrains.jetpad.hybrid.SelectionPosition.FIRST;
import static jetbrains.jetpad.hybrid.SelectionPosition.LAST;
import static jetbrains.jetpad.model.composite.Composites.firstFocusable;
import static jetbrains.jetpad.model.composite.Composites.lastFocusable;

public abstract class BaseHybridSynchronizer<SourceT, SpecT extends SimpleHybridEditorSpec<SourceT>> implements Synchronizer {
  public static final CellTraitPropertySpec<Runnable> ON_LAST_ITEM_DELETED = new CellTraitPropertySpec<>("onLastItemDeleted");

  static final CellTraitPropertySpec<HybridSynchronizer<?>> HYBRID_SYNCHRONIZER = new CellTraitPropertySpec<>("hybridSynchronizer");

  public static final ContentKind<List<Token>> TOKENS_CONTENT = new ContentKind<List<Token>>() {};

  private TokenListEditor<SourceT> myTokenListEditor;

  private Registration myAttachRegistration;
  private Mapper<?, ?> myContextMapper;
  private ReadableProperty<SourceT> mySource;
  private Cell myTarget;
  private List<Cell> myTargetList;
  private Set<Mapper<?, ? extends Cell>> myValueMappers;
  private ListMap<Cell, Mapper<?, ? extends Cell>> myValueCellToMapper;
  private TextCell myPlaceholder;
  private MapperFactory<Object, ? extends Cell> myMapperFactory;

  private SelectionSupport<Cell> mySelectionSupport;
  private String myPlaceHolderText = "empty";

  private boolean myHideTokensInMenu = false;
  private ReadableProperty<? extends SpecT> mySpec;

  BaseHybridSynchronizer(Mapper<?, ?> contextMapper, ReadableProperty<SourceT> source, Cell target,
                         ReadableProperty<? extends SpecT> spec, TokenListEditor<SourceT> editor) {
    myContextMapper = contextMapper;
    mySource = source;
    mySpec = spec;
    myTokenListEditor = editor;
    myTarget = target;

    myValueMappers = myContextMapper.createChildSet();

    myTargetList = CellLists.spaced(myTarget.children());

    myTarget.addTrait(createTargetTrait());

    addPlaceholder();

    myTokenListEditor.tokens.addHandler(new EventHandler<CollectionItemEvent<? extends Token>>() {
      @Override
      public void onEvent(CollectionItemEvent<? extends Token> event) {
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
      public void onItemAdded(CollectionItemEvent<? extends Cell> event) {
        event.getNewItem().selected().set(true);

        int index = myTargetList.indexOf(event.getNewItem());
        if (selection.size() > 1) {
          if (event.getIndex() == 0) {
            myTarget.children().get(index * 2 + 1).selected().set(true);
          } else { //last
            myTarget.children().get(index * 2 - 1).selected().set(true);
          }
        }
      }

      @Override
      public void onItemRemoved(CollectionItemEvent<? extends Cell> event) {
        event.getOldItem().selected().set(false);

        int index = myTargetList.indexOf(event.getOldItem());
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

  protected ReadableProperty<SourceT> getSource() {
    return mySource;
  }

  protected Cell getTarget() {
    return myTarget;
  }

  protected List<Cell> getTargetList() {
    return myTargetList;
  }

  private CellTrait createTargetTrait() {
    return new CellTrait() {
      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == HYBRID_SYNCHRONIZER) {
          return BaseHybridSynchronizer.this;
        }
        if (spec == CellStateHandler.PROPERTY) {
          return getCellStateHandler();
        }

        return super.get(cell, spec);
      }

      @Override
      public void onKeyPressed(Cell cell, KeyEvent event) {
        Cell focusedCell = cell.getContainer().focusedCell.get();
        if (myTargetList.contains(focusedCell)) {
          Cell currentCell = cell.getContainer().focusedCell.get();
          if (!hasSelection()) {
            if (event.is(KeyStrokeSpecs.SELECT_UP) && currentCell != null) {
              mySelectionSupport.select(currentCell, currentCell);
              event.consume();
            }
          } else {
            Range<Integer> currentRange = selection();
            if (event.is(KeyStrokeSpecs.SELECT_UP)) {
              ParseNode parseNode = myTokenListEditor.getParseNode();
              if (parseNode != null) {
                if (!currentRange.equals(parseNode.getRange())) {
                  ParseNode node = ParseNodes.findForRange(parseNode, currentRange);
                  ParseNode parentNode = ParseNodes.nonSameRangeParent(node);
                  if (parentNode != null) {
                    select(parentNode.getRange());
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
              ParseNode parseNode = myTokenListEditor.getParseNode();
              if (parseNode != null) {
                ParseNode node = ParseNodes.findForRange(parseNode, currentRange);
                ParseNode childNode = ParseNodes.nonSameRangeChild(node, myTargetList.indexOf(mySelectionSupport.currentCell()));
                if (childNode != null) {
                  select(childNode.getRange());
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

        int targetIndex;
        if (currentCell != null) {
          int currentCellIndex = myTargetList.indexOf(currentCell);
          boolean home = Positions.isHomePosition(currentCell);
          boolean end = Positions.isEndPosition(currentCell);
          if (home && end) {
            // One-char token which allows editing at only one side
            if (currentCell instanceof TextTokenCell && ((TextTokenCell) currentCell).noSpaceToLeft()) {
              targetIndex = currentCellIndex + 1;
            } else {
              targetIndex = currentCellIndex;
            }
          } else if (home) {
            targetIndex = currentCellIndex;
          } else {
            targetIndex = currentCellIndex + 1;
          }
        } else {
          targetIndex = 0;
        }

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

          @Override
          public String toString() {
            try {
              return TokenUtil.getText(tokens);
            } catch (UnsupportedOperationException e) {
              return super.toString();
            }
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

  protected abstract  CellStateHandler<Cell, ? extends CellState> getCellStateHandler();

  private CollectionListener<Token> createTokensListener() {
    return new CollectionAdapter<Token>() {
      @Override
      public void onItemAdded(CollectionItemEvent<? extends Token> event) {
        if (myPlaceholder != null) {
          removePlaceholder();
        }

        final Token token = event.getNewItem();
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
      public void onItemRemoved(CollectionItemEvent<? extends Token> event) {
        final int index = event.getIndex();
        Cell removedCell = myTargetList.remove(index);

        if (myValueCellToMapper != null && myValueCellToMapper.containsKey(removedCell)) {
          Mapper<?, ? extends Cell> valueMapper = myValueCellToMapper.get(removedCell);
          myValueMappers.remove(valueMapper);
          myValueCellToMapper.remove(removedCell);
          if (myValueCellToMapper.isEmpty()) {
            myValueCellToMapper = null;
          }
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
      if (myValueCellToMapper == null) {
        myValueCellToMapper = new ListMap<>();
      }
      myValueCellToMapper.put(target, mapper);

      target.addTrait(new TokenCellTraits.TokenCellTrait(true) {
        @Override
        protected CellTrait[] getBaseTraits(Cell cell) {
          return new CellTrait[] { CompletionSupport.trait() };
        }
      });

      firstFocusable(target).addTrait(new TokenCellTraits.LeftLeafTokenCellTrait());
      lastFocusable(target).addTrait(new TokenCellTraits.RightLeafTokenCellTrait());

      return target;
    }

    return new TextTokenCell(this, token);
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
    final TextCell result = new TextCell();
    result.addTrait(new DerivedCellTrait() {
      @Override
      protected CellTrait getBase(Cell cell) {
        return TextEditing.validTextEditing(Predicates.equalTo(""));
      }

      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == TextEditing.EAGER_COMPLETION) return true;

        if (spec == Completion.COMPLETION) return tokenCompletion().placeholderCompletion(cell);

        if (spec == TextEditing.AFTER_PASTE) {
          return new Supplier<Boolean>() {
            @Override
            public Boolean get() {
              return tokenOperations().afterPaste(result);
            }
          };
        }
        return super.get(cell, spec);
      }
    });
    return result;
  }

  void setTokens(List<Token> tokens) {
    myTokenListEditor.tokens.clear();
    myTokenListEditor.tokens.addAll(tokens);
    myTokenListEditor.updateToPrintedTokens();
  }

  public ObservableList<Token> tokens() {
    return myTokenListEditor.tokens;
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
    ParseNode parseNode = tokenListEditor().getParseNode();
    if (parseNode == null) {
      throw new IllegalStateException("Hybrid Synchronizer is in invalid state");
    }
    ParseNode result = ParseNodes.findNodeFor(parseNode, object);
    if (result == null) {
      throw new IllegalStateException("Can't find parse node for " + object);
    }
    return result.getRange();
  }

  public Runnable select(int index, SelectionPosition pos) {
    return tokenOperations().select(index, pos);
  }

  public Runnable selectOnCreation(int index, SelectionPosition pos) {
    return tokenOperations().selectOnCreation(index, pos);
  }

  public void setHideTokensInMenu(boolean hideTokens) {
    myHideTokensInMenu = hideTokens;
  }

  public boolean isHideTokensInMenu() {
    return myHideTokensInMenu;
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
      tokenOperations().select(!isEnd ? firstIndex : firstIndex - 1, isEnd ? LAST : FIRST).run();
    }
  }

  Runnable lastItemDeleted() {
    final Runnable result = myTarget.get(ON_LAST_ITEM_DELETED);
    if (result != null) {
      return result;
    }
    return CellActions.toCell(myPlaceholder);
  }

  public abstract ReadableProperty<Boolean> valid();

  public List<Cell> tokenCells() {
    return Collections.unmodifiableList(myTargetList);
  }

  SpecT editorSpec() {
    return mySpec.get();
  }

  public Cell placeholder() {
    return myPlaceholder;
  }

  public Cell target() {
    return myTarget;
  }

  ReadableProperty<SourceT> property() {
    return mySource;
  }

  Mapper<?, ?> contextMapper() {
    return myContextMapper;
  }

  TokenOperations<SourceT> tokenOperations() {
    return new TokenOperations<>(this);
  }

  TokenCompleter tokenCompletion() {
    return new TokenCompleter(this);
  }

  TokenListEditor<SourceT> tokenListEditor() {
    return myTokenListEditor;
  }

  TextTokenCell getPair(TextTokenCell cell) {
    PairSpec pairSpec = mySpec.get().getPairSpec();
    List<Token> tokens = myTokenListEditor.tokens;
    Token token = cell.getToken();

    if (pairSpec.isLeft(cell.getToken())) {
      int index = myTargetList.indexOf(cell);
      if (index == -1) return null;

      Stack<Token> pairStack = new Stack<>();
      for (int i = index + 1; i < myTargetList.size(); i++) {
        Cell targetCell = myTargetList.get(i);
        if (!(targetCell instanceof TextTokenCell)) continue;

        TextTokenCell tc = (TextTokenCell) targetCell;
        Token t = tc.getToken();
        if (pairStack.isEmpty() && pairSpec.isPair(token, t)) {
          return tc;
        }

        if (pairSpec.isLeft(t)) {
          pairStack.push(t);
        } else if (pairSpec.isRight(t) && !pairStack.isEmpty() && pairSpec.isPair(pairStack.peek(), t)) {
          pairStack.pop();
        }
      }
      return null;
    } else if (pairSpec.isRight(cell.getToken())) {
      int index = myTargetList.indexOf(cell);
      if (index == -1) return null;
      Stack<Token> pairStack = new Stack<>();

      for (int i = index - 1; i >=0; i--) {
        Cell targetCell = myTargetList.get(i);
        if (!(targetCell instanceof TextTokenCell)) continue;

        TextTokenCell tc = (TextTokenCell) targetCell;
        Token t = tokens.get(i);
        if (pairStack.isEmpty() && pairSpec.isPair(t, token)) {
          return tc;
        }

        if (pairSpec.isRight(t)) {
          pairStack.push(t);
        } else if (pairSpec.isLeft(t) && !pairStack.isEmpty() && pairSpec.isPair(t, pairStack.peek())) {
          pairStack.pop();
        }
      }
      return null;
    } else {
      return null;
    }
  }

  protected abstract Registration onAttach(Property<SourceT> syncValue);

  @Override
  public void attach(SynchronizerContext ctx) {
    CollectionListener<Token> tokensListener = createTokensListener();
    myAttachRegistration = new CompositeRegistration(
        onAttach(myTokenListEditor.value),
        myTokenListEditor.tokens.addListener(tokensListener));

    ObservableList<Token> tokens = myTokenListEditor.tokens;
    for (int i = 0; i < tokens.size(); i++) {
      Token t = tokens.get(i);
      tokensListener.onItemAdded(new CollectionItemEvent<>(null, t, i, CollectionItemEvent.EventType.ADD));
    }
  }

  @Override
  public void detach() {
    myTokenListEditor.dispose();
    myAttachRegistration.remove();
    myAttachRegistration = null;
  }
}
