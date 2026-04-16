package com.jonatanyuguero.apirest.repos;

import com.jonatanyuguero.apirest.model.Task;
import com.jonatanyuguero.apirest.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task,Long >{

    List<Task> findByAuthor(User author);

}
