package gradestyle.validator;

import java.nio.file.Path;

public class ValidatorException extends Exception {
  private Path path;

  public ValidatorException(Exception e) {
    super(e);
  }

  public ValidatorException(Path path) {
    this.path = path;
  }

  public Path getPath() {
    return path;
  }
}
