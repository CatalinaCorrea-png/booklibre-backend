package ar.edu.unsam.phm.repository
import ar.edu.unsam.phm.domain.Author
import org.springframework.stereotype.Component

//@org.springframework.stereotype.Repository
@Component
class AuthorRepository: Repository<Author>() {
}