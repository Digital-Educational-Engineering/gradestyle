package gradestyle.config.javadocconfig;

import gradestyle.config.CategoryConfig;

public class JavadocConstructorConfig extends CategoryConfig {
  private int minWords;

  public JavadocConstructorConfig(CategoryConfig config, int minWords) {
    super(config.getCategory(), config.getExamples(), config.getMode(), config.getScores());
    this.minWords = minWords;
  }

  public int getMinWords() {
    return minWords;
  }
}
