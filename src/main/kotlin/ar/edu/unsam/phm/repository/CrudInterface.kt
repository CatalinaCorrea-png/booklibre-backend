package ar.edu.unsam.phm.repository

import jakarta.persistence.EntityManager
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.NoRepositoryBean

@NoRepositoryBean
interface CrudInterface<Type: RepositoryElement>: CrudRepository<Type, Long>

class CrudInterfaceImpl<Type : RepositoryElement>(
    private val entityInformation: JpaEntityInformation<Type, Long>,
    private val entityManager: EntityManager
) : SimpleJpaRepository<Type, Long>(entityInformation, entityManager), CrudInterface<Type> {

    override fun <SubClass : Type> save(entity: SubClass): SubClass {
        entity.validate()
        return super.save(entity)
    }
}