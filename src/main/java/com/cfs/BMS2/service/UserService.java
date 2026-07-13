package com.cfs.BMS2.service;

import com.cfs.BMS2.dto.LoginRequest;
import com.cfs.BMS2.dto.UserRequest;
import com.cfs.BMS2.entity.User;
import com.cfs.BMS2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    //register

    public User register(UserRequest request)
    {
        if(userRepository.existsByEmail(request.getEmail()))
        {
            throw new RuntimeException("Email already exists: "+request.getEmail());
        }

        User user=User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .build();
        return userRepository.save(user);

    }

    //login

    public User login(LoginRequest request)
    {
        User user=userRepository.findByEmail(request.getEmail())
                .orElseThrow(()->new RuntimeException("User not found with email: "+request.getEmail()));
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword()))
        {
            throw new RuntimeException("Invalid password");
        }
        return user;
    }

    public List<User> getAllUser(){
        return userRepository.findAll();
    }

    public User getUserById(Long id)
    {
        return userRepository.findById(id)
                .orElseThrow(()->new RuntimeException("User not found with email: "+id));
    }

    public User getUserByEmail(String email)
    {
        return userRepository.findByEmail(email)
                .orElseThrow(()->new RuntimeException("User not found with email: "+email));
    }

}