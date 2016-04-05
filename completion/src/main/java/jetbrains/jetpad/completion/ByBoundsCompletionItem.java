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
package jetbrains.jetpad.completion;

public abstract class ByBoundsCompletionItem extends BaseCompletionItem {
  private final String myPrefix;
  private final String mySuffix;
  private final String myVisibleText;

  protected ByBoundsCompletionItem(String prefix) {
    this(prefix, "");
  }

  protected ByBoundsCompletionItem(String prefix, String suffix) {
    myPrefix = prefix;
    mySuffix = suffix;
    myVisibleText = prefix + "..." + suffix;
  }

  @Override
  public String visibleText(String text) {
    return myVisibleText;
  }

  @Override
  public boolean isStrictMatchPrefix(String text) {
    return text.length() < myPrefix.length() && myPrefix.startsWith(text)
        || isSuffixSpecified() && isPrefixedButNotSuffixed(text);
  }

  @Override
  public boolean isMatch(String text) {
    if (isSuffixSpecified()) {
      return isPrefixedAndSuffixed(text);
    } else {
      return text.startsWith(myPrefix);
    }
  }

  private boolean isSuffixSpecified() {
    return mySuffix.length() > 0;
  }

  private boolean isPrefixedButNotSuffixed(String text) {
    return isPrefixedAndHasSuffixAt(text, -1);
  }

  private boolean isPrefixedAndSuffixed(String text) {
    return isPrefixedAndHasSuffixAt(text, text.length() - mySuffix.length());
  }

  private boolean isPrefixedAndHasSuffixAt(String text, int expectedSuffixPos) {
    if (text.startsWith(myPrefix)) {
      int suffixPos = text.indexOf(mySuffix, myPrefix.length());
      return expectedSuffixPos == suffixPos;
    }
    return false;
  }

  protected String getBody(String text) {
    if (text.startsWith(myPrefix)) {
      if ((text.length() - myPrefix.length() >= mySuffix.length())
          && text.endsWith(mySuffix)) {
        return text.substring(myPrefix.length(), text.length() - mySuffix.length());
      } else {
        return text.substring(myPrefix.length());
      }
    }
    return text;
  }
}
