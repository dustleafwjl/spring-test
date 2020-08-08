package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.User;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@Service
public class UserService {
    final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public void addUser(@RequestBody @Valid User user) {
        UserDto userDto = new UserDto();
        userDto.setGender(user.getGender());
        userDto.setPhone(user.getPhone());
        userDto.setVoteNum(user.getVoteNum());
        userDto.setAge(user.getAge());
        userDto.setEmail(user.getEmail());
        userDto.setUserName(user.getUserName());
        userRepository.save(userDto);
    }

    public void deleteUser(int id) {
        userRepository.deleteById(id);
    }
}
