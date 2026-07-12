package com.cfs.BMS2.controller;

import com.cfs.BMS2.dto.LoginRequest;
import com.cfs.BMS2.dto.UserRequest;
import com.cfs.BMS2.entity.User;
import com.cfs.BMS2.repository.UserRepository;
import com.cfs.BMS2.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {


    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> createToken(@RequestBody LoginRequest request){

        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails.getUsername());
            // userDetails.getUsername() is the email (see CustomUserDetailsService)

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalStateException("Authenticated user not found: "
                                    + userDetails.getUsername()));

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("type", "Bearer");
            response.put("id", user.getId());
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("roles", userDetails.getAuthorities()
                    .stream().map(a -> a.getAuthority()).toList()
            );
            response.put("expireTime", jwtService.getJwtExpirationTime());

            System.out.println("User logged in: " + user.getEmail());

            return ResponseEntity.ok(response);
        }
        catch (Exception e){
            Map<String, Object> response = new HashMap<>();
            response.put("error", "invalid credentials");
            response.put("message", e.getMessage());

            return ResponseEntity.status(401).body(response);
        }

    }
}
