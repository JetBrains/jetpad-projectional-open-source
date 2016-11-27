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
package jetbrains.jetpad.hybrid.parser.prettyprint;

import com.google.common.base.Function;
import com.google.common.collect.Range;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.hybrid.parser.*;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.event.CompositeRegistration;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.EventSource;
import jetbrains.jetpad.model.property.ReadableProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class PrettyPrinterContext<NodeT>  {
  private PrettyPrinter<NodeT> myPrettyPrinter;

  private PrettyPrintResult myResult;

  public PrettyPrinterContext(PrettyPrinter<NodeT> pp) {
    this(new PrettyPrintResult(), pp);
    myResult.myStack.add(new ArrayList<BaseParseNode>());
  }

  private PrettyPrinterContext(PrettyPrintResult result, PrettyPrinter<NodeT> pp) {
    myResult = result;
    myPrettyPrinter = pp;
  }

  public <NewNodeT> PrettyPrinterContext<NewNodeT> withPrinter(PrettyPrinter<NewNodeT> pp) {
    return new PrettyPrinterContext<>(myResult, pp);
  }

  public void print(final NodeT obj) {
    if (myResult.myPrinted) {
      throw new IllegalStateException();
    }

    print(obj, new Runnable() {
      @Override
      public void run() {
        myPrettyPrinter.print(obj, PrettyPrinterContext.this);
      }
    });

    myResult.myPrinted = true;
  }

  public void append(Token token) {
    if (token == null) {
      throw new NullPointerException("Token can't be null");
    }

    myResult.myTokens.add(token);
    TokenParseNode result = new TokenParseNode(token, myResult.myTokens.size() - 1);
    myResult.myStack.peek().add(result);

    myResult.myPrinted = true;
  }

  public <ValueT> void append(ReadableProperty<ValueT> prop, Function<ValueT, Token> f) {
    myResult.myChangeSources.add(prop);

    append(f.apply(prop.get()));
  }

  public void append(ReadableProperty<? extends NodeT> prop) {
    myResult.myChangeSources.add(prop);

    final NodeT value = prop.get();
    if (value == null) return;
    print(value, new Runnable() {
      @Override
      public void run() {
        myPrettyPrinter.print(value, PrettyPrinterContext.this);
      }
    });
  }

  public void append(ObservableList<? extends NodeT> list) {
    myResult.myChangeSources.add(list);

    for (final NodeT e : list) {
      print(e, new Runnable() {
        @Override
        public void run() {
          myPrettyPrinter.print(e, PrettyPrinterContext.this);
        }
      });
    }
  }

  public void append(final ObservableList<? extends NodeT> list, Token separator) {
    myResult.myChangeSources.add(list);
    append((List<? extends NodeT>) list, separator);
  }

  public void append(final List<? extends NodeT> list, Token separator) {
    for (int i = 0; i < list.size(); i++) {
      if (i != 0) {
        append(separator);
      }
      final NodeT e = list.get(i);
      print(e, new Runnable() {
        @Override
        public void run() {
          myPrettyPrinter.print(e, PrettyPrinterContext.this);
        }
      });
    }
  }

  public void appendInt(ReadableProperty<Integer> prop) {
    append(prop, new Function<Integer, Token>() {
      @Override
      public Token apply(Integer input) {
        return new IntValueToken(input != null ? input : 0);
      }
    });
  }

  public void appendBool(ReadableProperty<Boolean> prop) {
    append(prop, new Function<Boolean, Token>() {
      @Override
      public Token apply(Boolean input) {
        return new BoolValueToken(input != null ? input : Boolean.FALSE);
      }
    });
  }

  public void appendId(ReadableProperty<String> prop) {
    append(prop, new Function<String, Token>() {
      @Override
      public Token apply(String input) {
        return new IdentifierToken(input != null ? input : "");
      }
    });
  }

  public void appendError(ReadableProperty<String> prop) {
    append(prop, new Function<String, Token>() {
      @Override
      public Token apply(String input) {
        return new ErrorToken(input != null ? input : "");
      }
    });
  }

  private void print(NodeT obj, Runnable r) {
    myResult.myStack.push(new ArrayList<BaseParseNode>());
    r.run();
    List<BaseParseNode> nodes = myResult.myStack.pop();

    BaseParseNode result;
    if (nodes.isEmpty()) {
      result = new EmptyParseNode(obj, myResult.myTokens.size());
    } else {
      result = new CompositeParseNode(obj, nodes);
    }
    myResult.myStack.peek().add(result);
  }

  public List<Token> tokens() {
    ensurePrinted();
    return Collections.unmodifiableList(myResult.myTokens);
  }

  public ParseNode result() {
    ensurePrinted();
    if (myResult.myStack.size() != 1 || myResult.myStack.peek().size() != 1) {
      throw new IllegalStateException();
    }
    return myResult.myStack.peek().get(0);
  }

  public EventSource<Object> changeSource() {
    return new EventSource<Object>() {
      @Override
      public Registration addHandler(EventHandler<? super Object> handler) {
        CompositeRegistration reg = new CompositeRegistration();
        for (EventSource<?> s : myResult.myChangeSources) {
          reg.add(s.addHandler(handler));
        }
        return reg;
      }
    };
  }

  private void ensurePrinted() {
    if (!myResult.myPrinted) {
      throw new IllegalStateException();
    }
  }

  private static class PrettyPrintResult {
    private List<Token> myTokens = new ArrayList<>();
    private Stack<List<BaseParseNode>> myStack = new Stack<>();
    private boolean myPrinted;
    private List<EventSource<?>> myChangeSources = new ArrayList<>();
  }

  private static abstract class BaseParseNode implements ParseNode {
    private BaseParseNode myParent;

    @Override
    public ParseNode getParent() {
      return myParent;
    }
  }

  private static class CompositeParseNode extends BaseParseNode {
    private Object myValue;
    private List<ParseNode> myChildren = new ArrayList<>();

    private CompositeParseNode(Object value, List<BaseParseNode> children) {
      myValue = value;
      if (children.isEmpty()) {
        throw new IllegalArgumentException();
      }
      for (BaseParseNode n : children) {
        n.myParent = this;
        myChildren.add(n);
      }
    }

    @Override
    public Object getValue() {
      return myValue;
    }

    @Override
    public List<ParseNode> getChildren() {
      return Collections.unmodifiableList(myChildren);
    }

    @Override
    public Range<Integer> getRange() {
      int start = myChildren.get(0).getRange().lowerEndpoint();
      int end = myChildren.get(myChildren.size() - 1).getRange().upperEndpoint();
      return Range.closed(start, end);
    }

    @Override
    public String toString() {
      return "" + myChildren;
    }
  }

  private static class EmptyParseNode extends BaseParseNode {
    private int myOffset;
    private Object myValue;

    private EmptyParseNode(Object value, int offset) {
      myOffset = offset;
      myValue = value;
    }

    @Override
    public Object getValue() {
      return myValue;
    }

    @Override
    public List<ParseNode> getChildren() {
      return Collections.<ParseNode>emptyList();
    }

    @Override
    public Range<Integer> getRange() {
      return Range.closed(myOffset, myOffset);
    }

    @Override
    public String toString() {
      return "empty";
    }
  }

  private static class TokenParseNode extends BaseParseNode {
    private Token myToken;
    private int myOffset;

    private TokenParseNode(Token token, int offset) {
      myToken = token;
      myOffset = offset;
    }

    @Override
    public Object getValue() {
      return myToken;
    }

    @Override
    public List<ParseNode> getChildren() {
      return Collections.emptyList();
    }

    @Override
    public Range<Integer> getRange() {
      return Range.closed(myOffset, myOffset + 1);
    }

    @Override
    public String toString() {
      return "" + myToken;
    }
  }
}