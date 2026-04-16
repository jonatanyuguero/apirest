package com.jonatanyuguero.apirest.service;


import com.jonatanyuguero.apirest.dto.EditTaskCommand;
import com.jonatanyuguero.apirest.error.TaskNotFoundException;
import com.jonatanyuguero.apirest.model.Task;
import com.jonatanyuguero.apirest.repos.TaskRepository;
import com.jonatanyuguero.apirest.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public List<Task> findAll(){
        List<Task> result = taskRepository.findAll();

        if(result.isEmpty())
            throw new TaskNotFoundException();
        return result;
    }

    public List<Task> findByAuthor(User author){
        List<Task> result = taskRepository.findByAuthor(author);

        if(result.isEmpty())
            throw new TaskNotFoundException();
        return result;
    }

    public Task findbyId(Long id){
        return taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
    }

    public Task save(EditTaskCommand cmd, User author){
        return taskRepository.save(
                Task.builder()
                        .title(cmd.title())
                        .description(cmd.description())
                        .deadline(cmd.deadline())
                        .author(author)
                        .build()
        );
    }

    public Task edit(EditTaskCommand cmd, Long id){
        return taskRepository.findById(id)
                .map(t -> {
                    t.setTitle(cmd.title());
                    t.setDescription(cmd.description());
                    t.setDeadline(cmd.deadline());
                    return taskRepository.save(t);
                })
                .orElseThrow(()->new TaskNotFoundException(id));

    }

    public void delete(Long id){
        taskRepository.deleteById(id);
    }
}
