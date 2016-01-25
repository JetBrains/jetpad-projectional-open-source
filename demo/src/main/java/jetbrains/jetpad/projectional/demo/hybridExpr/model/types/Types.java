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
package jetbrains.jetpad.projectional.demo.hybridExpr.model.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Types {
  public static final Type OBJECT = new Type() {
    @Override
    public List<FieldDescriptor> getFields() {
      return Collections.emptyList();
    }

    @Override
    public List<MethodDescriptor> getMethods() {
      return Arrays.asList(
        new MethodDescriptor(INT, "hashCode"),
        new MethodDescriptor(STRING, "toString"),
        new MethodDescriptor(BOOL, "equals")
      );
    }

    @Override
    public String toString() {
      return "Object";
    }
  };

  public static final Type STRING = new Type() {
    @Override
    public List<FieldDescriptor> getFields() {
      List<FieldDescriptor> result = new ArrayList<>();
      result.addAll(OBJECT.getFields());
      result.add(new FieldDescriptor(INT, "length"));
      return result;
    }

    @Override
    public List<MethodDescriptor> getMethods() {
      List<MethodDescriptor> result = new ArrayList<>();
      result.addAll(OBJECT.getMethods());
      result.add(new MethodDescriptor(STRING, "substring", INT));
      result.add(new MethodDescriptor(BOOL, "startsWith", STRING));
      result.add(new MethodDescriptor(BOOL, "endsWith", STRING));
      result.add(new MethodDescriptor(STRING, "concat", STRING));
      return result;
    }

    @Override
    public String toString() {
      return "String";
    }
  };
  public static final Type INT = new Type() {
    @Override
    public List<FieldDescriptor> getFields() {
      List<FieldDescriptor> result = new ArrayList<>();
      result.addAll(OBJECT.getFields());
      return result;
    }

    @Override
    public List<MethodDescriptor> getMethods() {
      List<MethodDescriptor> result = new ArrayList<>();
      result.addAll(OBJECT.getMethods());
      result.add(new MethodDescriptor(INT, "add", INT, INT));
      result.add(new MethodDescriptor(INT, "sub", INT, INT));
      result.add(new MethodDescriptor(INT, "mul", INT, INT));
      result.add(new MethodDescriptor(INT, "mul2", INT, INT));
      result.add(new MethodDescriptor(INT, "mul3", INT, INT));
      result.add(new MethodDescriptor(INT, "div", INT, INT));
      result.add(new MethodDescriptor(INT, "mod", INT, INT));
      result.add(new MethodDescriptor(INT, "neg", INT));
      return result;
    }

    @Override
    public String toString() {
      return "Int";
    }
  };
  public static final Type BOOL = new Type() {
    @Override
    public List<FieldDescriptor> getFields() {
      List<FieldDescriptor> result = new ArrayList<>();
      result.addAll(OBJECT.getFields());
      return result;
    }

    @Override
    public List<MethodDescriptor> getMethods() {
      List<MethodDescriptor> result = new ArrayList<>();
      result.addAll(OBJECT.getMethods());
      result.add(new MethodDescriptor(BOOL, "and", BOOL, BOOL));
      result.add(new MethodDescriptor(BOOL, "or", BOOL, BOOL));
      result.add(new MethodDescriptor(BOOL, "xor", BOOL, BOOL));
      result.add(new MethodDescriptor(BOOL, "not", BOOL));
      return result;
    }

    @Override
    public String toString() {
      return "Bool";
    }
  };
}