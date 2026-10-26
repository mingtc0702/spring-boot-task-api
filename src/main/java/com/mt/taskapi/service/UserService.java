package com.mt.taskapi.service;

import com.mt.taskapi.dto.UserRequest;
import com.mt.taskapi.dto.UserResponse;
import com.mt.taskapi.exception.EmailAlreadyExistsException;
import com.mt.taskapi.exception.UserNotFoundException;
import com.mt.taskapi.model.User;
import com.mt.taskapi.repository.UserRepository;
import com.mt.taskapi.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            CurrentUserService currentUserService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
    }


    @Transactional
    public UserResponse createUser(UserRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = new User(
                request.name(),
                request.email(),
                passwordEncoder.encode(request.password())
        );

        User savedUser = userRepository.save(user);

        return toUserResponse(savedUser);
    }


    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {

        User currentUser = currentUserService.getCurrentUser();

        if (!currentUser.getId().equals(id)) {
            throw new UserNotFoundException(id);
        }

        return toUserResponse(currentUser);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}
