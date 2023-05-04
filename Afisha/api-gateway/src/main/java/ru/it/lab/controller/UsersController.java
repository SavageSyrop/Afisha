package ru.it.lab.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UsersController {

    @GetMapping("/all")     // TODO переправка запроса в микросервис
    public String getAllUsers() {
        return "sus";
    }

//    @GetMapping("/myprofile")
//    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
//    public ResponseEntity<UserDTO> getMyProfile() {
//        return ResponseEntity.ok();
//    }
//
//    @GetMapping("/{userId}/profile}")
//    public ResponseEntity<UserDTO> getUserInfo(@PathVariable Long userId) {
//        return ResponseEntity.ok();
//    }
}
