package gradestyle.validator.javaparser;

import com.github.javaparser.ParseResult;
import com.github.javaparser.Problem;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.github.javaparser.javadoc.description.JavadocInlineTag;
import com.github.javaparser.resolution.MethodAmbiguityException;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import gradestyle.Repo;
import gradestyle.config.CommentingConfig;
import gradestyle.config.Config;
import gradestyle.config.OrderConfig;
import gradestyle.config.OrderConfig.OrderElement;
import gradestyle.config.javadocconfig.JavadocClassConfig;
import gradestyle.config.javadocconfig.JavadocConstructorConfig;
import gradestyle.config.javadocconfig.JavadocFieldConfig;
import gradestyle.config.javadocconfig.JavadocMethodConfig;
import gradestyle.config.programmingpracticeconfig.FinalizeOverrideConfig;
import gradestyle.config.programmingpracticeconfig.UnqualifiedStaticAccessConfig;
import gradestyle.util.FileUtils;
import gradestyle.validator.Category;
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
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class JavaParser implements Validator {
  private CommentingConfig commentingConfig;

  private JavadocClassConfig javadocClassConfig;
  private JavadocMethodConfig javadocMethodConfig;
  private JavadocFieldConfig javadocFieldConfig;
  private JavadocConstructorConfig javadocConstructorConfig;
  private FinalizeOverrideConfig finalizeOverrideConfig;
  private UnqualifiedStaticAccessConfig unqualifiedStaticAccessConfig;
  private OrderConfig orderConfig;

  @Override
  public void setup(Config config) {
    this.commentingConfig = config.getCategoryConfig(CommentingConfig.class);

    this.javadocClassConfig = config.getCategoryConfig(JavadocClassConfig.class);
    this.javadocMethodConfig = config.getCategoryConfig(JavadocMethodConfig.class);
    this.javadocFieldConfig = config.getCategoryConfig(JavadocFieldConfig.class);
    this.javadocConstructorConfig = config.getCategoryConfig(JavadocConstructorConfig.class);
    this.orderConfig = config.getCategoryConfig(OrderConfig.class);
    this.finalizeOverrideConfig = config.getCategoryConfig(FinalizeOverrideConfig.class);
    this.unqualifiedStaticAccessConfig =
        config.getCategoryConfig(UnqualifiedStaticAccessConfig.class);
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

    com.github.javaparser.JavaParser javaParser = gradestyle.util.JavaParser.get(repo);

    for (Path file : FileUtils.getJavaSrcFiles(repo.getDir()).toList()) {
      ParseResult<CompilationUnit> result = javaParser.parse(file);

      if (!result.isSuccessful()) {
        List<Problem> problems = result.getProblems();
        int numProblems = problems.size();

        if (numProblems == 1) {
          throw new ValidatorException(file, problems.get(0).getVerboseMessage());
        } else if (numProblems > 1) {
          StringBuilder message = new StringBuilder();
          for (int i = 0; i < numProblems - 1; i++) {
            message.append(String.valueOf(i)).append(") ");
            message.append(problems.get(i).getMessage()).append("\n");
          }
        }
      }

      CompilationUnit cu = result.getResult().get();

      privateFieldViolations(file).visit(cu, violations);

      if (orderConfig != null) {
        List<OrderElement> ordering = orderConfig.getOrdering();
        classOrderingViolations(file, ordering).visit(cu, violations);
      }

      if (commentingConfig != null) {
        IOException e = commentFrequencyViolations(file).visit(cu, violations);

        if (e != null) {
          throw e;
        }

        commentMeaningViolations(file).visit(cu, violations);
      }

      if (javadocFieldConfig != null) {
        javadocFieldViolations(file).visit(cu, violations);
      }
      if (javadocMethodConfig != null) {
        javadocMethodViolations(file).visit(cu, violations);
      }
      if (javadocConstructorConfig != null) {
        javadocConstructorViolations(file).visit(cu, violations);
      }
      if (javadocClassConfig != null) {
        javadocClassViolations(file).visit(cu, violations);
      }

      if (finalizeOverrideConfig != null) {
        finalizeNotAllowedViolation(file).visit(cu, violations);
      }

      if (unqualifiedStaticAccessConfig != null) {
        unqualifiedStaticMethodViolations(file).visit(cu, violations);
        unqualifiedStaticFieldViolations(file).visit(cu, violations);
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

  private VoidVisitorAdapter<Violations> unqualifiedStaticMethodViolations(Path file) {
    return new VoidVisitorAdapter<Violations>() {
      @Override
      public void visit(MethodCallExpr methodCall, Violations violations) {
        super.visit(methodCall, violations);
        try {
          ResolvedMethodDeclaration resolvedMethod = methodCall.resolve();

          if (!resolvedMethod.isStatic()) {
            return;
          }

          Expression scope = methodCall.getScope().orElse(null);
          if (scope == null) {
            return;
          }

          // Check if any part of the access chain is instance-based
          if (isInstanceAccess(scope) || isEnumConstant(scope)) {
            addViolation(
                violations, Type.UnqualifiedStaticAccess_Method, file, getFirstLine(methodCall));
          }
        } catch (UnsolvedSymbolException e) {
          // Handle unresolved symbol case gracefully
          // System.err.println("Unresolved method call: " + methodCall + " at file: " + file);
        } catch (UnsupportedOperationException e) {
          // System.err.println("Unsupported method call: " + methodCall + " at file: " + file);
        } catch (MethodAmbiguityException e) {
          // System.err.println("Ambiguous method call: " + methodCall + " at file: " + file);
        } catch (Exception e) {
          System.err.println("Unknown exception thrown during symbol resolution: " + e.getClass());
        }
      }

      private boolean isInstanceAccess(Expression expr) {
        if (expr instanceof NameExpr) {
          try {
            ResolvedValueDeclaration v = expr.asNameExpr().resolve();

            return !(v instanceof ResolvedFieldDeclaration
                && ((ResolvedFieldDeclaration) v).isStatic());
          } catch (UnsolvedSymbolException e) {
            return false;
          }

        } else if (expr.isFieldAccessExpr()) {
          ResolvedValueDeclaration v = expr.asFieldAccessExpr().resolve();

          if (v.isType()) {
            if (v.asType().isEnum()) {
              return false;
            }
          }
        }

        return true;
      }

      private boolean isEnumConstant(Expression expr) {
        if (expr instanceof NameExpr) {
          try {
            ResolvedValueDeclaration resolvedValue = ((NameExpr) expr).resolve();
            if (resolvedValue.isEnumConstant()) {

              return true; // The field is an enum constant
            }
          } catch (UnsolvedSymbolException e) {
            return false;
          }
        } else if (expr instanceof FieldAccessExpr) {
          try {
            ResolvedValueDeclaration resolvedValue = ((FieldAccessExpr) expr).resolve();
            if (resolvedValue.isEnumConstant()) {
              return true; // The field is an enum constant
            }
          } catch (UnsolvedSymbolException e) {
            return false;
          }
        }
        return false;
      }
    };
  }

  private VoidVisitorAdapter<Violations> unqualifiedStaticFieldViolations(Path file) {
    return new VoidVisitorAdapter<Violations>() {
      @Override
      public void visit(FieldAccessExpr fieldAccess, Violations violations) {
        super.visit(fieldAccess, violations);

        try {
          ResolvedValueDeclaration v = fieldAccess.resolve();
          if (!v.isField()) {
            return;
          }
          ResolvedFieldDeclaration resolvedField = v.asField();

          // skip non static calls
          if (!resolvedField.isStatic()) {
            return;
          }

          Expression scope = fieldAccess.getScope();

          // null scope means the method was called from inside the class, which is a valid use of a
          // static method.
          if (scope == null) {
            return;
          }

          // If the field is accessed via an instance or enum constant, flag it.
          if (isInstanceAccess(scope) || isEnumConstant(scope)) {
            addViolation(
                violations, Type.UnqualifiedStaticAccess_Field, file, getFirstLine(fieldAccess));
          }

        } catch (UnsolvedSymbolException e) {
        } catch (UnsupportedOperationException e) {
          // may want to update this in the future if javaparser gets updated to resolve wild cards.
        } catch (Exception e) {
          System.err.println("Unknown error occurred during parsing: " + e.getClass());
        }
      }

      private boolean isInstanceAccess(Expression expr) {
        if (expr instanceof FieldAccessExpr) {
          return isInstanceAccess(((FieldAccessExpr) expr).getScope());
        } else if (expr instanceof NameExpr) {
          try {
            ResolvedValueDeclaration resolvedValue = ((NameExpr) expr).resolve();
            return !(resolvedValue instanceof ResolvedFieldDeclaration
                && ((ResolvedFieldDeclaration) resolvedValue).isStatic());
          } catch (UnsolvedSymbolException e) {
            return false;
          }
        }
        return true;
      }

      private boolean isEnumConstant(Expression expr) {
        if (expr instanceof NameExpr) {
          try {
            ResolvedValueDeclaration resolvedValue = ((NameExpr) expr).resolve();
            if (resolvedValue.isEnumConstant()) {

              return true;
            }
          } catch (UnsolvedSymbolException e) {
            return false;
          }
        } else if (expr instanceof FieldAccessExpr) {
          try {
            ResolvedValueDeclaration resolvedValue = ((FieldAccessExpr) expr).resolve();
            if (resolvedValue.isEnumConstant()) {
              return true;
            }
          } catch (UnsolvedSymbolException e) {
            return false;
          }
        }
        return false;
      }
    };
  }

  private VoidVisitorAdapter<Violations> finalizeNotAllowedViolation(Path file) {
    return new VoidVisitorAdapter<Violations>() {
      @Override
      public void visit(MethodDeclaration method, Violations violations) {
        super.visit(method, violations);

        if (method.getNameAsString().equals("finalize")
            && method.getParameters().isEmpty()
            && method.getTypeAsString().equals("void")
            && (method.getModifiers().contains(Modifier.publicModifier())
                || method.getModifiers().contains(Modifier.protectedModifier()))) {

          addViolation(violations, Type.FinalizeOverride, file, getFirstLine(method));
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

        if (commentingConfig == null || distance >= commentingConfig.getLevenshteinDistance()) {
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

        addViolation(violations, Type.PrivateInstances, file, getFirstLine(decl.getVariable(0)));
      }
    };
  }

  private VoidVisitorAdapter<Violations> classOrderingViolations(
      Path file, List<OrderElement> ordering) {
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
        // Group members by type
        @SuppressWarnings("rawtypes")
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

        List<MethodDeclaration> staticMethods =
            decl.getMembers().stream()
                .filter(BodyDeclaration::isMethodDeclaration)
                .map(BodyDeclaration::asMethodDeclaration)
                .filter(MethodDeclaration::isStatic)
                .toList();

        List<FieldDeclaration> instanceFields =
            decl.getMembers().stream()
                .filter(BodyDeclaration::isFieldDeclaration)
                .map(BodyDeclaration::asFieldDeclaration)
                .filter(Predicate.not(FieldDeclaration::isStatic))
                .toList();

        List<ConstructorDeclaration> constructors =
            decl.getMembers().stream()
                .filter(BodyDeclaration::isConstructorDeclaration)
                .map(BodyDeclaration::asConstructorDeclaration)
                .toList();

        List<MethodDeclaration> instanceMethods =
            decl.getMembers().stream()
                .filter(BodyDeclaration::isMethodDeclaration)
                .map(BodyDeclaration::asMethodDeclaration)
                .filter(Predicate.not(MethodDeclaration::isStatic))
                .toList();

        Map<OrderElement, List<? extends Node>> groups =
            Map.of(
                OrderElement.InnerClasses, innerClasses,
                OrderElement.StaticFields, staticFields,
                OrderElement.StaticMethods, staticMethods,
                OrderElement.InstanceFields, instanceFields,
                OrderElement.Constructors, constructors,
                OrderElement.InstanceMethods, instanceMethods);

        for (int i = 0; i < ordering.size(); i++) {
          OrderElement orderElement = ordering.get(i);
          List<? extends Node> currentGroup = groups.get(orderElement);

          for (Node currentNode : currentGroup) {
            for (int j = i + 1; j < ordering.size(); j++) {
              OrderElement order = ordering.get(j);
              List<? extends Node> afterGroup = groups.get(order);

              for (Node node : afterGroup) {
                if (isBefore(node, currentNode)) {
                  addOrderViolation(
                      violations,
                      Type.valueOf("Ordering_" + order.name()),
                      file,
                      getNodeName(node),
                      getNodeName(currentNode),
                      getFirstLine(node));
                  // only have 1 violation for each out of order-violation
                  continue;
                }
              }
            }
          }
        }
      }

      private String getNodeName(Node node) {
        if (node instanceof MethodDeclaration) {
          return ((MethodDeclaration) node).getNameAsString();
        } else if (node instanceof FieldDeclaration) {
          return ((FieldDeclaration) node).getVariables().get(0).getNameAsString();
        } else if (node instanceof ConstructorDeclaration) {
          return ((ConstructorDeclaration) node).getNameAsString();
        } else if (node instanceof EnumDeclaration) {
          return ((EnumDeclaration) node).getNameAsString();
        } else if (node instanceof ClassOrInterfaceDeclaration) {
          return ((ClassOrInterfaceDeclaration) node).getNameAsString();
        } else if (node instanceof TypeDeclaration<?>) {
          return ((TypeDeclaration<?>) node).getNameAsString();
        } else if (node instanceof ImportDeclaration) {
          return ((ImportDeclaration) node).getNameAsString();
        } else if (node instanceof AnnotationDeclaration) {
          return ((AnnotationDeclaration) node).getNameAsString();
        } else if (node instanceof InitializerDeclaration) {
          return "Static Initializer Block";
        } else {
          return "unknown";
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

  private void addOrderViolation(
      Violations violations, Type type, Path file, String element, String reference, int line) {
    Violation violation = new Violation(type, file, line);
    String message = String.format(type.getMessage(), element, reference);
    OrderViolationResults.addViolation(violation, message);
    violations.getViolations().add(violation);
  }

  private VoidVisitorAdapter<Violations> javadocClassViolations(Path file) {
    return new VoidVisitorAdapter<Violations>() {
      @Override
      public void visit(ClassOrInterfaceDeclaration node, Violations violations) {
        super.visit(node, violations);
        Optional<Comment> comment = node.getComment();
        if (comment.isEmpty()) {
          addViolation(violations, Type.JavadocClass_Missing, file, getFirstLine(node));
        } else if (comment.get() instanceof JavadocComment) {
          // Validate Javadoc if it exists and is a JavadocComment
          validateJavadoc((JavadocComment) comment.get(), violations, file, Category.JavadocClass);
        } else {
          // Optionally handle cases where the comment is not a JavadocComment
          addViolation(violations, Type.Javadoc_Invalid, file, getFirstLine(node));
        }
      }
    };
  }

  private VoidVisitorAdapter<Violations> javadocFieldViolations(Path file) {
    return new VoidVisitorAdapter<Violations>() {
      @Override
      public void visit(FieldDeclaration node, Violations violations) {
        super.visit(node, violations);
        Optional<Comment> comment = node.getComment();
        if (comment.isEmpty()) {
          addViolation(violations, Type.JavadocField_Missing, file, getFirstLine(node));
        } else if (comment.get() instanceof JavadocComment) {
          // Validate Javadoc if it exists and is a JavadocComment
          validateJavadoc((JavadocComment) comment.get(), violations, file, Category.JavadocField);
        } else {
          // Optionally handle cases where the comment is not a JavadocComment
          addViolation(violations, Type.Javadoc_Invalid, file, getFirstLine(node));
        }
      }
    };
  }

  private VoidVisitorAdapter<Violations> javadocConstructorViolations(Path file) {
    return new VoidVisitorAdapter<Violations>() {
      @Override
      public void visit(ConstructorDeclaration node, Violations violations) {
        super.visit(node, violations);
        Optional<Comment> comment = node.getComment();
        if (comment.isEmpty()) {
          addViolation(violations, Type.JavadocConstructor_Missing, file, getFirstLine(node));
        } else if (comment.get() instanceof JavadocComment) {
          // Validate Javadoc if it exists and is a JavadocComment
          validateJavadoc(
              (JavadocComment) comment.get(), violations, file, Category.JavadocConstructor);
        } else {
          // Optionally handle cases where the comment is not a JavadocComment
          addViolation(violations, Type.Javadoc_Invalid, file, getFirstLine(node));
        }
      }
    };
  }

  private VoidVisitorAdapter<Violations> javadocMethodViolations(Path file) {
    return new VoidVisitorAdapter<Violations>() {
      @Override
      public void visit(MethodDeclaration node, Violations violations) {
        super.visit(node, violations);

        if (node.getAnnotationByName("Override").isPresent()) {
          return;
        }

        Optional<Comment> comment = node.getComment();

        if (comment.isEmpty()) {
          addViolation(violations, Type.JavadocMethod_Missing, file, getFirstLine(node));
        } else if (comment.get() instanceof JavadocComment) {
          // Validate Javadoc if it exists and is a JavadocComment
          validateJavadoc((JavadocComment) comment.get(), violations, file, Category.JavadocMethod);
        } else {
          // handle cases where the comment is not a javadocComment
          addViolation(violations, Type.Javadoc_Invalid, file, getFirstLine(node));
        }
      }
    };
  }

  private void validateJavadoc(
      JavadocComment comment, Violations violations, Path file, Category category) {
    JavadocDescription description = comment.parse().getDescription();

    if (description.isEmpty() || description.getElements().get(0) instanceof JavadocInlineTag) {
      addViolation(violations, Type.Javadoc_MissingSummary, file, getFirstLine(comment));
      return;
    }

    long words = Pattern.compile("[\\w-]+").matcher(description.toText()).results().count();

    int minWords;

    switch (category) {
      case JavadocClass:
        minWords = javadocClassConfig.getMinWords();
        break;

      case JavadocMethod:
        minWords = javadocMethodConfig.getMinWords();
        break;

      case JavadocField:
        minWords = javadocFieldConfig.getMinWords();
        break;
      case JavadocConstructor:
        minWords = javadocConstructorConfig.getMinWords();
        break;

      default:
        throw new IllegalArgumentException("Javadoc category not found when finding minWords");
    }
    if (words < minWords) {
      addViolation(violations, Type.Javadoc_SummaryLength, file, getFirstLine(comment));
    }
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
        code = "class X { void x() {" + contents + " } }";
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
