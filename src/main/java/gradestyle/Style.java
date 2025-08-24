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
import java.util.ArrayList;
import java.util.List;

public class Style {
  public static void main(String[] args) {
    Config config = Config.parse(args);

    if (config == null) {
      System.exit(1);
    }

    Github github = new Github(config);
    List<Repo> repos = Repo.getRepos(github);
    List<ValidationResult> results = new ArrayList<>();

    Validator[] validators = {new Checkstyle(), new JavaParser(), new Pmd(), new Cpd()};
    Csv csv = setupCsv(config);

    try {
      Validation validation = new Validation(validators, config);

      for (Validator validator : validators) {
        validator.setup(config);
      }

      for (Repo repo : repos) {
        ValidationResult result = validation.validate(repo);

        incrementCsv(csv, result);

        results.add(result);
      }
    } catch (ValidatorException e) {
      System.err.println("Unable to run style validation.");
      e.printStackTrace();
      System.exit(1);
    } finally {
      if (csv != null) {
        try {
          csv.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    // a future work could be to make the markdown and github feedback incremental as well, that way
    // we don't have to hold the list of results in memory, would only need to process one a time.
    outputMarkdown(config, results);
    sendGithubFeedback(github, results);
  }

  private static void incrementCsv(Csv csv, ValidationResult result) {

    if (csv == null) {
      return;
    }

    try {
      csv.write(result);
    } catch (IOException e) {
      System.err.println("Unable to write CSV file.");
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static Csv setupCsv(Config config) {
    if (config.getStyleFeedback().getReportsCsv() == null) {
      System.out.println("no csv");
      return null;
    }

    ValidationCsv writer = new ValidationCsv(config.getCategoryConfigs());
    try {
      return new Csv(config.getStyleFeedback().getReportsCsv(), writer);
    } catch (IOException e) {
      System.err.println("Unable to open CSV file.");
      e.printStackTrace();
      System.exit(1);
      return null;
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
