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
package jetbrains.jetpad.projectional.demo.hybridExpr.model.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MethodDescriptor {
  private String myName;
  private Type myReturnType;
  private List<Type> myParameterTypes;

  public MethodDescriptor(Type returnType, String name, Type... parameterTypes) {
    myReturnType = returnType;
    myName = name;
    myParameterTypes = new ArrayList<Type>(Arrays.asList(parameterTypes));
  }

  public Type getReturnType() {
    return myReturnType;
  }

  public String getName() {
    return myName;
  }

  public List<Type> getParameterTypes() {
    return Collections.unmodifiableList(myParameterTypes);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append(myName);
    result.append("(");
    String parms = getParameterTypes().toString();
    result.append(parms.substring(1, parms.length() - 1));
    result.append(")");
    return result.toString();
  }
}