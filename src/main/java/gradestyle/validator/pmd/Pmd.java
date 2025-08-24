package gradestyle.validator.pmd;

import gradestyle.Repo;
import gradestyle.config.programmingpracticeconfig.MissingOverrideConfig;
import gradestyle.util.FileUtils;
import gradestyle.validator.Type;
import gradestyle.validator.Validator;
import gradestyle.validator.ValidatorException;
import gradestyle.validator.Violation;
import gradestyle.validator.Violations;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.lang.java.JavaLanguageModule;
import net.sourceforge.pmd.lang.rule.Rule;
import net.sourceforge.pmd.reporting.Report;
import net.sourceforge.pmd.reporting.RuleViolation;

public class Pmd implements Validator {
  private static final URL config = Pmd.class.getResource("pmd.xml");

  @Override
  public Violations validate(Repo repo) throws ValidatorException {

    // Need the bytecode to use PMD's missing override rule:
    //    https://github.com/pmd/pmd/issues/2428
    repo.generateBytecode();

    PMDConfiguration configuration = new PMDConfiguration();

    JavaLanguageModule lan = new JavaLanguageModule();
    configuration.setDefaultLanguageVersion(lan.getVersion("21"));

    Path ruleSetPath;
    try {
      if (config == null) {
        throw new ValidatorException(new IllegalStateException("pmd.xml not found!"));
      }

      // Check if running from a JAR
      if (config.toURI().getScheme().equals("jar")) {
        // copy resource to a temporary file
        try (InputStream stream = Pmd.class.getResourceAsStream("pmd.xml")) {
          if (stream == null) {
            throw new ValidatorException(new Exception("Failed to load pmd.xml from JAR"));
          }
          Path tempFile = Files.createTempFile("pmd-ruleset", ".xml");
          Files.copy(stream, tempFile, StandardCopyOption.REPLACE_EXISTING);
          ruleSetPath = tempFile;
        }
      } else {
        // Running from IDE or normal file system
        ruleSetPath = Paths.get(config.toURI());
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
      throw new ValidatorException(e);
    }

    if (!Files.exists(ruleSetPath)) {
      throw new IllegalStateException("ERROR: pmd.xml ruleset not found.");
    }

    configuration.addRuleSet(ruleSetPath.toString());

    configuration.setIgnoreIncrementalAnalysis(true);
    configuration.setSourceEncoding(StandardCharsets.UTF_8);

    try {

      List<Path> sourceFiles = FileUtils.getJavaSrcFiles(repo.getDir()).toList();

      configuration.setInputPathList(sourceFiles);

      // Always add the repo's compiled code to PMD using a class loader.
      ClassLoader repoClassLoader = createProjectClassLoader(repo, configuration.getClassLoader());
      configuration.setClassLoader(repoClassLoader);

      // optionally add the repo's dependencies to the class loader also.
      if (MissingOverrideConfig.includeDepedencies()) {
        MavenAetherReader.configureMavenDependencies(repo, configuration, sourceFiles);
      }

    } catch (IOException e) {
      throw new ValidatorException(e);
    } catch (Exception e) {
      System.err.println("Error resolving Maven dependencies.");
      e.printStackTrace();
    }

    Report report = PmdAnalysis.create(configuration).performAnalysisAndCollectReport();

    if (!report.getProcessingErrors().isEmpty()) {
      StringBuffer sb = new StringBuffer();
      sb.append("PMD processing errors: \n");
      for (Report.ProcessingError error : report.getProcessingErrors()) {
        sb.append("\t  * " + error.getMsg() + "\n");
      }

      throw new ValidatorException(
          Path.of(report.getProcessingErrors().get(0).getFileId().getAbsolutePath()),
          sb.toString());
    }

    try {
      return getViolations(report.getViolations());
    } catch (IOException e) {
      throw new ValidatorException(e);
    }
  }

  private ClassLoader createProjectClassLoader(Repo repo, ClassLoader parent)
      throws MalformedURLException {
    List<URL> urls = new ArrayList<>();

    Path compiledClassesDir = repo.getBytecodeDir();
    if (Files.exists(compiledClassesDir)) {
      urls.add(compiledClassesDir.toUri().toURL());
    }

    return new URLClassLoader(urls.toArray(new URL[0]), parent);
  }

  private Violations getViolations(List<RuleViolation> ruleViolations) throws IOException {

    Violations violations = new Violations();

    for (RuleViolation violation : ruleViolations) {
      Type type = getType(violation.getRule());
      Path file = Path.of(violation.getFileId().getAbsolutePath());
      int start = violation.getBeginLine();
      int end = violation.getEndLine();

      // PMD violates a useless import on all wildcard imports unless compiled classes
      // are provided.
      if (type == Type.Useless_Import) {
        String line =
            Files.lines(Path.of(violation.getFileId().getAbsolutePath()))
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
      case "StringConcatenation":
        return Type.StringConcatenation;

      case "EmptyCatchBlock":
        return Type.EmptyCatchBlock;

      case "MissingOverride":
        return Type.MissingOverride;

      default:
        throw new IllegalArgumentException("Unknown rule: " + rule.getName());
    }
  }
}
