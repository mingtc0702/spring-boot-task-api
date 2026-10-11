package com.mt.taskapi.service;

import com.mt.taskapi.dto.UserRequest;
import com.mt.taskapi.dto.UserResponse;
import com.mt.taskapi.exception.EmailAlreadyExistsException;
import com.mt.taskapi.exception.UserNotFoundException;
import com.mt.taskapi.model.User;
import com.mt.taskapi.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Transactional
    public UserResponse createUser(UserRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = new User(
                request.name(),
                request.email()
        );

        User savedUser = userRepository.save(user);

        return toUserResponse(savedUser);
    }


    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(id));

        return toUserResponse(user);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}
