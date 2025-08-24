package gradestyle.config.javadocconfig;

import gradestyle.config.CategoryConfig;

public class JavadocClassConfig extends CategoryConfig {
  private int minWords;

  public JavadocClassConfig(CategoryConfig config, int minWords) {
    super(config.getCategory(), config.getExamples(), config.getMode(), config.getScores());
    this.minWords = minWords;
  }

  public int getMinWords() {
    return minWords;
  }

  public static CategoryConfig getConfig(
      String check, CategoryConfig categoryConfig, int minWords) {
    switch (check) {
      case "Class":
        return new JavadocClassConfig(categoryConfig, minWords);

      case "Constructor":
        return new JavadocConstructorConfig(categoryConfig, minWords);

      case "Method":
        return new JavadocMethodConfig(categoryConfig, minWords);
      case "Field":
        return new JavadocFieldConfig(categoryConfig, minWords);

      default:
        return null;
    }
  }
}
