package gradestyle.validator;

import gradestyle.Repo;
import gradestyle.config.Config;
import gradestyle.util.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class Validation {
  private Validator[] validators;

  private Config config;

  public Validation(Validator[] validators, Config config) {
    this.validators = validators;
    this.config = config;
  }

  public List<ValidationResult> validate(List<Repo> repos) throws ValidatorException {
    Map<String, List<String>> templateRepoLines;

    try {
      templateRepoLines = readTemplateRepoLines();
    } catch (IOException e) {
      throw new ValidatorException(e);
    }

    for (Validator validator : validators) {
      validator.setup(config);
    }

    List<ValidationResult> results = new ArrayList<>();

    for (Repo repo : repos) {
      List<Violation> violations = new ArrayList<>();
      Path error = null;

      for (Validator validator : validators) {
        try {
          for (Violation violation : validator.validate(repo).getViolations()) {
            if (notInTemplate(violation, repo, templateRepoLines)) {
              violations.add(violation);
            }
          }
        } catch (ValidatorException e) {
          error = e.getPath();

          if (error == null) {
            throw e;
          }

          System.err.println(
              "Style validation of \""
                  + repo.getName()
                  + "\" using \""
                  + validator.getClass().getSimpleName()
                  + "\" failed @ \""
                  + error
                  + "\".");
          break;
        } catch (IOException e) {
          throw new ValidatorException(e);
        }
      }

      results.add(new ValidationResult(repo, new Violations(violations), error));
    }

    return results;
  }

  private Map<String, List<String>> readTemplateRepoLines() throws IOException {
    Map<String, List<String>> templateRepoLines = new HashMap<>();

    if (!config.getTemplateIgnoreViolations()) {
      return templateRepoLines;
    }

    try (Stream<Path> files = FileUtils.getJavaFiles(config.getTemplateRepo())) {
      for (Path file : files.toList()) {
        try (Stream<String> lines = Files.lines(file)) {
          templateRepoLines
              .computeIfAbsent(file.getFileName().toString(), x -> new ArrayList<>())
              .addAll(lines.toList());
        }
      }
    }

    return templateRepoLines;
  }

  private boolean notInTemplate(
      Violation violation, Repo repo, Map<String, List<String>> templateRepoLines)
      throws IOException {
    if (!config.getTemplateIgnoreViolations()) {
      return true;
    }

    String fileName = violation.getPath().getFileName().toString();

    if (!templateRepoLines.containsKey(fileName)) {
      return true;
    }

    List<String> templateLines = templateRepoLines.get(fileName);

    try (Stream<String> repoLines = Files.lines(violation.getPath())) {
      Optional<String> repoLine = repoLines.skip(violation.getLine() - 1).findFirst();

      return repoLine.isEmpty() || !templateLines.contains(repoLine.get());
    }
  }
}
