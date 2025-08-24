package gradestyle.util;

import gradestyle.Repo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public class FileUtils {
  public static final Path MAIN_DIR = Path.of("src/main/java");
  public static final Path TEST_DIR = Path.of("src/test/java");
  public static final Path RESOURCES_DIR = Path.of("src/main/resources");

  public static Path getMainDir(Repo repo) throws IOException {
    return findFirstMatchingDir(repo.getDir(), MAIN_DIR.toString())
        .orElseThrow(() -> new IllegalStateException("Main dir not found"));
  }

  public static Stream<Path> getJavaSrcFiles(Path dir) throws IOException {
    return getJavaFiles(
        findFirstMatchingDir(dir, MAIN_DIR.toString())
            .orElseThrow(() -> new IllegalStateException("Main dir not found")));
  }

  public static Stream<Path> getJavaFiles(Path dir) throws IOException {
    return Files.walk(dir).filter(Files::isRegularFile).filter(FileUtils::isJavaFile);
  }

  public static boolean isJavaFile(Path file) {
    return getFileExtension(file).equals("java");
  }

  public static boolean isInRepoTestDir(Repo repo, Path file) throws IOException {
    Optional<Path> testDir = findFirstMatchingDir(repo.getDir(), TEST_DIR.toString());
    return testDir
        .map(path -> file.toAbsolutePath().startsWith(path.toAbsolutePath()))
        .orElse(false);
  }

  public static Stream<Path> getFxmlResourceFiles(Path dir) throws IOException {
    return getFxmlFiles(
        findFirstMatchingDir(dir, RESOURCES_DIR.toString())
            .orElseThrow(() -> new IllegalStateException("Resources dir not found")));
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

  public static String getPomFilePath(Repo repo) throws IOException {
    Optional<Path> pom =
        Files.walk(repo.getDir())
            .filter(Files::isRegularFile)
            .filter(p -> p.getFileName().toString().equals("pom.xml"))
            .findFirst();

    return pom.map(Path::toString)
        .orElseThrow(() -> new IllegalStateException("POM file not found"));
  }

  private static Optional<Path> findFirstMatchingDir(Path root, String relativePath)
      throws IOException {
    String normalized = relativePath.replace("\\", "/");
    return Files.walk(root)
        .filter(Files::isDirectory)
        .filter(p -> p.toString().replace("\\", "/").endsWith(normalized))
        .findFirst();
  }
}
