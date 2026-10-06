CREATE TABLE teams (
    id UUID PRIMARY KEY,
    team_name VARCHAR(255) NOT NULL,
    specialization VARCHAR(255) NOT NULL
    );

CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    department VARCHAR(255) NOT NULL,
    team_id UUID REFERENCES teams(id)
    );

CREATE TABLE projects (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    team_id UUID NOT NULL REFERENCES teams(id)
);

CREATE TABLE tasks (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    project_id UUID REFERENCES projects(id)
);

CREATE TABLE task_assignees (
    task_id UUID NOT NULL REFERENCES tasks(id),
    user_id UUID NOT NULL REFERENCES users(id),
    PRIMARY KEY (task_id, user_id)
);