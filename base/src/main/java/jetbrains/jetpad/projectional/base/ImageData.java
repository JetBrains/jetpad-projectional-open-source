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
package jetbrains.jetpad.projectional.base;

import jetbrains.jetpad.geometry.Vector;

public class ImageData {
  public static ImageData emptyImage(Vector dim) {
    return new EmptyImageData(dim);
  }

  public static ImageData binaryData(Vector dim, byte[] data) {
    return new BinaryImageData(dim, data);
  }

  public static ImageData urlImageData(Vector dim, String url) {
    return new UrlImageData(dim, url);
  }

  private Vector myDimension;

  private ImageData(Vector dim) {
    myDimension = dim;
  }

  public Vector getDimension() {
    return myDimension;
  }

  public static class EmptyImageData extends ImageData {
    EmptyImageData(Vector dim) {
      super(dim);
    }
  }

  public static class BinaryImageData extends ImageData {
    private byte[] myData;

    BinaryImageData(Vector dim, byte[] data) {
      super(dim);
      myData = new byte[data.length];
      System.arraycopy(data, 0, myData, 0, data.length);
    }

    public byte[] getData() {
      byte[] result = new byte[myData.length];
      System.arraycopy(myData, 0, result, 0, myData.length);
      return result;
    }
  }

  public static class UrlImageData extends ImageData {
    private String myUrl;

    UrlImageData(Vector dim, String url) {
      super(dim);
      myUrl = url;
    }

    public String getUrl() {
      return myUrl;
    }
  }
}