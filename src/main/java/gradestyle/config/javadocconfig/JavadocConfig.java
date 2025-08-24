package gradestyle.config.javadocconfig;

import gradestyle.config.CategoryConfig;

public class JavadocConfig extends CategoryConfig {
  public JavadocConfig(CategoryConfig config) {
    super(config.getCategory(), config.getExamples(), config.getMode(), config.getScores());
  }
}
