package com.example.taskmanager.service;

import com.example.taskmanager.dto.TeamCreateRequest;
import com.example.taskmanager.dto.TeamResponse;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.mapper.TeamMapper;
import com.example.taskmanager.model.Team;
import com.example.taskmanager.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;

    public TeamService(TeamRepository teamRepository, TeamMapper teamMapper) {
        this.teamRepository = teamRepository;
        this.teamMapper = teamMapper;
    }

    public List<TeamResponse> findAll() {
        return teamRepository.findAll()
                .stream()
                .map(teamMapper::toResponse)
                .toList();
    }

    public TeamResponse findById(UUID id) {
        return teamRepository.findById(id)
                .map(teamMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + id));
    }

    public TeamResponse save(TeamCreateRequest request) {
        Team team = teamMapper.toEntity(request);
        Team savedTeam = teamRepository.save(team);
        return teamMapper.toResponse(savedTeam);
    }

    public void deleteById(UUID id) {
        if (!teamRepository.existsById(id)) {
            throw new ResourceNotFoundException("Team not found: " + id);
        }
        teamRepository.deleteById(id);
    }
}
