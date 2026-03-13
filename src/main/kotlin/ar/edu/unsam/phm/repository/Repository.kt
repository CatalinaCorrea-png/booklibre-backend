package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.errors.ConflictException
import ar.edu.unsam.phm.errors.NotFoundException

open class Repository <Type: RepositoryElement> {
    private var idCounter: Int = 1
    private val collection: MutableList<Type> = mutableListOf()

    private fun getIDs(): List<Int> = collection.map { it.id }

    private fun generateID(): Int = (this.getIDs().maxOrNull() ?: 0) + 1

    fun repositoryObjects(): List<Type> = this.collection

    fun create(repositoryObject: Type): Unit {
        if (!repositoryObject.meetsNewCriteria()) {
            throw ConflictException("El objeto no puede generarse en el repositorio ya que no es nuevo. ID del objeto: ${repositoryObject.id}")
        }
        repositoryObject.id = generateID()
        collection.add(repositoryObject)
    }

    fun removeFromCollection(id: Int): Unit {
        collection.remove(this.getObject(id))
    }

     fun findIndexInCollection(id: Int): Int {
        val index = this.collection.indexOfFirst { item -> item.id == id }
        if (index == -1) {
            throw NotFoundException("No existe un indice donde exista este elemento en el repositorio. ID: ${id}")
        }
        return index
    }

    fun update(updatedObject: Type): Unit {
        val index = this.findIndexInCollection(updatedObject.id)
        this.collection[index] = updatedObject
    }

    private fun objectInCollection(id: Int): Boolean =
        this.collection.any { item -> item.id == id }

    private fun findObject(id: Int): Type =
        this.collection.find { item -> item.id == id }!!

    fun getObject(id: Int): Type {
        if (!objectInCollection(id)) {
            throw NotFoundException("No existe el id: $id en el repositorio")
        }
        return this.findObject(id)
    }

    fun search(criteria: String): List<Type> =
        this.collection.filter { item -> item.meetsSearchCriteria(criteria) }

}