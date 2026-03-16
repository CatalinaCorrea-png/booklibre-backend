package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.dto.UserDTO
import ar.edu.unsam.phm.dto.toUserDTO
import ar.edu.unsam.phm.services.UserService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin("*")
class UserController(private val userService: UserService) {

    @GetMapping("/profile/{userId}")
    fun getUserProfile(@PathVariable userId: Int): UserDTO =
        userService.getUserProfile(userId)
}