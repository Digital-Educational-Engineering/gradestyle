package gradestyle.validator.pmd;

import gradestyle.Repo;
import gradestyle.util.FileUtils;
import gradestyle.validator.Type;
import gradestyle.validator.Validator;
import gradestyle.validator.ValidatorException;
import gradestyle.validator.Violation;
import gradestyle.validator.Violations;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersion;

public class Pmd implements Validator {
  private static final String config = "gradestyle/validator/pmd/pmd.xml";

  @Override
  public Violations validate(Repo repo) throws ValidatorException {
    PMDConfiguration configuration = new PMDConfiguration();
    LanguageVersion version = LanguageRegistry.findLanguageByTerseName("java").getVersion("17");

    configuration.setInputPaths(FileUtils.getMainDir(repo).toString());
    configuration.addRuleSet(config);
    configuration.setDefaultLanguageVersion(version);
    configuration.setIgnoreIncrementalAnalysis(true);

    Report report = PmdAnalysis.create(configuration).performAnalysisAndCollectReport();

    if (!report.getProcessingErrors().isEmpty()) {
      throw new ValidatorException(Path.of(report.getProcessingErrors().get(0).getFile()));
    }

    try {
      return getViolations(report.getViolations());
    } catch (IOException e) {
      throw new ValidatorException(e);
    }
  }

  private Violations getViolations(List<RuleViolation> ruleViolations) throws IOException {
    Violations violations = new Violations();

    for (RuleViolation violation : ruleViolations) {
      Type type = getType(violation.getRule());
      Path file = Path.of(violation.getFilename());
      int start = violation.getBeginLine();
      int end = violation.getEndLine();

      // PMD violates a useless import on all wildcard imports unless compiled classes are provided.
      if (type == Type.Useless_Import) {
        String line =
            Files.lines(Path.of(violation.getFilename()))
                .skip(violation.getBeginLine() - 1)
                .findFirst()
                .get();

        if (line.contains("*")) {
          continue;
        }
      }

      violations.getViolations().add(new Violation(type, file, start, end));
    }

    return violations;
  }

  private Type getType(Rule rule) {
    switch (rule.getName()) {
      case "UnusedAssignment":
        return Type.Useless_Assignment;
      case "UnusedLocalVariable":
        return Type.Useless_LocalVariable;
      case "UnusedPrivateField":
        return Type.Useless_Field;
      case "UnusedPrivateMethod":
        return Type.Useless_Method;
      case "UnnecessaryCast":
        return Type.Useless_Cast;
      case "UnnecessaryConstructor":
        return Type.Useless_Constructor;
      case "UnnecessaryFullyQualifiedName":
        return Type.Useless_FullyQualifiedName;
      case "UnnecessaryImport":
        return Type.Useless_Import;
      case "UnnecessaryReturn":
        return Type.Useless_Return;
      case "FieldNamingConventions":
        return Type.VariableNames_FinalStaticUppercase;
      case "StringConcatenation":
        return Type.StringConcatenation;
      case "EarlyReturn":
        return Type.EarlyReturn;
      default:
        throw new IllegalArgumentException("Unknown rule: " + rule.getName());
    }
  }
}
