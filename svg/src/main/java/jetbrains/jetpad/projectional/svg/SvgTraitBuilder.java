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
package jetbrains.jetpad.projectional.svg;

import jetbrains.jetpad.event.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SvgTraitBuilder {
  private SvgTrait myParent;
  private Map<SvgEventSpec<? extends Event>, List<SvgEventHandler<? extends Event>>> myHandlers = new HashMap<>();

  public SvgTraitBuilder() {
    this(null);
  }

  public SvgTraitBuilder(SvgTrait parent) {
    myParent = parent;
  }

  public <EventT extends Event> SvgTraitBuilder on(SvgEventSpec<EventT> spec, SvgEventHandler<EventT> handler) {
    if (!myHandlers.containsKey(spec)) {
      myHandlers.put(spec, new ArrayList<SvgEventHandler<? extends Event>>());
    }
    myHandlers.get(spec).add(handler);
    return this;
  }

  public SvgTrait build() {
    return new SvgTrait(myParent, myHandlers);
  }
}
