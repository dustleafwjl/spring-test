package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.domain.User;
import com.thoughtworks.rslist.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class UserController {
  @Autowired
  UserService userService;

  @PostMapping("/user")
  public void register(@RequestBody @Valid User user) {
    userService.addUser(user);
  }



  @DeleteMapping("/user/{id}")
  public ResponseEntity deleteUser(@PathVariable int id) {
    userService.deleteUser(id);
    return ResponseEntity.ok().build();
  }
}
