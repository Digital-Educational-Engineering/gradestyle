package gradestyle;

import gradestyle.validator.ValidationResult;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class Csv {
  public interface Writer {
    List<String> getHeaders() throws IOException;

    List<Object> getRow(ValidationResult result) throws IOException;
  }

  private Path file;

  private Writer writer;
  private CSVPrinter printer;

  public Csv(Path file, Writer writer) throws IOException {
    this.file = file;
    this.writer = writer;

    BufferedWriter bufferedWriter = Files.newBufferedWriter(file);
    String[] headers = writer.getHeaders().toArray(String[]::new);
    CSVFormat format = CSVFormat.Builder.create().setHeader(headers).build();
    this.printer = new CSVPrinter(bufferedWriter, format);
  }

  public void write(ValidationResult result) throws IOException {
    printer.printRecord(writer.getRow(result));
    printer.flush();
  }

  public void close() throws IOException {
    printer.close();
  }
}
