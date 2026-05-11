package ar.edu.unsam.phm.repository

interface RepositoryElement {
    var id: String?

    fun matchesPartiallyWith(criteria: String, compareTo: String): Boolean =
        compareTo.contains(criteria, ignoreCase = true)

    fun matchesTotallyWith(criteria: String, compareTo: String): Boolean =
        compareTo.equals(criteria, ignoreCase = true)

    fun isNotEmpty(criteria: String) = criteria.isNotBlank()

    fun meetsSearchCriteria(criteria: String): Boolean

    fun validate()

    fun meetsNewCriteria(): Boolean = this.id == null

}