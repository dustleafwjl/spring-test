package com.thoughtworks.rslist.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@Data
@NoArgsConstructor
public class User {
    @NotNull
    @Size(max = 8)
    private String userName;
    private String gender;
    @Min(18)
    @Max(100)
    private int age;
    @Email
    private String email;
    @NotNull
    @Pattern(regexp = "1\\d{10}")
    private String phone;
    @JsonIgnore
    private int voteNum =10;

    public User(String userName, String gender, int age, String email, String phone) {
        this.userName = userName;
        this.gender = gender;
        this.age = age;
        this.email = email;
        this.phone = phone;
    }
}
