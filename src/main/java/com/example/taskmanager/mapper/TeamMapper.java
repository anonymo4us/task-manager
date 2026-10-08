package com.example.taskmanager.mapper;

import com.example.taskmanager.dto.TeamCreateRequest;
import com.example.taskmanager.dto.TeamResponse;
import com.example.taskmanager.model.Team;
import org.springframework.stereotype.Component;

@Component
public class TeamMapper {
    public Team toEntity(TeamCreateRequest request){
        Team team = new Team();
        team.setTeamName(request.teamName());
        team.setSpecialization(request.specialization());
        return team;
    }
     public TeamResponse toResponse(Team team){
        return new TeamResponse(
                team.getId(),
                team.getTeamName(),
                team.getSpecialization()
        );
     }
}
