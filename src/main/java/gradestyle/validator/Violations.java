package gradestyle.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Violations {
  private List<Violation> violations;

  public Violations() {
    this(new ArrayList<>());
  }

  public Violations(List<Violation> violations) {
    this.violations = violations;
  }

  private Violations(Stream<Violation> violations) {
    this(violations.collect(Collectors.toList()));
  }

  public Violations filterByType(Type type) {
    return new Violations(violations.stream().filter(v -> v.getType() == type));
  }

  public Violations filterByCategory(Category category) {
    return new Violations(violations.stream().filter(v -> v.getType().getCategory() == category));
  }

  public List<Violation> getCategoryExamples(Category category, int examples) {
    List<Violation> violations =
        filterByCategory(category).getViolations().stream().distinct().collect(Collectors.toList());

    Collections.shuffle(violations);

    return violations.subList(0, Math.min(violations.size(), examples));
  }

  public List<Violation> getViolations() {
    return violations;
  }
}
