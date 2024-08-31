package com.WorkStream.demo.service;

import com.WorkStream.demo.DTO.UserDTO;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<?> register(UserDTO userDTO);

    ResponseEntity<?> login(UserDTO userDTO);
}
