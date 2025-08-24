package gradestyle.config.programmingpracticeconfig;

import gradestyle.config.CategoryConfig;

public class MissingOverrideConfig extends CategoryConfig {
  private static boolean includeDepedencies = false;

  public MissingOverrideConfig(CategoryConfig config, boolean includeDepedencies) {
    super(config.getCategory(), config.getExamples(), config.getMode(), config.getScores());
    MissingOverrideConfig.includeDepedencies = includeDepedencies;
  }

  public static boolean includeDepedencies() {
    return includeDepedencies;
  }
}
