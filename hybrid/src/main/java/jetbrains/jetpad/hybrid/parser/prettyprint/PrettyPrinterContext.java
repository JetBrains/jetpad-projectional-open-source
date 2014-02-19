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
package jetbrains.jetpad.hybrid.parser.prettyprint;

import com.google.common.base.*;
import com.google.common.collect.Range;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.event.CompositeRegistration;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.EventSource;
import jetbrains.jetpad.model.event.Registration;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.hybrid.parser.BoolValueToken;
import jetbrains.jetpad.hybrid.parser.IdentifierToken;
import jetbrains.jetpad.hybrid.parser.IntValueToken;
import jetbrains.jetpad.hybrid.parser.Token;
import com.google.common.base.Objects;
import jetbrains.jetpad.model.property.WritableProperty;

import java.util.*;

public class PrettyPrinterContext<NodeT>  {
  private PrettyPrinter<NodeT> myPrettyPrinter;

  private List<Token> myTokens = new ArrayList<>();
  private Stack<List<BaseParseNode>> myStack = new Stack<>();
  private Stack<ParseNodeBuilder> myNodeBuilders = new Stack<>();
  private boolean myPrinted;
  private List<EventSource<?>> myChangeSources = new ArrayList<>();

  public PrettyPrinterContext(PrettyPrinter<NodeT> pp) {
    myPrettyPrinter = pp;
    myStack.add(new ArrayList<BaseParseNode>());
  }

  public void print(final NodeT obj) {
    if (myPrinted) throw new IllegalStateException();

    print(obj, new Runnable() {
      @Override
      public void run() {
        myPrettyPrinter.print(obj, PrettyPrinterContext.this);
      }
    });

    myPrinted = true;
  }

  public ParseNodeBuilder append(Token token) {
    myTokens.add(token);
    TokenParseNode result = new TokenParseNode(token, myTokens.size() - 1);
    myStack.peek().add(result);
    return new MyParseNodeBuilder(result);
  }

  public <ValueT> ParseNodeBuilder append(Property<ValueT> prop, Function<ValueT, Token> f) {
    myChangeSources.add(prop);

    return append(f.apply(prop.get()));
  }

  public void append(Property<? extends NodeT> prop) {
    myChangeSources.add(prop);

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
    myChangeSources.add(list);

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
    myChangeSources.add(list);

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

  public ParseNodeBuilder appendInt(Property<Integer> prop) {
    return append(prop, new Function<Integer, Token>() {
      @Override
      public Token apply(Integer input) {
        return new IntValueToken(input != null ? input : 0);
      }
    });
  }

  public ParseNodeBuilder appendBool(Property<Boolean> prop) {
    return append(prop, new Function<Boolean, Token>() {
      @Override
      public Token apply(Boolean input) {
        return new BoolValueToken(input != null ? input : Boolean.FALSE);
      }
    });
  }

  public ParseNodeBuilder appendId(Property<String> prop) {
    return append(prop, new Function<String, Token>() {
      @Override
      public Token apply(String input) {
        return new IdentifierToken(input != null ? input : "");
      }
    });
  }

  private ParseNodeBuilder print(NodeT obj, Runnable r) {
    myStack.push(new ArrayList<BaseParseNode>());
    final Map<ParseNodeProperty<?>, Object> props = new HashMap<>();
    myNodeBuilders.push(new BaseParseNodeBuilder() {
      @Override
      Map<ParseNodeProperty<?>, Object> getProps() {
        return props;
      }

      @Override
      void initProps() {
      }

      @Override
      void clearProps() {
      }
    });

    r.run();
    List<BaseParseNode> nodes = myStack.pop();
    myNodeBuilders.pop();

    BaseParseNode result;
    if (nodes.isEmpty()) {
      result = new EmptyParseNode(obj, myTokens.size());
    } else {
      result = new CompositeParseNode(obj, nodes);
    }
    result.myProperties = props;
    myStack.peek().add(result);

    return new MyParseNodeBuilder(result);
  }

  public ParseNodeBuilder currentNodeBuilder() {
    return myNodeBuilders.peek();
  }

  public List<Token> tokens() {
    ensurePrinted();
    return Collections.unmodifiableList(myTokens);
  }

  public ParseNode result() {
    ensurePrinted();
    if (myStack.size() != 1 || myStack.peek().size() != 1) throw new IllegalStateException();
    return myStack.peek().get(0);
  }

  public EventSource<Object> changeSource() {
    return new EventSource<Object>() {
      @Override
      public Registration addHandler(EventHandler<? super Object> handler) {
        CompositeRegistration reg = new CompositeRegistration();
        for (EventSource<?> s : myChangeSources) {
          reg.add(s.addHandler(handler));
        }
        return reg;
      }
    };
  }

  private void ensurePrinted() {
    if (!myPrinted) throw new IllegalStateException();
  }

  private static abstract class BaseParseNode implements ParseNode {
    private BaseParseNode myParent;
    private Map<ParseNodeProperty<?>, Object> myProperties;

    @Override
    public ParseNode parent() {
      return myParent;
    }

    @Override
    public <ValueT> ValueT get(ParseNodeProperty<ValueT> prop) {
      if (myProperties == null || !myProperties.containsKey(prop)) return prop.getDefaultValue();
      return (ValueT) myProperties.get(prop);
    }
  }

  private static class CompositeParseNode extends BaseParseNode {
    private Object myValue;
    private List<ParseNode> myChildren = new ArrayList<>();

    private CompositeParseNode(Object value, List<BaseParseNode> children) {
      myValue = value;
      if (children.isEmpty()) throw new IllegalArgumentException();
      for (BaseParseNode n : children) {
        n.myParent = this;
        myChildren.add(n);
      }
    }

    @Override
    public Object value() {
      return myValue;
    }

    @Override
    public List<ParseNode> children() {
      return Collections.unmodifiableList(myChildren);
    }

    @Override
    public Range<Integer> range() {
      int start = myChildren.get(0).range().lowerEndpoint();
      int end = myChildren.get(myChildren.size() - 1).range().upperEndpoint();
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
    public Object value() {
      return myValue;
    }

    @Override
    public List<ParseNode> children() {
      return Collections.<ParseNode>emptyList();
    }

    @Override
    public Range<Integer> range() {
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
    public Object value() {
      return myToken;
    }

    @Override
    public List<ParseNode> children() {
      return Collections.emptyList();
    }

    @Override
    public Range<Integer> range() {
      return Range.closed(myOffset, myOffset + 1);
    }

    @Override
    public String toString() {
      return "" + myToken;
    }
  }

  public static interface ParseNodeBuilder {
    <ValueT> void set(ParseNodeProperty<ValueT> prop, ValueT val);
  }

  private static abstract class BaseParseNodeBuilder implements ParseNodeBuilder {
    abstract Map<ParseNodeProperty<?>, Object> getProps();
    abstract void initProps();
    abstract void clearProps();

    @Override
    public <ValueT> void set(ParseNodeProperty<ValueT> prop, ValueT val) {
      if (Objects.equal(prop.getDefaultValue(), val)) {
        if (getProps() != null) {
          getProps().remove(prop);
          if (getProps().isEmpty()) {
            clearProps();
          }
        }
        return;
      }
      if (getProps() == null) {
        initProps();
      }
      getProps().put(prop, val);
    }
  }

  private static class MyParseNodeBuilder extends BaseParseNodeBuilder {
    private BaseParseNode myNode;

    private MyParseNodeBuilder(BaseParseNode node) {
      myNode = node;
    }

    @Override
    Map<ParseNodeProperty<?>, Object> getProps() {
      return myNode.myProperties;
    }

    @Override
    void initProps() {
      myNode.myProperties = new HashMap<>(1);
    }

    @Override
    void clearProps() {
      myNode.myProperties = null;
    }
  }
}