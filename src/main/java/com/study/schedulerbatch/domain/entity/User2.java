package com.study.schedulerbatch.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Table(name = "user2")
public class User2 {
    @Id
    private Long id;

    private int age;

    private String username;

    private String email;

    private String password;

    private String memo;
}
