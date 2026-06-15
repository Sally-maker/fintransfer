package com.gabriel.fintransfer.transaction.authorization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    public ClaudeAuthorizationService(AnthropicProperties properties, ObjectMapper objectMapper) {
        this.model = properties.model();
        this.objectMapper = objectMapper;
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
                - Deny if payer is MERCHANT
                - Deny if amount exceeds available balance
                - Deny suspiciously large transfers (>95%% of balance in a round number)
                - Otherwise approve standard transactions

                Response format (JSON only):
                {"approved": true, "reason": "brief explanation"}
                """.formatted(
                ctx.payerName(), ctx.payerType(),
                ctx.payeeName(), ctx.payeeType(),
                ctx.amount(), ctx.payerBalance()
        );

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

            String text = response.path("content").get(0).path("text").asText();
            JsonNode result = objectMapper.readTree(text.trim());
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
