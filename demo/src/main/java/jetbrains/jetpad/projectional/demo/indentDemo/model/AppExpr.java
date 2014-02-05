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
package jetbrains.jetpad.projectional.demo.indentDemo.model;

import jetbrains.jetpad.model.children.ChildProperty;
import jetbrains.jetpad.model.property.Property;

public class AppExpr extends Expr {
  public final Property<Expr> fun = new ChildProperty<LambdaNode, Expr>(this);
  public final Property<Expr> arg = new ChildProperty<LambdaNode, Expr>(this);

  @Override
  public Expr copy() {
    AppExpr result = new AppExpr();
    result.fun.set(copy(fun.get()));
    result.arg.set(copy(arg.get()));
    return result;
  }

  @Override
  public String toString() {
    return "(app " + fun.get() + " " + arg.get() + ")";
  }
}