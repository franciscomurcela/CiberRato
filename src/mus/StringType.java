public class StringType extends Type {
  public StringType() {
    super("string");
  }

  public boolean isNumeric() {
    return false;
  }
}
