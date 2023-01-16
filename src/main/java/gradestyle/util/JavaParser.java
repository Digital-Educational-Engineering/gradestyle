package gradestyle.util;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import gradestyle.Repo;

public class JavaParser {
  public static com.github.javaparser.JavaParser get(Repo repo) {
    JavaParserTypeSolver typeSolver = new JavaParserTypeSolver(FileUtils.getMainDir(repo));
    JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);

    ParserConfiguration config =
        new ParserConfiguration()
            .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17)
            .setSymbolResolver(symbolSolver);

    return new com.github.javaparser.JavaParser(config);
  }
}
