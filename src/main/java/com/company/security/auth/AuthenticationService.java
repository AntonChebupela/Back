package com.company.security.auth;


import com.company.token.Token;
import com.company.token.TokenRepository;
import com.company.token.TokenService;
import com.company.user.Role;
import com.company.user.User;
import com.company.user.UserDto;
import com.company.user.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public ResponseEntity<AuthenticationResponse> register(RegisterRequest request, HttpServletResponse httpServletResponse) {
        User user = setUser(request);
        authenticate(request);
        String refreshToken = tokenService.generateAccessToken(user);
        String accessToken = tokenService.generateRefreshToken(user);
        setTokenToRepository(refreshToken, user);
        setTokenToCookie(httpServletResponse, refreshToken);
        return ResponseEntity.ok(buildResponse(refreshToken, accessToken, user));
    }

    public ResponseEntity<AuthenticationResponse> authenticate(AuthenticationRequest request, HttpServletResponse httpServletResponse) {
        User user = getUser(request.getEmail());
        authenticate(request);
        String refreshToken = tokenService.generateAccessToken(user);
        String accessToken = tokenService.generateRefreshToken(user);
        setTokenToRepository(refreshToken, user);
        setTokenToCookie(httpServletResponse, refreshToken);
        return ResponseEntity.ok(buildResponse(refreshToken, accessToken, user));
    }

    public ResponseEntity<AuthenticationResponse> refresh(String requestToken, HttpServletResponse httpServletResponse) {
        checkToken(requestToken);
        String email = tokenService.getUsername(requestToken);
        User user = getUser(email);
        String refreshToken = tokenService.generateAccessToken(user);
        String accessToken = tokenService.generateRefreshToken(user);
        setTokenToRepository(refreshToken, user);
        setTokenToCookie(httpServletResponse, refreshToken);
        return ResponseEntity.ok(buildResponse(refreshToken, accessToken, user));
    }

    public ResponseEntity<Void> logout(String refreshToken) {
        try {
            tokenRepository.delete(tokenRepository.getByRefreshToken(refreshToken)
                    .orElseThrow(() -> new EntityNotFoundException("Token not found")));
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            throw new RuntimeException("Token deleting error");
        }
    }

    private void checkToken(String token) {
        Token dbToken = tokenRepository.getByRefreshToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Token not found"));
        if (!dbToken.getRefreshToken().equals(token)) {
            throw new EntityExistsException("Tokens are not equal");
        }
    }

    private User setUser(RegisterRequest request) {
        Optional<User> optUser = userRepository.getByEmail(request.getEmail());
        if (optUser.isPresent()) {
            throw new EntityExistsException("User with email: " + request.getEmail() + " already exists");
        }
        var user = User.builder()
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .role(Role.CLIENT)
                .build();
        return userRepository.save(user);
    }

    private User getUser(String email) {
        return userRepository.getByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User with email " + email + "not found"));
    }

    private void setTokenToCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refreshToken", token);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    private AuthenticationResponse buildResponse(String refreshToken, String accessToken, User user) {
        return AuthenticationResponse.builder()
                .refreshToken(refreshToken)
                .accessToken(accessToken)
                .user(new UserDto(user.getId(), user.getEmail(), user.getPassword(), user.getRole().toString()))
                .build();
    }

    private void authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
    }

    private void authenticate(RegisterRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
    }

    private void setTokenToRepository(String refreshToken, User user) {
        Optional<Token> optToken = tokenRepository.getByUser(user);
        if (optToken.isPresent()) {
            Token token = optToken.get();
            token.setRefreshToken(refreshToken);
            tokenRepository.save(token);
        } else {
            tokenRepository.save(new Token(user, refreshToken));
        }
    }
}