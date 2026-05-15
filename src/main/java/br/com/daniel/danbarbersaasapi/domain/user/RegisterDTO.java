package br.com.daniel.danbarbersaasapi.domain.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterDTO(
		@NotBlank(message = "O e-mail é obrigatório")
		@Email(message = "O e-mail informado é inválido")
		String email,

		@NotBlank(message = "A senha é obrigatória")
		@Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
		String password,

		@NotNull(message = "O perfil do usuário é obrigatório")
		UserRole role
) {
}