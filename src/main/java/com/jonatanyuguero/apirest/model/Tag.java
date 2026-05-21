package com.jonatanyuguero.apirest.model;

import com.jonatanyuguero.apirest.users.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString(exclude = "author")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tag")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;
}
