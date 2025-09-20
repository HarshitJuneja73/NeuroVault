package com.harshit.NeuroVault.service;

import com.harshit.NeuroVault.model.User;
import com.harshit.NeuroVault.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public User saveUser(User user){
        user.setPassword(encoder.encode(user.getPassword()));
        return repository.save(user);
    }
    public User getUserByUsername(String username){
        return repository.findByUsername(username);
    }
}
