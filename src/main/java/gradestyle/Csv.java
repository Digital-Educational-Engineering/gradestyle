package gradestyle;

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

    List<List<Object>> getRows() throws IOException;
  }

  private Path file;

  private Writer writer;

  public Csv(Path file, Writer writer) {
    this.file = file;
    this.writer = writer;
  }

  public void write() throws IOException {
    BufferedWriter bufferedWriter = Files.newBufferedWriter(file);
    String[] headers = writer.getHeaders().toArray(String[]::new);
    CSVFormat format = CSVFormat.Builder.create().setHeader(headers).build();
    CSVPrinter printer = new CSVPrinter(bufferedWriter, format);

    printer.printRecords(writer.getRows());
    printer.close();
  }
}
