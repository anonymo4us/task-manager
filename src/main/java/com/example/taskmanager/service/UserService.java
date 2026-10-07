package com.example.taskmanager.service;

import com.example.taskmanager.dto.UserCreateRequest;
import com.example.taskmanager.dto.UserResponse;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.mapper.UserMapper;
import com.example.taskmanager.model.Team;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TeamRepository;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, TeamRepository teamRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.userMapper = userMapper;
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public UserResponse findById(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    public UserResponse save(UserCreateRequest request) {
        Team team = resolveTeam(request.teamId());
        User user = userMapper.toEntity(request, team);
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    private Team resolveTeam(UUID teamId) {
        return teamId == null
            ? null
            : teamRepository.findById(teamId)
            .orElseThrow(() -> new ResourceNotFoundException("Team not found"));
    }

    public void deleteById(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }
}
