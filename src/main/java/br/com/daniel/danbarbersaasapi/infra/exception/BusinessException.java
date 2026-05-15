package br.com.daniel.danbarbersaasapi.infra.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}

