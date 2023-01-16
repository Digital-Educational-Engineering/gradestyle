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
import java.util.Iterator;
import java.util.List;
import net.sourceforge.pmd.cpd.CPD;
import net.sourceforge.pmd.cpd.CPDConfiguration;
import net.sourceforge.pmd.cpd.JavaLanguage;
import net.sourceforge.pmd.cpd.Mark;
import net.sourceforge.pmd.cpd.Match;

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

    configuration.setLanguage(new JavaLanguage());
    configuration.setMinimumTileSize(config.getTokens());
    configuration.setIgnoreAnnotations(true);
    configuration.setIgnoreIdentifiers(true);
    configuration.setIgnoreLiterals(true);

    CPD cpd = new CPD(configuration);

    try {
      cpd.addRecursively(FileUtils.getMainDir(repo).toFile());
    } catch (IOException e) {
      throw new ValidatorException(e);
    }

    cpd.go();

    List<Violation> violations = new ArrayList<>();
    Iterator<Match> matches = cpd.getMatches();

    while (matches.hasNext()) {
      Match match = matches.next();

      Mark mark1 = match.getFirstMark();
      Mark mark2 = match.getSecondMark();

      violations.add(
          new Violation(
              Type.Clones, Path.of(mark1.getFilename()), mark1.getBeginLine(), mark1.getEndLine()));
      violations.add(
          new Violation(
              Type.Clones, Path.of(mark2.getFilename()), mark2.getBeginLine(), mark2.getEndLine()));
    }

    return new Violations(violations);
  }
}
