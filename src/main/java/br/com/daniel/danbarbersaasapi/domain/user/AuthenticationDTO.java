package br.com.daniel.danbarbersaasapi.domain.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthenticationDTO(
		@NotBlank(message = "O e-mail é obrigatório")
		@Email(message = "O e-mail informado é inválido")
		String email,

		@NotBlank(message = "A senha é obrigatória")
		String password
) {
}