package gradestyle.config;

public class CommentingConfig extends CategoryConfig {
  private int minLines;

  private int minFrequency;

  private int maxFrequency;

  private int levenshteinDistance;

  public CommentingConfig(
      CategoryConfig config,
      int minLines,
      int minFrequency,
      int maxFrequency,
      int levenshteinDistance) {
    super(config.getCategory(), config.getExamples(), config.getMode(), config.getScores());
    this.minLines = minLines;
    this.minFrequency = minFrequency;
    this.maxFrequency = maxFrequency;
    this.levenshteinDistance = levenshteinDistance;
  }

  public int getMinLines() {
    return this.minLines;
  }

  public int getMinFrequency() {
    return this.minFrequency;
  }

  public int getMaxFrequency() {
    return this.maxFrequency;
  }

  public int getLevenshteinDistance() {
    return this.levenshteinDistance;
  }
}
