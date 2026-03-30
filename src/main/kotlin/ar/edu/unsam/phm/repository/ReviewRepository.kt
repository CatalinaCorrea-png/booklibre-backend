package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Review
import org.springframework.stereotype.Component


//@org.springframework.stereotype.Repository
@Component
class ReviewRepository: Repository<Review>() {}