package gradestyle.config;

import java.nio.file.Path;

public class FeedbackReportConfig {
  private Path reportsCsv;

  private Path reportsMd;

  private String feedbackTitle;

  private String feedbackMessage;

  private String feedbackError;

  public FeedbackReportConfig(
      Path reportsCsv,
      Path reportsMd,
      String feedbackTitle,
      String feedbackMessage,
      String feedbackError) {
    this.reportsCsv = reportsCsv;
    this.reportsMd = reportsMd;
    this.feedbackTitle = feedbackTitle;
    this.feedbackMessage = feedbackMessage;
    this.feedbackError = feedbackError;
  }

  public Path getReportsCsv() {
    return reportsCsv;
  }

  public Path getReportsMd() {
    return reportsMd;
  }

  public String getFeedbackTitle() {
    return feedbackTitle;
  }

  public String getFeedbackMessage() {
    return feedbackMessage;
  }

  public String getFeedbackError() {
    return feedbackError;
  }
}
