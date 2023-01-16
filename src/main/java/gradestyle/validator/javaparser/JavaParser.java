package gradestyle.validator.javaparser;

import com.github.javaparser.ParseResult;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.github.javaparser.javadoc.description.JavadocInlineTag;
import gradestyle.Repo;
import gradestyle.config.CommentingConfig;
import gradestyle.config.Config;
import gradestyle.config.JavaDocConfig;
import gradestyle.util.FileUtils;
import gradestyle.validator.Type;
import gradestyle.validator.Validator;
import gradestyle.validator.ValidatorException;
import gradestyle.validator.Violation;
import gradestyle.validator.Violations;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class JavaParser implements Validator {
  private CommentingConfig commentingConfig;

  private JavaDocConfig javaDocConfig;

  @Override
  public void setup(Config config) {
    this.commentingConfig = config.getCategoryConfig(CommentingConfig.class);
    this.javaDocConfig = config.getCategoryConfig(JavaDocConfig.class);
  }

  @Override
  public Violations validate(Repo repo) throws ValidatorException {
    Violations violations = new Violations();

    try {
      runJavaparser(repo, violations);
    } catch (IOException e) {
      throw new ValidatorException(e);
    }

    return violations;
  }

  private void runJavaparser(Repo repo, Violations violations)
      throws ValidatorException, IOException {
    for (Path file : FileUtils.getJavaSrcFiles(repo.getDir()).toList()) {
      ParseResult<CompilationUnit> result = gradestyle.util.JavaParser.get(repo).parse(file);

      if (!result.isSuccessful()) {
        throw new ValidatorException(file);
      }

      CompilationUnit cu = result.getResult().get();

      privateFieldViolations(file).visit(cu, violations);
      classOrderingViolations(file).visit(cu, violations);

      if (commentingConfig != null) {
        IOException e = commentFrequencyViolations(file).visit(cu, violations);

        if (e != null) {
          throw e;
        }

        commentMeaningViolations(file).visit(cu, violations);
      }

      if (javaDocConfig != null) {
        javadocCommentViolations(file).visit(cu, violations);
      }

      commentViolations(repo, file, cu, violations);
    }
  }

  private GenericVisitorAdapter<IOException, Violations> commentFrequencyViolations(Path file) {
    return new GenericVisitorAdapter<IOException, Violations>() {
      @Override
      public IOException visit(MethodDeclaration decl, Violations violations) {
        IOException superE = super.visit(decl, violations);

        if (superE != null) {
          return superE;
        }

        try {
          visitException(decl, violations);
        } catch (IOException e) {
          return e;
        }

        return null;
      }

      private void visitException(MethodDeclaration decl, Violations violations)
          throws IOException {
        long methodLines = numLines(decl) - 1; // Don't count signature.
        if (methodLines <= commentingConfig.getMinLines()) {
          return;
        }

        long commentLines = 0;

        if (decl.getJavadocComment().isPresent()) {
          commentLines += numLines(decl.getJavadocComment().get());
        }

        for (Comment comment : decl.getAllContainedComments()) {
          commentLines += numLines(comment);
        }

        int ratio = (int) ((float) commentLines / methodLines * 100);

        if (ratio >= commentingConfig.getMinFrequency()
            && ratio <= commentingConfig.getMaxFrequency()) {
          return;
        }

        Type type =
            ratio < commentingConfig.getMinFrequency()
                ? Type.Commenting_FrequencyLow
                : Type.Commenting_FrequencyHigh;

        addViolation(violations, type, file, getFirstLine(decl.getName()));

        return;
      }

      private long numLines(Node node) throws IOException {
        try (Stream<String> lines = Files.lines(file)) {
          return lines
              .skip(getFirstLine(node) - 1)
              .limit(getLastLine(node) - getFirstLine(node) + 1)
              .map(String::trim)
              .filter(line -> !line.isEmpty())
              .filter(line -> !line.equals("{"))
              .filter(line -> !line.equals("}"))
              .count();
        }
      }
    };
  }

  private VoidVisitorAdapter<Violations> commentMeaningViolations(Path file) {
    return new VoidVisitorAdapter<Violations>() {
      @Override
      public void visit(LineComment comment, Violations violations) {
        super.visit(comment, violations);
        visitComment(comment, violations);
      }

      @Override
      public void visit(BlockComment comment, Violations violations) {
        super.visit(comment, violations);
        visitComment(comment, violations);
      }

      private void visitComment(Comment comment, Violations violations) {
        if (comment.isJavadocComment()) {
          return;
        }

        Node node = comment.getCommentedNode().orElse(null);

        if (node == null) {
          return;
        }

        String text = comment.getContent();
        String code = node.removeComment().toString();
        int distance = new LevenshteinDistance().apply(text, code);

        if (commentingConfig == null || distance > commentingConfig.getLevenshteinDistance()) {
          return;
        }

        addViolation(violations, Type.Commenting_Meaningful, file, getFirstLine(comment));
      }
    };
  }

  private VoidVisitorAdapter<Violations> privateFieldViolations(Path file) {
    return new VoidVisitorAdapter<Violations>() {
      @Override
      public void visit(FieldDeclaration decl, Violations violations) {
        super.visit(decl, violations);

        if (decl.isPrivate() || decl.isProtected() || decl.isStatic() || decl.isFinal()) {
          return;
        }

        addViolation(violations, Type.PrivateMembers, file, getFirstLine(decl.getVariable(0)));
      }
    };
  }

  private VoidVisitorAdapter<Violations> classOrderingViolations(Path file) {
    return new VoidVisitorAdapter<Violations>() {
      @Override
      public void visit(ClassOrInterfaceDeclaration decl, Violations violations) {
        super.visit(decl, violations);

        if (!decl.isInterface()) {
          visitAll(decl, violations);
        }
      }

      @Override
      public void visit(EnumDeclaration decl, Violations violations) {
        super.visit(decl, violations);
        visitAll(decl, violations);
      }

      private <T extends TypeDeclaration<?>> void visitAll(
          TypeDeclaration<T> decl, Violations violations) {
        List<TypeDeclaration> innerClasses =
            decl.getMembers().stream()
                .filter(
                    member -> member.isClassOrInterfaceDeclaration() || member.isEnumDeclaration())
                .map(BodyDeclaration::asTypeDeclaration)
                .toList();

        List<FieldDeclaration> staticFields =
            decl.getMembers().stream()
                .filter(BodyDeclaration::isFieldDeclaration)
                .map(BodyDeclaration::asFieldDeclaration)
                .filter(FieldDeclaration::isStatic)
                .toList();

        if (!innerClasses.isEmpty()) {
          staticFields.stream()
              .filter(field -> isBefore(field, getLastElement(innerClasses)))
              .map(field -> field.getVariable(0))
              .map(JavaParser.this::getFirstLine)
              .forEach(line -> addViolation(violations, Type.Ordering_StaticField, file, line));
        }

        List<MethodDeclaration> staticMethods =
            decl.getMembers().stream()
                .filter(BodyDeclaration::isMethodDeclaration)
                .map(BodyDeclaration::asMethodDeclaration)
                .filter(MethodDeclaration::isStatic)
                .toList();

        if (!staticFields.isEmpty() || !innerClasses.isEmpty()) {
          Node lastDeclaration;

          if (!staticFields.isEmpty()) {
            lastDeclaration = getLastElement(staticFields);
          } else {
            lastDeclaration = getLastElement(innerClasses);
          }

          staticMethods.stream()
              .filter(method -> isBefore(method, lastDeclaration))
              .map(MethodDeclaration::getName)
              .map(JavaParser.this::getFirstLine)
              .forEach(line -> addViolation(violations, Type.Ordering_StaticMethod, file, line));
        }

        List<FieldDeclaration> fields =
            decl.getMembers().stream()
                .filter(BodyDeclaration::isFieldDeclaration)
                .map(BodyDeclaration::asFieldDeclaration)
                .filter(Predicate.not(FieldDeclaration::isStatic))
                .toList();

        if (!staticMethods.isEmpty() || !staticFields.isEmpty() || !innerClasses.isEmpty()) {
          Node lastDeclaration;

          if (!staticMethods.isEmpty()) {
            lastDeclaration = getLastElement(staticMethods);
          } else if (!staticFields.isEmpty()) {
            lastDeclaration = getLastElement(staticFields);
          } else {
            lastDeclaration = getLastElement(innerClasses);
          }

          fields.stream()
              .filter(field -> isBefore(field, lastDeclaration))
              .map(field -> field.getVariable(0))
              .map(JavaParser.this::getFirstLine)
              .forEach(line -> addViolation(violations, Type.Ordering_Field, file, line));
        }

        List<ConstructorDeclaration> constructors =
            decl.getMembers().stream()
                .filter(BodyDeclaration::isConstructorDeclaration)
                .map(BodyDeclaration::asConstructorDeclaration)
                .toList();

        if (!fields.isEmpty()
            || !staticMethods.isEmpty()
            || !staticFields.isEmpty()
            || !innerClasses.isEmpty()) {
          Node lastDeclaration;

          if (!fields.isEmpty()) {
            lastDeclaration = getLastElement(fields);
          } else if (!staticMethods.isEmpty()) {
            lastDeclaration = getLastElement(staticMethods);
          } else if (!staticFields.isEmpty()) {
            lastDeclaration = getLastElement(staticFields);
          } else {
            lastDeclaration = getLastElement(innerClasses);
          }

          constructors.stream()
              .filter(constructor -> isBefore(constructor, lastDeclaration))
              .map(ConstructorDeclaration::getName)
              .map(JavaParser.this::getFirstLine)
              .forEach(line -> addViolation(violations, Type.Ordering_Constructor, file, line));
        }

        List<MethodDeclaration> methods =
            decl.getMembers().stream()
                .filter(BodyDeclaration::isMethodDeclaration)
                .map(BodyDeclaration::asMethodDeclaration)
                .filter(Predicate.not(MethodDeclaration::isStatic))
                .toList();

        if (!constructors.isEmpty()
            || !fields.isEmpty()
            || !staticMethods.isEmpty()
            || !staticFields.isEmpty()
            || !innerClasses.isEmpty()) {
          Node lastDeclaration;

          if (!constructors.isEmpty()) {
            lastDeclaration = getLastElement(constructors);
          } else if (!fields.isEmpty()) {
            lastDeclaration = getLastElement(fields);
          } else if (!staticMethods.isEmpty()) {
            lastDeclaration = getLastElement(staticMethods);
          } else if (!staticFields.isEmpty()) {
            lastDeclaration = getLastElement(staticFields);
          } else {
            lastDeclaration = getLastElement(innerClasses);
          }

          methods.stream()
              .filter(method -> isBefore(method, lastDeclaration))
              .map(MethodDeclaration::getName)
              .map(JavaParser.this::getFirstLine)
              .forEach(line -> addViolation(violations, Type.Ordering_Method, file, line));
        }
      }

      private <T> T getLastElement(List<T> list) {
        return list.get(list.size() - 1);
      }

      private boolean isBefore(Node a, Node b) {
        return getFirstLine(a) < getFirstLine(b);
      }
    };
  }

  private VoidVisitorAdapter<Violations> javadocCommentViolations(Path file) {
    return new VoidVisitorAdapter<Violations>() {
      @Override
      public void visit(JavadocComment comment, Violations violations) {
        super.visit(comment, violations);

        Optional<Node> commentedNode = comment.getCommentedNode();

        if (commentedNode.isEmpty()) {
          return;
        }

        if (commentedNode.get() instanceof MethodDeclaration
            && ((MethodDeclaration) commentedNode.get()).isAnnotationPresent("Override")) {
          return;
        }

        JavadocDescription description = comment.parse().getDescription();

        if (description.isEmpty() || description.getElements().get(0) instanceof JavadocInlineTag) {
          addViolation(violations, Type.JavaDoc_MissingSummary, file, getFirstLine(comment));
          return;
        }

        long words = Pattern.compile("[\\w-]+").matcher(description.toText()).results().count();

        if (words < javaDocConfig.getMinWords()) {
          addViolation(violations, Type.JavaDoc_SummaryLength, file, getFirstLine(comment));
        }
      }
    };
  }

  private void commentViolations(Repo repo, Path file, CompilationUnit cu, Violations violations) {
    for (Comment comment : getMergedComments(cu)) {
      Optional<Node> parent = comment.getParentNode();
      String contents = comment.getContent();

      if (comment.isJavadocComment() || contents.isBlank() || parent.isEmpty()) {
        continue;
      }

      String code;
      if (parent.get() instanceof BlockStmt) {
        code = "class X { void x() {" + contents + "; } }";
      } else if (parent.get() instanceof ClassOrInterfaceDeclaration) {
        code = "class X {" + contents + " }";
      } else {
        continue;
      }

      ParseResult<CompilationUnit> result = gradestyle.util.JavaParser.get(repo).parse(code);

      if (result.isSuccessful()) {
        addViolation(violations, Type.Useless_CommentedCode, file, getFirstLine(comment));
      }
    }
  }

  private List<Comment> getMergedComments(CompilationUnit cu) {
    List<Comment> comments = new ArrayList<>();

    if (cu.getAllComments().isEmpty()) {
      return comments;
    }

    Comment lastComment = cu.getAllComments().get(0);

    for (int i = 1; i < cu.getAllComments().size(); i++) {
      Comment comment = cu.getAllComments().get(i);

      if (lastComment.isLineComment()
          && comment.isLineComment()
          && getLastLine(lastComment) == getFirstLine(comment) - 1
          && getFirstColumn(lastComment) == getFirstColumn(comment)) {
        TokenRange range =
            lastComment.getTokenRange().get().withEnd(comment.getTokenRange().get().getEnd());
        String content = lastComment.getContent() + comment.getContent();
        Optional<Node> parent = lastComment.getParentNode();

        lastComment = new LineComment(range, content);

        if (parent.isPresent()) {
          lastComment.setParentNode(parent.get());
        }
      } else {
        comments.add(lastComment);

        lastComment = comment;
      }
    }

    comments.add(lastComment);

    return comments;
  }

  private int getFirstLine(Node node) {
    return node.getRange().get().begin.line;
  }

  private int getLastLine(Node node) {
    return node.getRange().get().end.line;
  }

  private int getFirstColumn(Node node) {
    return node.getRange().get().begin.column;
  }

  private void addViolation(Violations violations, Type type, Path file, int line) {
    violations.getViolations().add(new Violation(type, file, line));
  }
}
