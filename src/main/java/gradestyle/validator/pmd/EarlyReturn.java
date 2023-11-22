package gradestyle.validator.pmd;

import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTBlockStatement;
import net.sourceforge.pmd.lang.java.ast.ASTIfStatement;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTStatement;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

public class EarlyReturn extends AbstractJavaRule {
  public EarlyReturn() {
    addRuleChainVisit(ASTMethodDeclaration.class);
  }

  @Override
  public Object visit(ASTMethodDeclaration method, Object data) {
    ASTBlock body = method.getBody();

    if (body == null || body.getNumChildren() == 0) {
      return data;
    }

    JavaNode blockNode = body.getChild(body.getNumChildren() - 1);

    if (blockNode instanceof ASTBlockStatement blockStmt) {
      JavaNode stmtNode = blockStmt.getChild(0);

      if (stmtNode instanceof ASTStatement stmt) {
        if (stmt.getChild(0) instanceof ASTIfStatement conditional) {
          asCtx(data).addViolation(conditional);
        }
      }
    }

    return data;
  }
}
