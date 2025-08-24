package gradestyle.validator.javaparser;

import gradestyle.validator.Violation;
import java.util.HashMap;
import java.util.Map;

public class OrderViolationResults {
  private static Map<Violation, String> violationMessages = new HashMap<>();

  public static void addViolation(Violation violation, String message) {
    violationMessages.put(violation, message);
  }

  public static String getMessage(Violation violation) {
    return violationMessages.get(violation);
  }
}
