package jetbrains.jetpad.projectional.view;

import jetbrains.jetpad.geometry.Vector;

public class ImageData {
  public static ImageData emptyImage(Vector dim) {
    return new EmptyImageData(dim);
  }

  public static ImageData binaryData(Vector dim, byte[] data) {
    return new BinaryImageData(dim, data);
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
}
