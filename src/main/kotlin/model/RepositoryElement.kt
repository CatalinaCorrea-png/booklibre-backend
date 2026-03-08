package model

interface RepositoryElement {
    var id: Int

    fun matchesPartiallyWith(criteria: String, compareTo: String): Boolean =
        compareTo.contains(criteria, ignoreCase = true)

    fun matchesTotallyWith(criteria: String, compareTo: String): Boolean =
        compareTo.equals(criteria, ignoreCase = true)

    fun isNotEmpty(criteria: String) = criteria.isNotBlank()

    fun meetsSearchCriteria(criteria: String): Boolean

    fun meetsCreationCriteria(): Boolean

    fun meetsNewCriteria(): Boolean = this.id == 0


}