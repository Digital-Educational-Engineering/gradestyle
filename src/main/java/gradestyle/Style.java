package gradestyle;

import gradestyle.config.Config;
import gradestyle.validator.Validation;
import gradestyle.validator.ValidationCsv;
import gradestyle.validator.ValidationMarkdown;
import gradestyle.validator.ValidationResult;
import gradestyle.validator.Validator;
import gradestyle.validator.ValidatorException;
import gradestyle.validator.checkstyle.Checkstyle;
import gradestyle.validator.cpd.Cpd;
import gradestyle.validator.javaparser.JavaParser;
import gradestyle.validator.pmd.Pmd;
import java.io.IOException;
import java.util.List;

public class Style {
  public static void main(String[] args) {
    Config config = Config.parse(args);

    if (config == null) {
      System.exit(1);
    }

    Github github = new Github(config);
    List<Repo> repos = Repo.getRepos(github);
    List<ValidationResult> results = runValidation(config, repos);

    outputCsv(config, results);
    outputMarkdown(config, results);
    sendGithubFeedback(github, results);
  }

  private static List<ValidationResult> runValidation(Config config, List<Repo> repos) {
    Validator[] validators = {new Checkstyle(), new JavaParser(), new Pmd(), new Cpd()};

    Validation validation = new Validation(validators, config);

    try {
      return validation.validate(repos);
    } catch (ValidatorException e) {
      System.err.println("Unable to run style validation.");
      e.printStackTrace();
      System.exit(1);
    }

    return null;
  }

  private static void outputCsv(Config config, List<ValidationResult> results) {
    if (config.getStyleFeedback().getReportsCsv() == null) {
      return;
    }

    ValidationCsv writer = new ValidationCsv(config.getCategoryConfigs(), results);
    Csv csv = new Csv(config.getStyleFeedback().getReportsCsv(), writer);

    try {
      csv.write();
    } catch (IOException e) {
      System.err.println("Unable to write CSV file.");
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void outputMarkdown(Config config, List<ValidationResult> results) {
    if (config.getStyleFeedback().getReportsMd() == null) {
      return;
    }

    ValidationMarkdown writer = new ValidationMarkdown(config);
    Markdown<ValidationResult> md =
        new Markdown<>(config.getStyleFeedback().getReportsMd(), writer);

    try {
      md.write(results);
    } catch (IOException e) {
      System.err.println("Unable to write markdown files.");
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void sendGithubFeedback(Github github, List<ValidationResult> results) {
    if (!github.getConfig().getGithubFeedback()) {
      return;
    }

    try {
      github.sendFeedback(results);
    } catch (IOException e) {
      System.err.println("Unable to send GitHub feedback.");
      e.printStackTrace();
      System.exit(1);
    }
  }
}
