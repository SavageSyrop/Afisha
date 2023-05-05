package ru.it.lab.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.it.lab.dto.LoginDTO;
import ru.it.lab.dto.UserDTO;
import ru.it.lab.service.AuthorizationService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/")
public class UsersController {

    @Autowired
    private AuthorizationService authorizationService;


    @GetMapping("login")
    public void login(@RequestBody LoginDTO loginDto, HttpServletResponse httpResponse) throws IOException {
        authorizationService.login(loginDto.getUsername(), loginDto.getPassword());
        httpResponse.sendRedirect("user/myprofile");
    }

    @GetMapping("user/myprofile")
    @PreAuthorize("hasAuthority('AUTHORIZED_ACTIONS')")
    public String getMyProfile() {
        return "sas";
    }
//
//    @GetMapping("/{userId}/profile}")
//    public ResponseEntity<UserDTO> getUserInfo(@PathVariable Long userId) {
//        return ResponseEntity.ok();
//    }
}
