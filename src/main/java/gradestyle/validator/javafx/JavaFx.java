package gradestyle.validator.javafx;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import gradestyle.Repo;
import gradestyle.config.Config;
import gradestyle.util.FileUtils;
import gradestyle.util.JavaParser;
import gradestyle.validator.Category;
import gradestyle.validator.Type;
import gradestyle.validator.Validator;
import gradestyle.validator.ValidatorException;
import gradestyle.validator.Violation;
import gradestyle.validator.Violations;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

public class JavaFx implements Validator {
  private SAXParserFactory factory = SAXParserFactory.newInstance();

  private boolean enabled;

  @Override
  public void setup(Config config) {
    enabled = config.getCategoryConfig(Category.JavaFX) != null;
  }

  @Override
  public Violations validate(Repo repo) throws ValidatorException {
    Violations violations = new Violations();

    if (!enabled) {
      return violations;
    }

    try {
      runJavaFx(repo, violations);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      throw new ValidatorException(e);
    }

    return violations;
  }

  private void runJavaFx(Repo repo, Violations violations)
      throws ParserConfigurationException, SAXException, IOException, ValidatorException {
    SAXParser parser = factory.newSAXParser();

    for (Path fxmlFile : FileUtils.getFxmlResourceFiles(repo.getDir()).toList()) {
      Fxml fxml = new Fxml();

      parser.parse(fxmlFile.toFile(), fxml);

      Path javaFile = null;
      ClassOrInterfaceDeclaration declaration = null;

      for (Path file : FileUtils.getJavaSrcFiles(repo.getDir()).toList()) {
        ParseResult<CompilationUnit> result = JavaParser.get(repo).parse(file);

        if (!result.isSuccessful()) {
          throw new ValidatorException(file);
        }

        Optional<ClassOrInterfaceDeclaration> optionalDeclaration =
            result
                .getResult()
                .get()
                .findFirst(
                    ClassOrInterfaceDeclaration.class,
                    decl -> decl.getFullyQualifiedName().get().equals(fxml.getController()));

        if (optionalDeclaration.isPresent()) {
          javaFile = file;
          declaration = optionalDeclaration.get();
          break;
        }
      }

      if (declaration == null) {
        continue;
      }

      checkControllerClass(violations, javaFile, declaration);
      checkIds(violations, javaFile, declaration, fxml.getIds());
      checkActions(violations, javaFile, declaration, fxml.getActions());
    }
  }

  private void checkControllerClass(
      Violations violations, Path file, ClassOrInterfaceDeclaration decl) {
    if (!decl.getNameAsString().endsWith("Controller")) {
      addViolation(violations, Type.JavaFX_Controller, file, decl.getName());
    }
  }

  private void checkIds(
      Violations violations, Path file, ClassOrInterfaceDeclaration decl, List<String> ids) {
    for (String id : ids) {
      Optional<FieldDeclaration> field = decl.getFieldByName(id);

      if (field.isEmpty()) {
        continue;
      }

      Optional<AnnotationExpr> annotation = field.get().getAnnotationByName("FXML");

      if (annotation.isEmpty()) {
        addViolation(
            violations, Type.JavaFX_FieldAnnotation, file, field.get().getVariable(0).getName());
      }

      for (VariableDeclarator variable : field.get().getVariables()) {
        if (variable.getInitializer().isPresent()) {
          addViolation(violations, Type.JavaFX_Initializer, file, variable.getInitializer().get());
        }
      }
    }
  }

  private void checkActions(
      Violations violations, Path file, ClassOrInterfaceDeclaration decl, List<String> actions) {
    for (String action : actions) {
      List<MethodDeclaration> methods = decl.getMethodsByName(action);

      if (methods.isEmpty()) {
        continue;
      }

      for (MethodDeclaration method : methods) {
        Optional<AnnotationExpr> annotation = method.getAnnotationByName("FXML");

        if (annotation.isEmpty()) {
          addViolation(violations, Type.JavaFX_EventHandlerAnnotation, file, method.getName());
        }

        if (!StringUtils.splitByCharacterTypeCamelCase(method.getNameAsString())[0].equals("on")) {
          addViolation(violations, Type.JavaFX_EventHandlerName, file, method.getName());
        }

        if (!method.isPrivate() && !method.isProtected()) {
          addViolation(violations, Type.JavaFX_EventHandlerPrivate, file, method.getName());
        }
      }
    }
  }

  private void addViolation(Violations violations, Type type, Path file, Node node) {
    violations.getViolations().add(new Violation(type, file, node.getRange().get().begin.line));
  }
}
