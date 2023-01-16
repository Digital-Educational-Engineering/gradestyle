package gradestyle.validator;

import java.nio.file.Path;
import java.util.Objects;

public class Violation {
  private Type type;

  private Path file;

  private int beginLine;

  private int endLine;

  public Violation(Type type, Path file, int beginLine) {
    this.type = type;
    this.file = file;
    this.beginLine = beginLine;
    this.endLine = -1;
  }

  public Violation(Type type, Path file, int beginLine, int endLine) {
    this.type = type;
    this.file = file;
    this.beginLine = beginLine;
    this.endLine = endLine;
  }

  public Type getType() {
    return type;
  }

  public Path getPath() {
    return file;
  }

  public int getLine() {
    return beginLine;
  }

  public int getEndLine() {
    return endLine;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }

    if (other instanceof Violation violation) {
      return violation.type.getMessage().equals(type.getMessage())
          && violation.file.equals(file)
          && violation.beginLine == beginLine
          && violation.endLine == endLine;
    }

    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(file, beginLine, endLine);
  }
}
