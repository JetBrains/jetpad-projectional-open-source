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
package jetbrains.jetpad.projectional.view;

public class ViewPropertySpec<ValueT> {
  private String myName;
  private ValueT myDefaultValue;
  private ViewPropertyKind myKind;


  public ViewPropertySpec(String name,  ViewPropertyKind kind, ValueT defaultValue) {
    myName = name;
    myDefaultValue = defaultValue;
    myKind = kind;
  }

  public ViewPropertySpec(String name, ViewPropertyKind kind) {
    this(name, kind, null);
  }

  public ViewPropertySpec(String name) {
    this(name, ViewPropertyKind.NONE, null);
  }

  public ValueT defaultValue(View view) {
    return myDefaultValue;
  }

  ViewPropertyKind kind() {
    return myKind;
  }

  @Override
  public String toString() {
    return myName;
  }
}