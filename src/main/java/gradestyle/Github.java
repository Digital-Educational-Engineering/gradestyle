package gradestyle;

import gradestyle.config.CategoryConfig;
import gradestyle.config.Config;
import gradestyle.validator.ValidationMarkdown;
import gradestyle.validator.ValidationResult;
import gradestyle.validator.Violation;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHubBuilder;

public class Github {
  private Config config;

  public Github(Config config) {
    this.config = config;
  }

  public Config getConfig() {
    return config;
  }

  public List<Repo> cloneAssignment() throws IOException, GitAPIException {
    GHOrganization github = getGithub();

    List<Repo> repos = new ArrayList<>();

    for (GHRepository ghRepo : github.listRepositories()) {
      if (!ghRepo.getName().startsWith(config.getGithubAssignment())) {
        continue;
      }

      Path clonePath = config.getRepos().resolve(ghRepo.getName());
      File cloneDir = clonePath.toFile();

      String hash = ghRepo.listCommits().toArray()[0].getSHA1();
      Repo repo = new Repo(clonePath, config.getGithubClassroom(), ghRepo.getName(), hash);

      if (!ghRepo.isTemplate()) {
        repos.add(repo);
      }

      getRepo(repo.getRepoUrl(), cloneDir);

      Git.open(cloneDir).checkout().setName(hash).call();
    }

    return repos;
  }

  private GHOrganization getGithub() throws IOException {
    GitHubBuilder builder = new GitHubBuilder();

    if (config.getGithubToken() != null) {
      builder = builder.withOAuthToken(config.getGithubToken());
    }

    return builder.build().getOrganization(config.getGithubClassroom());
  }

  private void getRepo(String url, File dir) throws IOException, GitAPIException {
    CredentialsProvider creds =
        new UsernamePasswordCredentialsProvider(config.getGithubToken(), "");

    if (dir.exists()) {
      try {
        Git.open(dir).pull().setCredentialsProvider(creds).call();
        return;
      } catch (GitAPIException e) {
        FileUtils.deleteDirectory(dir);
      }
    }

    Git.cloneRepository().setURI(url).setDirectory(dir).setCredentialsProvider(creds).call();
  }

  public void sendFeedback(List<ValidationResult> results) throws IOException {
    GHOrganization github = getGithub();

    for (ValidationResult result : results) {
      if (result.getViolations().getViolations().isEmpty()) {
        continue;
      }

      StringBuilder sb = new StringBuilder();

      Markdown.message(sb, config.getStyleFeedback().getFeedbackMessage());

      if (result.getError() == null) {
        ValidationMarkdown.scoreTable(sb, result, config.getCategoryConfigs());
        feedback(sb, result, config.getCategoryConfigs());
      } else {
        Markdown.message(sb, config.getStyleFeedback().getFeedbackError());
      }

      Markdown.footer(sb, result.getRepo());

      github
          .getRepository(result.getRepo().getName())
          .createIssue(config.getStyleFeedback().getFeedbackTitle())
          .body(sb.toString())
          .create();
    }
  }

  private void feedback(StringBuilder sb, ValidationResult result, List<CategoryConfig> configs) {
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

        String url = repo.getFileLineUrl(relative, example.getLine());

        if (example.getEndLine() != -1) {
          url = repo.getFileRangeUrl(relative, example.getLine(), example.getEndLine());
        }

        sb.append(example.getType().getMessage()).append("\n").append(url).append("\n\n");
      }
    }
  }
}
