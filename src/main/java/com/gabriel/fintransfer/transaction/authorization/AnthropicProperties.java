package com.gabriel.fintransfer.transaction.authorization;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "anthropic")
public record AnthropicProperties(String apiKey, String model, String baseUrl) {}
