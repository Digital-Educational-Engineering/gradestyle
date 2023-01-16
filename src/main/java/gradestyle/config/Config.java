package gradestyle.config;

import gradestyle.config.CategoryConfig.Mode;
import gradestyle.validator.Category;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class Config {
  public static Config parse(String[] args) {
    if (args.length != 1) {
      System.err.println("Missing config file argument.");
      return null;
    }

    try {
      return createConfig(args[0]);
    } catch (ConfigurationException e) {
      System.err.println("Invalid config file: " + args[0]);
    }

    return null;
  }

  private static Config createConfig(String filename) throws ConfigurationException {
    PropertiesBuilderParameters params =
        new Parameters()
            .properties()
            .setListDelimiterHandler(new DefaultListDelimiterHandler(','))
            .setFileName(filename);

    Configuration config =
        new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
            .configure(params)
            .getConfiguration();

    Path parent = Path.of(filename).toAbsolutePath().getParent();
    Path repos = resolveOptionalPath(parent, config.getString("repos"), parent);
    String packageString = config.getString("package");

    Path defaultTemplate = repos.resolve(repos.getFileName());
    Path template = resolveOptionalPath(repos, config.getString("template"), defaultTemplate);
    boolean templateIgnoreViolations = config.getBoolean("template.ignoreViolations", false);

    Path reportsCsv = resolveOptionalPath(parent, config.getString("reports.csv"), null);
    Path reportsMd = resolveOptionalPath(parent, config.getString("reports.md"), null);
    String feedbackTitle = config.getString("feedback.title");
    String feedbackMessage = config.getString("feedback.message");
    String feedbackError = config.getString("feedback.error");

    FeedbackReportConfig styleFeedback =
        new FeedbackReportConfig(
            reportsCsv, reportsMd, feedbackTitle, feedbackMessage, feedbackError);

    boolean github = config.getBoolean("github", false);
    String githubToken = config.getString("github.token");
    String githubClassroom = config.getString("github.classroom");
    String githubAssignment = config.getString("github.assignment");
    boolean githubFeedback = config.getBoolean("github.feedback", false);

    List<CategoryConfig> categoryConfigs = createCategoryConfigs(config);

    return new Config(
        repos,
        packageString,
        template,
        templateIgnoreViolations,
        styleFeedback,
        github,
        githubToken,
        githubClassroom,
        githubAssignment,
        github && !categoryConfigs.isEmpty() && githubFeedback,
        categoryConfigs);
  }

  private static Path resolveOptionalPath(Path path, String other, Path fallback) {
    if (other == null) {
      return fallback;
    }

    if (Path.of(other).isAbsolute()) {
      return Path.of(other);
    }

    return path.resolve(other);
  }

  private static List<CategoryConfig> createCategoryConfigs(Configuration config) {
    List<CategoryConfig> categoryConfigs = new ArrayList<>();

    for (Category category : Category.values()) {
      if (!config.getBoolean(category.name(), false)) {
        continue;
      }

      int examples = config.getInt(category.name() + ".examples", Integer.MAX_VALUE);
      Mode mode = config.get(Mode.class, category.name() + ".mode");
      List<Integer> scoreList = config.getList(Integer.class, category.name() + ".scores");

      Collections.sort(scoreList);

      CategoryConfig categoryConfig = new CategoryConfig(category, examples, mode, scoreList);

      switch (category) {
        case Commenting:
          {
            int minLines = config.getInt(category.name() + ".minLines");
            int minFrequency = config.getInt(category.name() + ".minFrequency");
            int maxFrequency = config.getInt(category.name() + ".maxFrequency");
            int levenshteinDistance = config.getInt(category.name() + ".levenshteinDistance");

            categoryConfig =
                new CommentingConfig(
                    categoryConfig, minLines, minFrequency, maxFrequency, levenshteinDistance);
            break;
          }
        case JavaDoc:
          {
            int minWords = config.getInt(category.name() + ".minWords");

            categoryConfig = new JavaDocConfig(categoryConfig, minWords);
            break;
          }
        case Clones:
          {
            int tokens = config.getInt(category.name() + ".tokens");

            categoryConfig = new ClonesConfig(categoryConfig, tokens);
          }
        default:
          break;
      }

      categoryConfigs.add(categoryConfig);
    }

    return categoryConfigs;
  }

  private Path repos;

  private String packageString;

  private Path templateRepo;

  private boolean templateIgnoreViolations;

  private FeedbackReportConfig styleFeedback;

  private boolean github;

  private String githubToken;

  private String githubClassroom;

  private String githubAssignment;

  private boolean githubFeedback;

  private List<CategoryConfig> categoryConfigs;

  private Config(
      Path repos,
      String packageString,
      Path templateRepo,
      boolean templateIgnoreViolations,
      FeedbackReportConfig styleFeedback,
      boolean github,
      String githubToken,
      String githubClassroom,
      String githubAssignment,
      boolean githubFeedback,
      List<CategoryConfig> categoryConfigs) {
    this.repos = repos;
    this.packageString = packageString;
    this.templateRepo = templateRepo;
    this.templateIgnoreViolations = templateIgnoreViolations;
    this.styleFeedback = styleFeedback;
    this.github = github;
    this.githubToken = githubToken;
    this.githubClassroom = githubClassroom;
    this.githubAssignment = githubAssignment;
    this.githubFeedback = githubFeedback;
    this.categoryConfigs = categoryConfigs;
  }

  public Path getRepos() {
    return repos;
  }

  public String getPackage() {
    return packageString;
  }

  public Path getTemplateRepo() {
    return templateRepo;
  }

  public boolean getTemplateIgnoreViolations() {
    return templateIgnoreViolations;
  }

  public FeedbackReportConfig getStyleFeedback() {
    return styleFeedback;
  }

  public boolean getGithub() {
    return github;
  }

  public String getGithubToken() {
    return githubToken;
  }

  public String getGithubClassroom() {
    return githubClassroom;
  }

  public String getGithubAssignment() {
    return githubAssignment;
  }

  public boolean getGithubFeedback() {
    return githubFeedback;
  }

  public List<CategoryConfig> getCategoryConfigs() {
    return categoryConfigs;
  }

  public CategoryConfig getCategoryConfig(Category category) {
    return getCategoryConfigs().stream()
        .filter(config -> config.getCategory() == category)
        .findFirst()
        .orElse(null);
  }

  public <T extends CategoryConfig> T getCategoryConfig(Class<T> category) {
    return getCategoryConfigs().stream()
        .filter(category::isInstance)
        .map(category::cast)
        .findFirst()
        .orElse(null);
  }
}
