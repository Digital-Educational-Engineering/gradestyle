package gradestyle.util;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import gradestyle.Repo;
import java.io.IOException;

public class JavaParser {
  public static com.github.javaparser.JavaParser get(Repo repo) {

    ParserConfiguration config =
        new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);

    JavaParserTypeSolver javaParserTypeSolver;
    try {
      javaParserTypeSolver = new JavaParserTypeSolver(FileUtils.getMainDir(repo), config);
    } catch (IOException e) {
      javaParserTypeSolver = null;
      e.printStackTrace();
    }

    CombinedTypeSolver typeSolver = new CombinedTypeSolver();
    typeSolver.add(new ReflectionTypeSolver());
    typeSolver.add(javaParserTypeSolver);

    // Now set the resolver
    JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
    config.setSymbolResolver(symbolSolver);

    return new com.github.javaparser.JavaParser(config);
  }
}
