package br.com.daniel.danbarbersaasapi.domain.user;

public record RegisterDTO(String email, String password, UserRole role) {
}