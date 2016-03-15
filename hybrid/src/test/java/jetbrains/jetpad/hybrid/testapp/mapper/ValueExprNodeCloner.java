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
package jetbrains.jetpad.hybrid.testapp.mapper;

import jetbrains.jetpad.hybrid.parser.ValueToken;
import jetbrains.jetpad.hybrid.testapp.model.Comment;
import jetbrains.jetpad.hybrid.testapp.model.Expr;
import jetbrains.jetpad.hybrid.testapp.model.ExprNode;

public class ValueExprNodeCloner implements ValueToken.ValueCloner<ExprNode> {

  public ValueExprNodeCloner() {
  }

  @Override
  public ExprNode clone(ExprNode val) {
    if (val instanceof Expr) {
      Expr expr = (Expr) val;
      return new ValueExprCloner().clone(expr);
    }

    if (val instanceof Comment) {
      return new Comment();
    }

    throw new IllegalArgumentException(val.toString());
  }
}
