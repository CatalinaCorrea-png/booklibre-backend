package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.User
import org.springframework.stereotype.Component

//@org.springframework.stereotype.Repository
@Component
class UserRepository: Repository<User>() {}