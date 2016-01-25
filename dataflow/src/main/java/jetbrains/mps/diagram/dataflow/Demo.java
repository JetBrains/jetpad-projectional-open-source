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
package jetbrains.mps.diagram.dataflow;

import jetbrains.jetpad.geometry.Vector;
import jetbrains.mps.diagram.dataflow.model.Block;
import jetbrains.mps.diagram.dataflow.model.Blocks;
import jetbrains.mps.diagram.dataflow.model.Connector;
import jetbrains.mps.diagram.dataflow.model.Diagram;

public class Demo {
  public static Diagram createDemoModel() {
    Diagram result = new Diagram();

    Block b1 = Blocks.newConstant();
    b1.location.set(new Vector(100, 100));
    result.blocks.add(b1);

    Block b2 = Blocks.newSum();
    b2.location.set(new Vector(300, 100));
    result.blocks.add(b2);

    Connector c1 = new Connector();
    c1.output.set(b1.outputs.get(0));
    c1.input.set(b2.inputs.get(0));
    result.connectors.add(c1);

    Block b3 = Blocks.newInt();
    b3.location.set(new Vector(500, 100));
    result.blocks.add(b3);

    Connector c2 = new Connector();
    c2.output.set(b2.outputs.get(0));
    c2.input.set(b3.inputs.get(0));
    result.connectors.add(c2);

    return result;
  }
}