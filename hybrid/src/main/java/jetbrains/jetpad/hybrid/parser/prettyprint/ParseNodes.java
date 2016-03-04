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
package jetbrains.jetpad.hybrid.parser.prettyprint;

import com.google.common.collect.Range;

public class ParseNodes {
  public static ParseNode findForRange(ParseNode root, Range<Integer> range) {
    if (!root.getRange().encloses(range)) return null;
    for (ParseNode child : root.getChildren()) {
      ParseNode result = findForRange(child, range);
      if (result != null) {
        return result;
      }
    }
    return root;
  }

  public static ParseNode nonSameRangeParent(ParseNode node) {
    ParseNode parent = node.getParent();
    if (parent == null) return null;
    if (node.getRange().equals(parent.getRange())) {
      return nonSameRangeParent(parent);
    }
    return parent;
  }

  public static ParseNode nonSameRangeChild(ParseNode node, int offset) {
    Range<Integer> targetRange = Range.closed(offset, offset + 1);

    if (!node.getRange().encloses(targetRange)) {
      throw new IllegalArgumentException();
    }

    for (ParseNode child : node.getChildren()) {
      if (child.getRange().equals(node.getRange())) {
        return nonSameRangeChild(child, offset);
      }
      if (child.getRange().encloses(targetRange)) {
        return child;
      }
    }

    return null;
  }

  public static ParseNode findNodeFor(ParseNode parent, Object model) {
    if (parent.getValue() == model) return parent;
    for (ParseNode child : parent.getChildren()) {
      ParseNode result = findNodeFor(child, model);
      if (result != null) return result;
    }
    return null;
  }
}