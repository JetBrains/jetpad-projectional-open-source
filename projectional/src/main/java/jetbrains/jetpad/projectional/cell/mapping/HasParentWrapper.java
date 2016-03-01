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
package jetbrains.jetpad.projectional.cell.mapping;

import jetbrains.jetpad.model.composite.HasParent;

/**
 * To compose a mapper, having f(HasParent) as source, desiring to be able to find cells for wrapped HasParent object,
 * one can use this interface at the mapper.
 * The declared method is a getter for f argument. See CellProviderTest for an example.
 */
public interface HasParentWrapper {
  HasParent<?> getCellMappingSource();
}
