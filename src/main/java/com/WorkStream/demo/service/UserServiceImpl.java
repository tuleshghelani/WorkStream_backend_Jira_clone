package com.WorkStream.demo.service;

import com.WorkStream.demo.DTO.UserDTO;
import com.WorkStream.demo.config.JwtService;
import com.WorkStream.demo.entity.User;
import com.WorkStream.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /*public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }*/

    @Override
    @Transactional
    public ResponseEntity<?> register(UserDTO userDTO) {
        Optional<User> tempUser = userRepository.findByEmail(userDTO.getEmail());
        if(tempUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("status", false, "message", "Email already exist"));
//            return ResponseEntity.ok(new JSONObject().put("status", false).put("message", "Email already exist"));
        }
        User user = new User();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        user.setJwt_token(jwtToken);
        userRepository.save(user);
//        saveUserToken(savedUser, jwtToken);
        return ResponseEntity.ok(UserDTO.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build());
    }

    @Override
    public ResponseEntity<?> login(UserDTO userDTO) {
        try {
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userDTO.getEmail(),
                            userDTO.getPassword()
                    )
            );
            var user = userRepository.findByEmail(userDTO.getEmail())
                    .orElseThrow();
            var jwtToken = jwtService.generateToken(user);
            user.setJwt_token(jwtToken);
            userRepository.save(user);
            var refreshToken = jwtService.generateRefreshToken(user);
//        revokeAllUserTokens(user);
//        saveUserToken(user, jwtToken);
            return ResponseEntity.ok(UserDTO.builder()
                    .accessToken(jwtToken)
//                    .refreshToken(refreshToken)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject()
                            .put("status", false)
                            .put("message", "Invalid username or password")
                            .toString());
//                    .body(Map.of("status", false, "message", "Invalid username or password"));
        }
    }




}
