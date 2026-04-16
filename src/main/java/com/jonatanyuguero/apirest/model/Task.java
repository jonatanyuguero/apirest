package com.jonatanyuguero.apirest.model;

import com.jonatanyuguero.apirest.users.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Task {

    @Id @GeneratedValue
    private long id;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();


    private String title;

    @Lob
    private String description;

    private LocalDateTime deadline;

    @ManyToOne
    private User author;
}
