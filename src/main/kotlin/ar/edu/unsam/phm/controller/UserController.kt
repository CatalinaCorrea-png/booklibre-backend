package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.services.UserService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.AuthRegisterRequest
import ar.edu.unsam.phm.dto.AuthRequest
import ar.edu.unsam.phm.dto.AuthResponse

@RestController
@CrossOrigin("*")

class UserController(val userService: UserService) {
    @PostMapping("/login")
    fun getUser(@RequestBody request: AuthRequest) : AuthResponse {
        val user = User(email = request.email, password = request.password )
        val userOK = userService.validate(user)
        return AuthResponse(email= userOK.email, name= userOK.name, id= userOK.id)
    }





}