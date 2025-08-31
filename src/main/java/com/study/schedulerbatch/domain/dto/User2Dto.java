package com.study.schedulerbatch.domain.dto;

import com.study.schedulerbatch.domain.entity.User2;
import lombok.*;

@ToString
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User2Dto {
    private Long id;

    private int age;

    private String username;

    private String email;

    private String password;

    private String memo;

    public User2Dto getUserDtoFromUser(User2 user) {
        // @NoArgsConstructor 이거 덕분에 new 안 붙이고 UserDto 바로 리턴 가능
        return User2Dto.builder()
                .id(user.getId())
                .age(user.getAge())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .memo(user.getMemo())
                .build();
    }
}
