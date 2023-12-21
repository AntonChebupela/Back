package com.company.user;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    private static String getErrorMessage(Integer id) {
        return "User with id " + id + "not found";
    }

    public User getById(Integer id) {
        return  repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(getErrorMessage(id)));
    }

    public boolean isClient(User user) {
        return user.getRole() == Role.CLIENT;
    }
}
