package com.scriptorium.scriptorium.domain;

import jakarta.annotation.Generated;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="curators")
public class Curator {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Getter
    @Setter
    @Column(unique = true,nullable = false)
    private String email;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String institution;
    @Getter
    @Setter
    private String role;
    @Getter
    @Setter
    @Column(name ="hash_password")
    private String hashedPassword;
    @Getter
    @Setter
    @Column(name ="created_at")
    private LocalDateTime createdAt;


}
