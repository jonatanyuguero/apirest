package com.jonatanyuguero.apirest.repos;

import com.jonatanyuguero.apirest.model.Tag;
import com.jonatanyuguero.apirest.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByNameIgnoreCase(String name);

    List<Tag> findByAuthor(User author);

    Optional<Tag> findByIdAndAuthor(Long id, User author);
}
