package org.miage.bankservice.boundary;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class SpringController {

    @GetMapping
    public ResponseEntity<User> getUser(){

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return  new ResponseEntity<User>(user, HttpStatus.OK);
    }
}

