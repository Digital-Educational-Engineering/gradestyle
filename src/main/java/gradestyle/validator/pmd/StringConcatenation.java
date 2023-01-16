package gradestyle.validator.pmd;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTAssignmentOperator;
import net.sourceforge.pmd.lang.java.ast.ASTDoStatement;
import net.sourceforge.pmd.lang.java.ast.ASTForStatement;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTStatementExpression;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.ast.ASTWhileStatement;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;

public class StringConcatenation extends AbstractJavaRule {
  public StringConcatenation() {
    addRuleChainVisit(ASTVariableDeclaratorId.class);
  }

  @Override
  public Object visit(ASTVariableDeclaratorId node, Object data) {
    if (!TypeTestUtil.isA(String.class, node)
        || node.hasArrayType()
        || node.getNthParent(3) instanceof ASTForStatement) {
      return data;
    }

    for (NameOccurrence no : node.getUsages()) {
      Node name = no.getLocation();

      if (!isInLoop(name, node)) {
        continue;
      }

      ASTStatementExpression statement = name.getFirstParentOfType(ASTStatementExpression.class);

      if (statement == null) {
        continue;
      }

      if (statement.getNumChildren() == 0
          || !(statement.getChild(0) instanceof ASTPrimaryExpression)) {
        continue;
      }

      ASTName astName = statement.getChild(0).getFirstDescendantOfType(ASTName.class);

      if (astName == null) {
        continue;
      }

      ASTAssignmentOperator assignmentOperator =
          statement.getFirstDescendantOfType(ASTAssignmentOperator.class);

      if (assignmentOperator == null) {
        continue;
      }

      if (assignmentOperator.hasImageEqualTo("+=")
          || (!astName.equals(name) && astName.hasImageEqualTo(name.getImage()))) {
        asCtx(data).addViolation(assignmentOperator);
      }
    }

    return data;
  }

  private boolean isInLoop(Node node, ASTVariableDeclaratorId decl) {
    Stream<AbstractNode> fors = getParentNodes(node, ASTForStatement.class);
    Stream<AbstractNode> whiles = getParentNodes(node, ASTWhileStatement.class);
    Stream<AbstractNode> dos = getParentNodes(node, ASTDoStatement.class);

    List<AbstractNode> loops = Stream.of(fors, whiles, dos).flatMap(Function.identity()).toList();

    Stream<ASTVariableDeclaratorId> decls =
        loops.stream()
            .map(loop -> loop.findDescendantsOfType(ASTVariableDeclaratorId.class))
            .flatMap(List::stream);

    return !loops.isEmpty() && decls.filter(d -> d.equals(decl)).findFirst().isEmpty();
  }

  private <T extends AbstractNode> Stream<AbstractNode> getParentNodes(Node node, Class<T> type) {
    return node.getParentsOfType(type).stream().map(AbstractNode.class::cast);
  }
}
