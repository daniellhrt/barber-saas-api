package br.com.daniel.danbarbersaasapi.controllers;

import br.com.daniel.danbarbersaasapi.domain.user.AuthenticationDTO;
import br.com.daniel.danbarbersaasapi.domain.user.RegisterDTO;
import br.com.daniel.danbarbersaasapi.domain.user.User;
import br.com.daniel.danbarbersaasapi.domain.user.UserRole;
import br.com.daniel.danbarbersaasapi.infra.exception.ErrorHandler;
import br.com.daniel.danbarbersaasapi.repository.UserRepository;
import br.com.daniel.danbarbersaasapi.security.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new ErrorHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("Deve autenticar usuário e retornar token")
    void loginShouldReturnToken() throws Exception {
        String email = "admin@barber.com";
        String password = "password123";
        User principal = new User(UUID.randomUUID(), email, "hashed", UserRole.ADMIN, null);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(tokenService.generateToken(principal)).thenReturn("jwt-token");

        AuthenticationDTO request = new AuthenticationDTO(email, password);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    @DisplayName("Deve registrar usuário novo com senha criptografada")
    void registerShouldCreateUser() throws Exception {
        String email = "novo@barber.com";
        String password = "password123";

        when(userRepository.findByEmail(email)).thenReturn(null);
        when(passwordEncoder.encode(password)).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterDTO request = new RegisterDTO(email, password, UserRole.BARBER);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Deve retornar conflito quando o e-mail já existir")
    void registerShouldReturnConflictWhenEmailExists() throws Exception {
        String email = "admin@barber.com";
        when(userRepository.findByEmail(email)).thenReturn(new User(UUID.randomUUID(), email, "hashed", UserRole.ADMIN, null));

        RegisterDTO request = new RegisterDTO(email, "password123", UserRole.ADMIN);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}


