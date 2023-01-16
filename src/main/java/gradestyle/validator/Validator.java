package gradestyle.validator;

import gradestyle.Repo;
import gradestyle.config.Config;

public interface Validator {
  default void setup(Config config) throws ValidatorException {}

  Violations validate(Repo repo) throws ValidatorException;
}
