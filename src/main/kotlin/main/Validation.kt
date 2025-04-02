package main

object Validation {

    fun validateIdentifier(identifier: String) {
        assert(identifier.isNotBlank())
        assert(identifier.isNotBlank())
        assert(identifier.contains(";").not())
        assert(identifier.contains(",").not())
        assert(identifier.contains("\n").not())
    }

}