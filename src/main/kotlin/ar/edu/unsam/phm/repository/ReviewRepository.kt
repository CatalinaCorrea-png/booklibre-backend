package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Review
import org.springframework.stereotype.Component


@Component
class ReviewRepository: Repository<Review>() {}