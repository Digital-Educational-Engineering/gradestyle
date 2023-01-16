package gradestyle.config;

import gradestyle.validator.Category;
import java.util.List;

public class CategoryConfig {
  public enum Mode {
    ABSOLUTE,
    RELATIVE,
  }

  private Category category;

  private int examples;

  private Mode mode;

  private List<Integer> scores;

  public CategoryConfig(Category category, int examples, Mode mode, List<Integer> scores) {
    this.category = category;
    this.examples = examples;
    this.mode = mode;
    this.scores = scores;
  }

  public Category getCategory() {
    return category;
  }

  public int getExamples() {
    return examples;
  }

  public Mode getMode() {
    return mode;
  }

  public List<Integer> getScores() {
    return scores;
  }
}
