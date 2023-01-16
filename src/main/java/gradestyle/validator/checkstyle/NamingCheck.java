package gradestyle.validator.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.AnnotationUtil;
import java.util.List;
import java.util.stream.Stream;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWordSet;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import org.apache.commons.lang3.StringUtils;

public class NamingCheck extends AbstractCheck {
  public static final String CLASS_NOUN_KEY = "naming.class.noun";
  public static final String METHOD_VERB_KEY = "naming.method.verb";

  private Dictionary dictionary;

  private List<String> nouns = List.of();

  private List<String> verbs = List.of();

  public NamingCheck() throws JWNLException {
    this.dictionary = Dictionary.getDefaultResourceInstance();
  }

  public void setNouns(String[] nouns) {
    this.nouns = makeAllLowerCase(nouns);
  }

  public void setVerbs(String[] verbs) {
    this.verbs = makeAllLowerCase(verbs);
  }

  private List<String> makeAllLowerCase(String[] words) {
    return Stream.of(words).map(String::toLowerCase).toList();
  }

  @Override
  public int[] getDefaultTokens() {
    return getRequiredTokens();
  }

  @Override
  public int[] getAcceptableTokens() {
    return getRequiredTokens();
  }

  @Override
  public int[] getRequiredTokens() {
    return new int[] {
      TokenTypes.CLASS_DEF, TokenTypes.METHOD_DEF,
    };
  }

  @Override
  public void visitToken(DetailAST ast) {
    DetailAST token = ast.findFirstToken(TokenTypes.IDENT);
    String[] components = StringUtils.splitByCharacterTypeCamelCase(token.getText());

    switch (ast.getType()) {
      case TokenTypes.CLASS_DEF:
        visitClass(token, components);
        break;

      case TokenTypes.METHOD_DEF:
        visitMethod(ast, token, components);
        break;

      default:
        throw new IllegalArgumentException("Unknown token type: " + token.getType());
    }
  }

  private void visitClass(DetailAST token, String[] components) {
    for (String component : components) {
      if (wordIsNoun(component)) {
        return;
      }
    }

    violation(CLASS_NOUN_KEY, token);
  }

  private void visitMethod(DetailAST ast, DetailAST token, String[] components) {
    if (AnnotationUtil.containsAnnotation(ast, "Override")) {
      return;
    }

    if (isMainMethod(ast)) {
      return;
    }

    for (String component : components) {
      if (wordIsVerb(component)) {
        return;
      }
    }

    violation(METHOD_VERB_KEY, token);
  }

  private boolean isMainMethod(DetailAST ast) {
    DetailAST modifiers = ast.findFirstToken(TokenTypes.MODIFIERS);
    DetailAST publicToken = modifiers.findFirstToken(TokenTypes.LITERAL_PUBLIC);
    DetailAST staticToken = modifiers.findFirstToken(TokenTypes.LITERAL_STATIC);

    if (publicToken == null || staticToken == null) {
      return false;
    }

    DetailAST returnType = ast.findFirstToken(TokenTypes.TYPE);
    DetailAST voidToken = returnType.findFirstToken(TokenTypes.LITERAL_VOID);

    if (voidToken == null) {
      return false;
    }

    String name = returnType.getNextSibling().getText();

    if (!name.equals("main")) {
      return false;
    }

    DetailAST params = ast.findFirstToken(TokenTypes.PARAMETERS);

    if (params.getChildCount() != 1) {
      return false;
    }

    DetailAST def = params.findFirstToken(TokenTypes.PARAMETER_DEF);

    // Only allow modifiers, type, and name.
    if (def.getChildCount() != 3) {
      return false;
    }

    DetailAST type = def.findFirstToken(TokenTypes.TYPE);
    DetailAST string = type.getFirstChild();
    DetailAST array = type.findFirstToken(TokenTypes.ARRAY_DECLARATOR);

    if (string.getType() != TokenTypes.IDENT
        || !string.getText().equals("String")
        || array == null) {
      return false;
    }

    return true;
  }

  private boolean wordIsNoun(String word) {
    return nouns.contains(word.toLowerCase()) || wordIsPOS(word, POS.NOUN);
  }

  private boolean wordIsVerb(String word) {
    return verbs.contains(word.toLowerCase()) || wordIsPOS(word, POS.VERB);
  }

  private boolean wordIsPOS(String word, POS pos) {
    try {
      IndexWordSet set = dictionary.lookupAllIndexWords(word);

      if (set.size() == 0) {
        return true;
      }

      return set.isValidPOS(pos);
    } catch (JWNLException e) {
      throw new RuntimeException(e);
    }
  }

  private void violation(String key, DetailAST token) {
    log(token.getLineNo(), key);
  }
}
