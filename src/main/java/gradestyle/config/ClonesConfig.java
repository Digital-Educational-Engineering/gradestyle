package gradestyle.config;

public class ClonesConfig extends CategoryConfig {
  private int tokens;

  public ClonesConfig(CategoryConfig config, int tokens) {
    super(config.getCategory(), config.getExamples(), config.getMode(), config.getScores());
    this.tokens = tokens;
  }

  public int getTokens() {
    return this.tokens;
  }
}
