package gradestyle;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.steppschuh.markdowngenerator.link.Link;
import net.steppschuh.markdowngenerator.rule.HorizontalRule;
import net.steppschuh.markdowngenerator.text.code.Code;
import net.steppschuh.markdowngenerator.text.emphasis.ItalicText;
import net.steppschuh.markdowngenerator.text.heading.Heading;

public class Markdown<T> {
  public interface Writer<T> {
    String getFileName(T item) throws IOException;

    String write(T item) throws IOException;
  }

  public static void title(StringBuilder sb, String title) {
    heading(sb, title, 1);
  }

  public static void heading(StringBuilder sb, String title, int level) {
    if (title != null) {
      sb.append(new Heading(title, level)).append("\n\n");
    }
  }

  public static void message(StringBuilder sb, String message) {
    if (message != null) {
      sb.append(message).append("\n\n");
    }
  }

  public static void footer(StringBuilder sb, Repo repo) {
    if (repo.getCommit() == null) {
      return;
    }

    sb.append(new HorizontalRule()).append("\n\n");

    Object hash = new Code(repo.getCommit().substring(0, 7)).toString();

    if (repo.getCommitUrl() != null) {
      hash = new Link(hash, repo.getCommitUrl());
    }

    String commit = "Report generated on commit " + hash + ".";
    sb.append(new ItalicText(commit)).append("\n");
  }

  private Path dir;

  private Writer<T> writer;

  public Markdown(Path dir, Writer<T> writer) {
    this.dir = dir;
    this.writer = writer;
  }

  public void write(List<T> items) throws IOException {
    Files.createDirectories(dir);

    for (T item : items) {
      Path file = dir.resolve(writer.getFileName(item) + ".md");
      BufferedWriter bufferedWriter = Files.newBufferedWriter(file);

      bufferedWriter.append(writer.write(item));
      bufferedWriter.close();
    }
  }
}
