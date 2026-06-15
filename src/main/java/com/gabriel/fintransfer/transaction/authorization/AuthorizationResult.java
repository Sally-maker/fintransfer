package com.gabriel.fintransfer.transaction.authorization;

public record AuthorizationResult(boolean approved, String reason) {

    public static AuthorizationResult approved(String reason) {
        return new AuthorizationResult(true, reason);
    }

    public static AuthorizationResult denied(String reason) {
        return new AuthorizationResult(false, reason);
    }
}
