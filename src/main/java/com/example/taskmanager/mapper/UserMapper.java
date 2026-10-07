package com.example.taskmanager.mapper;

import com.example.taskmanager.dto.UserCreateRequest;
import com.example.taskmanager.dto.UserResponse;
import com.example.taskmanager.model.Team;
import com.example.taskmanager.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toEntity(UserCreateRequest request, Team team) {
        User user = new User();
        user.setName(request.name());
        user.setDepartment(request.department());
        user.setTeam(team);
        return user;
    }
    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getDepartment()
        );
    }
}
