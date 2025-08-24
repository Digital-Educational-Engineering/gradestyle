package gradestyle.config.programmingpracticeconfig;

import gradestyle.config.CategoryConfig;

public class FinalizeOverrideConfig extends CategoryConfig {
  public FinalizeOverrideConfig(CategoryConfig config) {
    super(config.getCategory(), config.getExamples(), config.getMode(), config.getScores());
  }
}
