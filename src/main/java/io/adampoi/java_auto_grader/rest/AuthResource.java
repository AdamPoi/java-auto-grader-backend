package io.adampoi.java_auto_grader.rest;

import io.adampoi.java_auto_grader.domain.RefreshToken;
import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.model.dto.RefreshTokenRequestDTO;
import io.adampoi.java_auto_grader.model.dto.RegisterRequestDTO;
import io.adampoi.java_auto_grader.model.dto.UserDTO;
import io.adampoi.java_auto_grader.model.response.ApiSuccessResponse;
import io.adampoi.java_auto_grader.model.response.LoginRequestDTO;
import io.adampoi.java_auto_grader.model.response.LoginResponseDTO;
import io.adampoi.java_auto_grader.model.response.TokenResponseDTO;
import io.adampoi.java_auto_grader.service.AuthService;
import io.adampoi.java_auto_grader.service.JwtService;
import io.adampoi.java_auto_grader.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping(value = "/api/auth",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
public class AuthResource {

    private final AuthService authService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthResource(final AuthService authService, JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    @ApiResponse(responseCode = "200")
    public ApiSuccessResponse<LoginResponseDTO> login(@RequestBody @Valid final LoginRequestDTO loginRequestDTO) {
        User authenticatedUser = authService.login(loginRequestDTO);

        String accessToken = jwtService.generateToken(authenticatedUser);

        RefreshToken refreshToken = refreshTokenService.create(authenticatedUser);

        LoginResponseDTO loginResponse = LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .expireIn(jwtService.getExpirationTime())
                .build();

        return ApiSuccessResponse.<LoginResponseDTO>builder()
                .data(loginResponse)
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PostMapping("/register")
    @ApiResponse(responseCode = "201")
    public ApiSuccessResponse<UUID> register(@RequestBody @Valid final RegisterRequestDTO registerRequest) {
        return ApiSuccessResponse.<UUID>builder()
                .data(authService.register(registerRequest))
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    @GetMapping("/me")
    @ApiResponse(responseCode = "200")
    public ApiSuccessResponse<UserDTO> getCurrentUser(Authentication authentication) {
        return ApiSuccessResponse.<UserDTO>builder()
                .data(authService.getCurrentUser(authentication))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PostMapping("/refresh")
    @ApiResponse(responseCode = "200")
    public ApiSuccessResponse<TokenResponseDTO> refreshToken(@RequestBody @Valid RefreshTokenRequestDTO refreshTokenRequestDTO) {
        String accessToken = refreshTokenService.refresh(refreshTokenRequestDTO.getRefreshToken());
        TokenResponseDTO tokenResponse = TokenResponseDTO.builder().
                accessToken(accessToken).build();
        return ApiSuccessResponse.<TokenResponseDTO>builder()
                .data(tokenResponse)
                .statusCode(HttpStatus.OK)
                .build();


    }

    @PostMapping("/logout")
    @ApiResponse(responseCode = "204")
    public ApiSuccessResponse<Void> logout(@RequestBody @Valid RefreshTokenRequestDTO refreshTokenRequestDTO) {
        refreshTokenService.delete(refreshTokenRequestDTO.getRefreshToken());
        return ApiSuccessResponse.<Void>builder()
                .statusCode(HttpStatus.NO_CONTENT)
                .build();
    }
}