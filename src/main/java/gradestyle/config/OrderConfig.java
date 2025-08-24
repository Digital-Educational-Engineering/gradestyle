package gradestyle.config;

import java.util.List;

public class OrderConfig extends CategoryConfig {
  private List<OrderElement> order;

  public OrderConfig(CategoryConfig config, List<OrderElement> ordering) {
    super(config.getCategory(), config.getExamples(), config.getMode(), config.getScores());
    this.order = ordering;
  }

  public enum OrderElement {
    InnerClasses,
    StaticFields,
    StaticMethods,
    InstanceFields,
    Constructors,
    InstanceMethods;
  }

  public static List<OrderElement> defaultOrdering() {
    return List.of(
        OrderElement.InnerClasses,
        OrderElement.StaticFields,
        OrderElement.StaticMethods,
        OrderElement.InstanceFields,
        OrderElement.Constructors,
        OrderElement.InstanceMethods);
  }

  public List<OrderElement> getOrdering() {
    return order;
  }
}
