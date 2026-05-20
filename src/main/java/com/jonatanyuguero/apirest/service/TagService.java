package com.jonatanyuguero.apirest.service;

import com.jonatanyuguero.apirest.dto.TagCommand;
import com.jonatanyuguero.apirest.error.TagNotFoundException;
import com.jonatanyuguero.apirest.model.Tag;
import com.jonatanyuguero.apirest.repos.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<Tag> findAll() {
        return tagRepository.findAll();
    }

    public Tag findById(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException(id));
    }

    public Tag save(TagCommand cmd) {
        return tagRepository.save(
                Tag.builder().name(cmd.name()).build()
        );
    }

    public Tag edit(Long id, TagCommand cmd) {
        return tagRepository.findById(id)
                .map(t -> {
                    t.setName(cmd.name());
                    return tagRepository.save(t);
                })
                .orElseThrow(() -> new TagNotFoundException(id));
    }

    public void delete(Long id) {
        if (!tagRepository.existsById(id))
            throw new TagNotFoundException(id);
        tagRepository.deleteById(id);
    }
}
