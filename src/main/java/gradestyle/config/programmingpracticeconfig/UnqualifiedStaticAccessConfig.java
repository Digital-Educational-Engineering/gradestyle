package gradestyle.config.programmingpracticeconfig;

import gradestyle.config.CategoryConfig;

public class UnqualifiedStaticAccessConfig extends CategoryConfig {
  public UnqualifiedStaticAccessConfig(CategoryConfig config) {
    super(config.getCategory(), config.getExamples(), config.getMode(), config.getScores());
  }
}
