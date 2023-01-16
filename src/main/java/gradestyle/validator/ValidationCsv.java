package gradestyle.validator;

import gradestyle.Csv.Writer;
import gradestyle.config.CategoryConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class ValidationCsv implements Writer {
  private List<CategoryConfig> configs;

  private List<ValidationResult> results;

  public ValidationCsv(List<CategoryConfig> configs, List<ValidationResult> results) {
    this.configs = configs;
    this.results = results;
  }

  @Override
  public List<String> getHeaders() throws IOException {
    Stream<String> typeHeaders =
        configs.stream()
            .map(CategoryConfig::getCategory)
            .map(Category::getTypes)
            .flatMap(Collection::stream)
            .map(Object::toString);

    Stream<String> categoryNames =
        configs.stream().map(CategoryConfig::getCategory).map(Object::toString);

    Stream<String> scoreHeaders =
        Stream.concat(categoryNames, Stream.of("Total")).map(c -> c + " Score");

    Stream<Stream<String>> headers =
        Stream.of(Stream.of("Name", "Hash"), typeHeaders, scoreHeaders);

    return headers.flatMap(Function.identity()).toList();
  }

  @Override
  public List<List<Object>> getRows() throws IOException {
    List<List<Object>> rows = new ArrayList<>();

    for (ValidationResult result : results) {
      rows.add(getRow(result));
    }

    return rows;
  }

  private List<Object> getRow(ValidationResult result) throws IOException {
    List<Object> row = new ArrayList<>();

    row.add(result.getRepo().getName());
    row.add(result.getRepo().getCommit());

    if (result.getError() != null) {
      int remaining =
          configs.stream()
                  .map(CategoryConfig::getCategory)
                  .map(Category::getTypes)
                  .mapToInt(List::size)
                  .sum()
              + configs.size()
              + 1;

      row.addAll(Collections.nCopies(remaining, "N/A"));

      return row;
    }

    for (CategoryConfig config : configs) {
      for (Type type : config.getCategory().getTypes()) {
        row.add(result.getViolations().filterByType(type).getViolations().size());
      }
    }

    Map<Category, Integer> categoryScore = Category.getCategoryScores(result, configs);
    List<Integer> categoryScores =
        configs.stream().map(CategoryConfig::getCategory).map(categoryScore::get).toList();

    row.addAll(categoryScores);
    row.add(categoryScores.stream().mapToInt(Integer::intValue).sum());

    return row;
  }
}
