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
package jetbrains.jetpad.projectional.demo.expr.model;

import jetbrains.jetpad.model.children.ChildProperty;
import jetbrains.jetpad.model.property.Property;

public abstract class BinaryExpression extends Expression {
  public final Property<Expression> left = new ChildProperty<ExpressionAstNode, Expression>(this);
  public final Property<Expression> right = new ChildProperty<ExpressionAstNode, Expression>(this);

  protected abstract String getSign();

  protected abstract BinaryExpression createCopy();

  @Override
  public Expression copy() {
    BinaryExpression copy = createCopy();
    if (left.get() != null) {
      copy.left.set(left.get().copy());
    }
    if (right.get() != null) {
      copy.right.set(right.get().copy());
    }
    return copy;
  }

  @Override
  public String toString() {
    return "{" + left.get() + " " + getSign() + " " + right.get() + "}";
  }
}