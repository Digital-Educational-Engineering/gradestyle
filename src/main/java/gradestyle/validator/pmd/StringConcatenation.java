package gradestyle.validator.pmd;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.ast.impl.AbstractNode;
import net.sourceforge.pmd.lang.java.ast.ASTAssignableExpr.ASTNamedReferenceExpr;
import net.sourceforge.pmd.lang.java.ast.ASTAssignmentExpression;
import net.sourceforge.pmd.lang.java.ast.ASTDoStatement;
import net.sourceforge.pmd.lang.java.ast.ASTExpressionStatement;
import net.sourceforge.pmd.lang.java.ast.ASTForStatement;
import net.sourceforge.pmd.lang.java.ast.ASTInfixExpression;
import net.sourceforge.pmd.lang.java.ast.ASTVariableId;
import net.sourceforge.pmd.lang.java.ast.ASTWhileStatement;
import net.sourceforge.pmd.lang.java.ast.AssignmentOp;
import net.sourceforge.pmd.lang.java.ast.BinaryOp;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRulechainRule;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;

public class StringConcatenation extends AbstractJavaRulechainRule {
  public StringConcatenation() {
    super(ASTVariableId.class);
  }

  @Override
  public Object visit(ASTVariableId node, Object data) {
    if (!TypeTestUtil.isA(String.class, node)
        || node.hasArrayType()
        || node.ancestors().get(3 - 1) instanceof ASTForStatement) {

      return data;
    }

    for (ASTNamedReferenceExpr no : node.getLocalUsages()) {

      int parentIndex = no.getIndexInParent();
      Node name = no.getParent().getChild(parentIndex);

      if (!isInLoop(name, node)) {
        continue;
      }

      ASTExpressionStatement statement = name.ancestors(ASTExpressionStatement.class).first();

      if (statement == null) {
        continue;
      }

      ASTAssignmentExpression assignmentExpr = statement.firstChild(ASTAssignmentExpression.class);

      if (assignmentExpr == null) {
        continue;
      }

      // Proceed with analyzing the assignment
      ASTNamedReferenceExpr lhs = assignmentExpr.firstChild(ASTNamedReferenceExpr.class);

      if (lhs == null || !lhs.getReferencedSym().equals(node.getSymbol())) {
        continue;
      }

      if (assignmentExpr.getOperator() == AssignmentOp.ADD_ASSIGN) {
        asCtx(data)
            .addViolationWithMessage(
                assignmentExpr, "Avoid string concatenation in loops; use StringBuilder instead.");
      } else if (assignmentExpr.getOperator() == AssignmentOp.ASSIGN) {
        // Check if RHS is an infix expression with +
        ASTInfixExpression rhs = assignmentExpr.firstChild(ASTInfixExpression.class);

        if (rhs != null && rhs.getOperator() == BinaryOp.ADD) {
          asCtx(data)
              .addViolationWithMessage(
                  assignmentExpr,
                  "Avoid string concatenation in loops; use StringBuilder instead.");
        }
      }
    }

    return data;
  }

  @SuppressWarnings("rawtypes")
  private boolean isInLoop(Node node, ASTVariableId decl) {
    Stream<AbstractNode> fors = getParentNodes(node, ASTForStatement.class);
    Stream<AbstractNode> whiles = getParentNodes(node, ASTWhileStatement.class);
    Stream<AbstractNode> dos = getParentNodes(node, ASTDoStatement.class);

    List<AbstractNode> loops = Stream.of(fors, whiles, dos).flatMap(Function.identity()).toList();

    Stream<ASTVariableId> decls =
        loops.stream()
            .map(loop -> loop.descendants(ASTVariableId.class).toList())
            .flatMap(List::stream);

    return !loops.isEmpty() && decls.filter(d -> d.equals(decl)).findFirst().isEmpty();
  }

  @SuppressWarnings("rawtypes")
  private <T extends AbstractNode> Stream<AbstractNode> getParentNodes(Node node, Class<T> type) {
    return node.ancestors(type).toList().stream().map(AbstractNode.class::cast);
  }
}
