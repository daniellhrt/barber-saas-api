package br.com.daniel.danbarbersaasapi.infra.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;


@RestControllerAdvice
public class ErrorHandler {

    // 1. Tratamento para ERRO 404 (Não Encontrado)
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> tratarErro404() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> tratarResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErroMessage(ex.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> tratarResponseStatus(ResponseStatusException ex) {
        var status = ex.getStatusCode();
        var message = ex.getReason() != null ? ex.getReason() : "Erro na requisição.";
        return ResponseEntity.status(status).body(new ErroMessage(message));
    }

    // 2. Tratamento para ERRO 400 (Bad Request - Validação de Campos como @NotBlank)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> tratarErro400(MethodArgumentNotValidException ex) {
        var erros = ex.getFieldErrors();
        return ResponseEntity.badRequest().body(erros.stream().map(DadosErroValidacao::new).toList());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> tratarBusinessException(BusinessException ex) {
        return ResponseEntity.badRequest().body(new ErroMessage(ex.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<?> tratarConflictException(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErroMessage(ex.getMessage()));
    }

    // 3. Tratamento para Senha Incorreta
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> tratarErroBadCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErroMessage("Credenciais inválidas."));
    }

    // 4. Tratamento para Token Ausente ou Inválido
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> tratarErroAuthentication() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErroMessage("Falha na autenticação. Faça login novamente."));
    }

    // 5. Tratamento para Acesso Negado (Tentou acessar uma rota de ADMIN sendo BARBER)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> tratarErroAcessoNegado() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErroMessage("Acesso negado. Você não tem permissão."));
    }

    // 6. Tratamento para qualquer erro genérico / interno do servidor (Erro 500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> tratarErro500(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErroMessage("Erro interno: " + ex.getLocalizedMessage()));
    }

    // --- DTOs auxiliares internos para formatar a resposta JSON ---

    public record DadosErroValidacao(String campo, String mensagem) {
        public DadosErroValidacao(FieldError erro) {
            this(erro.getField(), erro.getDefaultMessage());
        }
    }

    public record ErroMessage(String error) {
    }
}