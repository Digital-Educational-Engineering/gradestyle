package gradestyle.validator;

import gradestyle.Markdown;
import gradestyle.Repo;
import gradestyle.config.CategoryConfig;
import gradestyle.config.Config;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import net.steppschuh.markdowngenerator.link.Link;
import net.steppschuh.markdowngenerator.table.Table;
import net.steppschuh.markdowngenerator.text.code.Code;
import net.steppschuh.markdowngenerator.text.emphasis.BoldText;

public class ValidationMarkdown implements Markdown.Writer<ValidationResult> {
  private Config config;

  public ValidationMarkdown(Config config) {
    this.config = config;
  }

  @Override
  public String getFileName(ValidationResult result) throws IOException {
    return result.getRepo().getName();
  }

  @Override
  public String write(ValidationResult result) throws IOException {
    StringBuilder sb = new StringBuilder();

    Markdown.title(sb, config.getStyleFeedback().getFeedbackTitle());
    Markdown.message(sb, config.getStyleFeedback().getFeedbackMessage());

    if (result.getError() == null) {
      scoreTable(sb, result, config.getCategoryConfigs());
      feedback(sb, result, config.getCategoryConfigs());
    } else {
      Markdown.message(sb, config.getStyleFeedback().getFeedbackError());
    }

    Markdown.footer(sb, result.getRepo());

    return sb.toString();
  }

  public static void scoreTable(
      StringBuilder sb, ValidationResult result, List<CategoryConfig> configs) throws IOException {
    Markdown.heading(sb, "Scores", 2);

    Table.Builder builder =
        new Table.Builder()
            .addRow(new BoldText("Category"), new BoldText("Score"))
            .withAlignments(Table.ALIGN_LEFT, Table.ALIGN_CENTER);

    Map<Category, Integer> scores = Category.getCategoryScores(result, configs);

    int total = 0;
    int max = 0;

    for (CategoryConfig config : configs) {
      Category category = config.getCategory();
      int score = scores.get(category);
      int maxScore = config.getScores().size();
      String text = score + " / " + maxScore;

      total += score;
      max += maxScore;

      builder.addRow(category, text);
    }

    builder.addRow(new BoldText("Total"), new BoldText(total + " / " + max));

    sb.append(builder.build()).append("\n\n");
  }

  public static void feedback(
      StringBuilder sb, ValidationResult result, List<CategoryConfig> configs) {
    boolean generatedHeader = false;

    for (CategoryConfig config : configs) {
      List<Violation> examples =
          result.getViolations().getCategoryExamples(config.getCategory(), config.getExamples());

      if (examples.isEmpty()) {
        continue;
      }

      if (!generatedHeader) {
        Markdown.heading(sb, "Feedback", 2);
        generatedHeader = true;
      }

      Markdown.heading(sb, config.getCategory().toString(), 3);

      for (Violation example : examples) {
        Repo repo = result.getRepo();
        Path relative =
            repo.getDir().toAbsolutePath().relativize(example.getPath().toAbsolutePath());

        String fileName = relative + ":" + example.getLine();
        String url = repo.getFileLineUrl(relative, example.getLine());

        if (example.getEndLine() != -1) {
          fileName += "-" + example.getEndLine();
          url = repo.getFileRangeUrl(relative, example.getLine(), example.getEndLine());
        }

        Object file = new Code(fileName);

        if (url != null) {
          file = new Link(file, url);
        }

        sb.append(file).append(": ").append(example.getType().getMessage()).append("\n\n");
      }
    }
  }
}
