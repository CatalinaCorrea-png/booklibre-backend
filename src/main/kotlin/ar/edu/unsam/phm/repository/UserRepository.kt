package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.User
import org.springframework.stereotype.Component

@Component
class UserRepository: Repository<User>() {}