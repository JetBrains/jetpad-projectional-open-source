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
package jetbrains.jetpad.projectional.svg.toAwt;

import jetbrains.jetpad.projectional.svg.SvgNodeContainer;
import jetbrains.jetpad.projectional.svg.SvgSvgElement;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SvgAwtPeerTest {
  SvgNodeContainer container;
  SvgSvgElement root;
  SvgRootDocumentMapper mapper;

  @Before
  public void setUp() throws Exception {
    root = new SvgSvgElement();
    container = new SvgNodeContainer(root);
    mapper = new SvgRootDocumentMapper(root);
  }

  @Test
  public void documentInitNullPeer() {
    assertNull(container.getPeer());
  }

  @Test
  public void mapperAttachPeerSet() {
    mapper.attachRoot();
    assertNotNull(container.getPeer());
  }

  @Test
  public void mapperDetachRootNullPeer() {
    mapper.attachRoot();
    mapper.detachRoot();
    assertNull(container.getPeer());
  }
}