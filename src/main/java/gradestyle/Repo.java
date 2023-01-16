package gradestyle;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
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

    DirectoryStream<Path> paths;
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
      e.printStackTrace();
      System.err.println("Unable to read local repos.");
      return repos;
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

  private Path dir;

  private String org;

  private String name;

  private String commit;

  public Repo(Path dir, String org, String name, String commit) {
    this.dir = dir;
    this.org = org;
    this.name = name;
    this.commit = commit;
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

    String url = getFileLineUrl(file, begin);

    if (begin != end) {
      url += "-L" + end;
    }

    return url;
  }
}
