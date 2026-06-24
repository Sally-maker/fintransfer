package com.gabriel.fintransfer.transaction.authorization;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import com.gabriel.fintransfer.shared.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ClaudeAuthorizationService implements TransactionAuthorizationService {

    private final RestClient restClient;
    private final String model;
    private final JsonMapper jsonMapper;

    public ClaudeAuthorizationService(AnthropicProperties properties, JsonMapper jsonMapper) {
        this.model = properties.model();
        this.jsonMapper = jsonMapper;
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("x-api-key", properties.apiKey())
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("content-type", "application/json")
                .build();
    }

    @Override
    public AuthorizationResult authorize(TransactionContext ctx) {
        String prompt = """
                You are a financial transaction authorization system for FinTransfer.
                Analyze the transaction below and respond with JSON ONLY — no explanation, no markdown.

                Transaction:
                - Payer: %s (type: %s)
                - Payee: %s (type: %s)
                - Amount: R$ %.2f
                - Payer available balance: R$ %.2f

                Rules:
                - Deny if payer is MERCHANT (merchants can only receive transfers)
                - Deny if amount exceeds available balance
                - Deny if amount is above R$ 5000.00 (high value transfer limit)
                - Deny if amount is below R$ 1.00 (suspicious micro-transaction)
                - Deny if payer and payee are the same person
                - Approve all other transactions

                Response format (JSON only):
                {"approved": true, "reason": "brief explanation"}
                """.formatted(
                ctx.payerName(), ctx.payerType(),
                ctx.payeeName(), ctx.payeeType(),
                ctx.amount(), ctx.payerBalance()
        );

        return callClaude(prompt);
    }

    @Override
    public AuthorizationResult authorizeRefund(RefundContext ctx) {
        String prompt = """
                You are a financial refund authorization system for FinTransfer.
                Analyze the refund request below and respond with JSON ONLY — no explanation, no markdown.

                Refund request:
                - Requester: %s
                - Recipient of original transfer: %s
                - Amount: R$ %.2f
                - Reason provided: %s
                - Time since transaction: %d minutes

                Rules:
                - Approve if reason is valid (wrong recipient, duplicate transfer, incorrect amount, service not received)
                - Deny if reason is vague or empty (e.g. "test", "no reason")
                - Deny if transaction was made more than 7 days ago (10080 minutes)
                - Approve all other valid refund requests

                Response format (JSON only):
                {"approved": true, "reason": "brief explanation"}
                """.formatted(
                ctx.requesterName(),
                ctx.recipientName(),
                ctx.amount(),
                ctx.reason(),
                ctx.minutesSinceTransaction()
        );

        return callClaude(prompt);
    }

    private AuthorizationResult callClaude(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", 150,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        try {
            JsonNode response = restClient.post()
                    .uri("/v1/messages")
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);

            String text = response.path("content").get(0).path("text").asText().trim();
            text = text.replaceAll("(?s)```(?:json)?\\s*(.*?)\\s*```", "$1").trim();
            JsonNode result = jsonMapper.readTree(text);
            boolean approved = result.path("approved").asBoolean();
            String reason = result.path("reason").asText("No reason provided");
            return new AuthorizationResult(approved, reason);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Claude authorization request failed", e);
            throw new BusinessException("Authorization service unavailable", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
