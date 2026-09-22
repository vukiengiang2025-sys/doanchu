package com.example.solver

object CandidateFilter {

    /**
     * Filters a collection of words using the constraints.
     */
    fun filter(words: Sequence<String>, constraints: GameConstraints): List<String> {
        return words.filter { constraints.matches(it) }.toList()
    }

    /**
     * Filters a list of candidate words using the constraints.
     */
    fun filter(words: List<String>, constraints: GameConstraints): List<String> {
        return words.filter { constraints.matches(it) }
    }
}
