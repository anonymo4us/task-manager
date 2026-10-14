package com.example.taskmanager.service;

import com.example.taskmanager.dto.TeamCreateRequest;
import com.example.taskmanager.dto.TeamResponse;
import com.example.taskmanager.exception.ResourceConflictException;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.mapper.TeamMapper;
import com.example.taskmanager.model.Team;
import com.example.taskmanager.repository.TeamRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public List<TeamResponse> findAll() {
        return teamRepository.findAll()
                .stream()
                .map(teamMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TeamResponse findById(UUID id) {
        return teamRepository.findById(id)
                .map(teamMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + id));
    }

    @Transactional
    public TeamResponse save(TeamCreateRequest request) {
        Team team = teamMapper.toEntity(request);
        Team savedTeam = teamRepository.save(team);
        return teamMapper.toResponse(savedTeam);
    }

    @Transactional
    public void deleteById(UUID id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + id));

        try {
            teamRepository.delete(team);
            teamRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ResourceConflictException(
                    "Team " + id + " cannot be deleted because it is referenced by other resources");
        }
    }
}
