package br.com.daniel.danbarbersaasapi.domain.user;

public enum UserRole {
    ADMIN("ADMIN"),
    BARBER("BARBER");

    private final String role;

    UserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}