package gradestyle;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;

public class Repo {
  public static List<Repo> getRepos(Github github) {
    if (github.getConfig().getGithub()) {
      try {
        return github.cloneAssignment();
      } catch (GitAPIException | IOException e) {
        e.printStackTrace();
        System.err.println("Unable to clone assignment from GitHub.");
      }
    }

    List<Repo> repos = new ArrayList<>();

    Iterable<Path> paths;

    if (github.getConfig().getRepos() == null) {
      paths = List.of(Paths.get("").toAbsolutePath());
    } else {
      try {
        paths =
            Files.newDirectoryStream(
                github.getConfig().getRepos(),
                new DirectoryStream.Filter<Path>() {
                  @Override
                  public boolean accept(Path path) {
                    return !path.equals(github.getConfig().getTemplateRepo());
                  }
                });
      } catch (IOException e) {
        System.err.println("Unable to read local repos.");

        return repos;
      }
    }

    for (Path path : paths) {
      if (!Files.isDirectory(path)) {
        continue;
      }

      String hash = null;

      try {
        hash =
            Git.open(path.toFile())
                .getRepository()
                .exactRef(Constants.HEAD)
                .getObjectId()
                .getName();
      } catch (IOException e) {
      }

      repos.add(
          new Repo(
              path, github.getConfig().getGithubClassroom(), path.getFileName().toString(), hash));
    }

    return repos;
  }

  enum BuildTool {
    MAVEN_WRAPPER,
    MAVEN,
    GRADLE_WRAPPER,
    GRADLE,
    NONE
  }

  private Path dir;

  private String org;

  private String name;

  private String commit;

  private BuildTool buildTool;

  public Repo(Path dir, String org, String name, String commit) {
    this.dir = dir;
    this.org = org;
    this.name = name;
    this.commit = commit;
    resolveBuildTool();
  }

  private void resolveBuildTool() {
    if (Files.exists(dir.resolve("mvnw"))) {
      buildTool = BuildTool.MAVEN_WRAPPER;
    } else if (Files.exists(dir.resolve("gradlew"))) {
      buildTool = BuildTool.GRADLE_WRAPPER;
    } else if (Files.exists(dir.resolve("pom.xml"))) {
      buildTool = BuildTool.MAVEN;
    } else if (Files.exists(dir.resolve("build.gradle"))) {
      buildTool = BuildTool.GRADLE;
    } else {
      buildTool = BuildTool.NONE;
    }
  }

  public BuildTool getBuildTool() {
    return buildTool;
  }

  public boolean generateBytecode() {
    switch (buildTool) {
      case MAVEN_WRAPPER:
        return runMavenWrapper("compile");
      case GRADLE_WRAPPER:
        return runGradleWrapper();
      case MAVEN:
        return runMaven();
      case GRADLE:
        return runGradle();
      default:
        return false;
    }
  }

  private boolean runGradle() {
    throw new UnsupportedOperationException("Unimplemented method 'runGradle'");
  }

  private boolean runMaven() {
    throw new UnsupportedOperationException("Unimplemented method 'runMaven'");
  }

  private boolean runGradleWrapper() {
    throw new UnsupportedOperationException("Unimplemented method 'runGradleWrapper'");
  }

  private boolean runMavenWrapper(String... args) {
    try {
      List<String> command = new ArrayList<>();

      boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
      if (isWindows) {
        command.add("cmd.exe");
        command.add("/c");
        command.add("mvnw.cmd");
      } else {
        command.add("./mvnw");
      }

      for (String arg : args) {
        command.add(arg);
      }

      ProcessBuilder pb = new ProcessBuilder(command);
      pb.directory(dir.toFile());
      pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
      pb.redirectError(ProcessBuilder.Redirect.DISCARD);

      Process p = pb.start();
      return p.waitFor() == 0;
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
    return false;
  }

  public Path getBytecodeDir() {
    switch (buildTool) {
      case MAVEN:
      case MAVEN_WRAPPER:
        return dir.resolve("target/classes");
      case GRADLE:
      case GRADLE_WRAPPER:
        return dir.resolve("build/classes/java/main");
      default:
        return dir.resolve("bin");
    }
  }

  public Path getDir() {
    return dir;
  }

  public String getOrg() {
    return org;
  }

  public String getName() {
    return name;
  }

  public String getCommit() {
    return commit;
  }

  public String getRepoUrl() {
    if (org == null || name == null) {
      return null;
    }

    return "https://github.com/" + org + "/" + name;
  }

  public String getCommitUrl() {
    if (getRepoUrl() == null) {
      return null;
    }

    return getRepoUrl() + "/commit/" + commit;
  }

  public String getFileUrl(Path file) {
    if (getRepoUrl() == null) {
      return null;
    }

    return getRepoUrl() + "/blob/" + commit + "/" + file;
  }

  public String getFileLineUrl(Path file, int line) {
    if (getFileUrl(file) == null) {
      return null;
    }

    return getFileUrl(file) + "#L" + line;
  }

  public String getFileRangeUrl(Path file, int begin, int end) {
    if (getFileLineUrl(file, begin) == null) {
      return null;
    }

    return getFileLineUrl(file, begin) + "-L" + end;
  }
}
