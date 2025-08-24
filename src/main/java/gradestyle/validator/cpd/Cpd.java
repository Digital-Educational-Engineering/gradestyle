package gradestyle.validator.cpd;

import gradestyle.Repo;
import gradestyle.config.ClonesConfig;
import gradestyle.config.Config;
import gradestyle.util.FileUtils;
import gradestyle.validator.Type;
import gradestyle.validator.Validator;
import gradestyle.validator.ValidatorException;
import gradestyle.validator.Violation;
import gradestyle.validator.Violations;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.sourceforge.pmd.cpd.CPDConfiguration;
import net.sourceforge.pmd.cpd.CPDReport;
import net.sourceforge.pmd.cpd.CpdAnalysis;
import net.sourceforge.pmd.cpd.Mark;
import net.sourceforge.pmd.cpd.Match;
import net.sourceforge.pmd.lang.java.JavaLanguageModule;

public class Cpd implements Validator {
  private ClonesConfig config;

  public void setup(Config config) throws ValidatorException {
    this.config = config.getCategoryConfig(ClonesConfig.class);
  }

  @Override
  public Violations validate(Repo repo) throws ValidatorException {
    if (config == null) {
      return new Violations();
    }

    CPDConfiguration configuration = new CPDConfiguration();

    configuration.setMinimumTileSize(config.getTokens());
    configuration.setIgnoreAnnotations(true);
    configuration.setIgnoreIdentifiers(true);
    configuration.setIgnoreLiterals(true);

    JavaLanguageModule lan = new JavaLanguageModule();
    configuration.setDefaultLanguageVersion(lan.getVersion("21"));

    try {
      List<Path> sourceFiles = FileUtils.getJavaSrcFiles(repo.getDir()).toList();
      configuration.setInputPathList(sourceFiles);
    } catch (IOException e) {
      throw new ValidatorException(e);
    }

    CpdAnalysis analysis = CpdAnalysis.create(configuration);

    List<Violation> violations = new ArrayList<>();
    Consumer<CPDReport> consumer =
        new Consumer<CPDReport>() {

          @Override
          public void accept(CPDReport report) {
            for (Match match : report.getMatches()) {
              Mark mark1 = match.getFirstMark();
              Mark mark2 = match.getSecondMark();

              mark1.getLocation().getStartLine();

              violations.add(
                  new Violation(
                      Type.Clones,
                      Path.of(mark1.getLocation().getFileId().getAbsolutePath()),
                      mark1.getLocation().getStartLine(),
                      mark1.getLocation().getEndLine()));
              violations.add(
                  new Violation(
                      Type.Clones,
                      Path.of(mark2.getLocation().getFileId().getAbsolutePath()),
                      mark2.getLocation().getStartLine(),
                      mark2.getLocation().getEndLine()));
            }
          }
        };
    analysis.performAnalysis(consumer);

    return new Violations(violations);
  }
}
