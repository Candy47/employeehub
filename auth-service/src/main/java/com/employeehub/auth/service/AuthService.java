package com.employeehub.auth.service;

import com.employeehub.auth.dto.*;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    AuthResult login(LoginRequest request);
}