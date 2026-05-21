package com.jonatanyuguero.apirest.service;

import com.jonatanyuguero.apirest.dto.TagCommand;
import com.jonatanyuguero.apirest.error.TagNotFoundException;
import com.jonatanyuguero.apirest.model.Tag;
import com.jonatanyuguero.apirest.repos.TagRepository;
import com.jonatanyuguero.apirest.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<Tag> findAll(User author) {
        return tagRepository.findByAuthor(author);
    }

    public Tag findById(Long id, User author) {
        return tagRepository.findByIdAndAuthor(id, author)
                .orElseThrow(() -> new TagNotFoundException(id));
    }

    public Tag save(TagCommand cmd, User author) {
        return tagRepository.save(
                Tag.builder()
                        .name(cmd.name())
                        .author(author)
                        .build()
        );
    }

    public Tag edit(Long id, TagCommand cmd, User author) {
        return tagRepository.findByIdAndAuthor(id, author)
                .map(t -> {
                    t.setName(cmd.name());
                    return tagRepository.save(t);
                })
                .orElseThrow(() -> new TagNotFoundException(id));
    }

    public void delete(Long id, User author) {
        Tag tag = tagRepository.findByIdAndAuthor(id, author)
                .orElseThrow(() -> new TagNotFoundException(id));
        tagRepository.delete(tag);
    }
}
