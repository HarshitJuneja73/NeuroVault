package com.harshit.NeuroVault.controller;

import com.harshit.NeuroVault.model.User;
import com.harshit.NeuroVault.service.JwtService;
import com.harshit.NeuroVault.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

@RestController
@Tag(name = "User", description = "Operations related to user management")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;

    @PostMapping("register")
    @Operation(
            summary = "Register new user",
            description = "Registers a new user and returns the created user object."
    )
    public User register(@RequestBody User user){
        return userService.saveUser(user);
    }
    @PostMapping("login")
    @Operation(
            summary = "User login",
            description = "Authenticates user credentials and returns a JWT token if successful."
    )
    public String login(@RequestBody User user){
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        if(authentication.isAuthenticated()){
            return jwtService.generateToken(user.getUsername());
        } else {
            return "Login failed";
        }
    }

}
