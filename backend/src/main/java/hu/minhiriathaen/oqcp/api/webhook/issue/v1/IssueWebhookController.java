package hu.minhiriathaen.oqcp.api.webhook.issue.v1;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.ContextJwt;
import hu.minhiriathaen.oqcp.jira.transfer.webhook.issue.update.IssueUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class IssueWebhookController {

  public static final String ISSUE_UPDATED_WEBHOOK_URL = "/webhooks/v1/issues/updated";

  private final AsyncIssueWebhookService asyncIssueWebhookService;

  @ContextJwt
  @PostMapping(ISSUE_UPDATED_WEBHOOK_URL)
  public void handleIssueUpdated(
      @AuthenticationPrincipal final AtlassianHostUser atlassianHostUser,
      @RequestBody final IssueUpdatedEvent event) {
    log.info("Issue updated event received: {}", event);

    asyncIssueWebhookService.handleIssueUpdated(atlassianHostUser, event);
  }
}
