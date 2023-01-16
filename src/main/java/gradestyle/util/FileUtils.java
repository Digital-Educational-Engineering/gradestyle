package gradestyle.util;

import gradestyle.Repo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class FileUtils {
  public static final Path MAIN_DIR = Path.of("src/main/java");
  public static final Path TEST_DIR = Path.of("src/test/java");
  public static final Path RESOURCES_DIR = Path.of("src/main/resources");

  public static Path getMainDir(Repo repo) {
    return repo.getDir().resolve(MAIN_DIR);
  }

  public static Stream<Path> getJavaSrcFiles(Path dir) throws IOException {
    return getJavaFiles(dir.resolve(MAIN_DIR));
  }

  public static Stream<Path> getJavaFiles(Path dir) throws IOException {
    return Files.walk(dir).filter(Files::isRegularFile).filter(FileUtils::isJavaFile);
  }

  public static boolean isJavaFile(Path file) {
    return getFileExtension(file).equals("java");
  }

  public static boolean isInRepoTestDir(Repo repo, Path file) {
    return file.toAbsolutePath().startsWith(repo.getDir().resolve(TEST_DIR).toAbsolutePath());
  }

  public static Stream<Path> getFxmlResourceFiles(Path dir) throws IOException {
    return getFxmlFiles(dir.resolve(RESOURCES_DIR));
  }

  public static Stream<Path> getFxmlFiles(Path dir) throws IOException {
    return Files.walk(dir).filter(Files::isRegularFile).filter(FileUtils::isFxmlFile);
  }

  public static boolean isFxmlFile(Path file) {
    return getFileExtension(file).equals("fxml");
  }

  public static String getFileExtension(Path file) {
    String fileName = file.getFileName().toString();
    int dotIndex = fileName.lastIndexOf('.');

    return dotIndex == -1 ? "" : fileName.substring(dotIndex + 1);
  }
}
