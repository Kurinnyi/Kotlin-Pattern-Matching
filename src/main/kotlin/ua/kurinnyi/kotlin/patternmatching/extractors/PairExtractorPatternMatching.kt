package ua.kurinnyi.kotlin.patternmatching.extractors

import ua.kurinnyi.kotlin.patternmatching.PatternMatching


interface PairExtractor<From, To1, To2> {

    fun unapply(from: From): Pair<To1, To2>?

    data class Pair<To1, To2>(val first: To1, val second: To2)
}

inline fun <reified FROM, RESULT, E1, E2> PatternMatching.MatchContext<RESULT>.caseE(extractor: PairExtractor<FROM, E1, E2>)
        : MatcherBuilderTwoElement<FROM, E1, E2, RESULT> {
    if (!fulfilled && value is FROM) {
        val unapplied = extractor.unapply(value)
        if (unapplied != null) {
            return MatcherBuilderExtractedTwoElement(this, unapplied, value)
        }
    }
    return FailedMatcherBuilderExtractedTwoElement()
}

interface MatcherBuilderTwoElement<F, E1, E2, RESULT> {
    fun and(predicate: F.(E1, E2) -> Boolean): MatcherBuilderTwoElement<F, E1, E2, RESULT>

    fun then(action: F.(E1, E2) -> RESULT)
}

class MatcherBuilderExtractedTwoElement<F, E1, E2, RESULT>(
        private val matchContext: PatternMatching.MatchContext<RESULT>,
        private val extracted: PairExtractor.Pair<E1, E2>,
        private val value: F
) : MatcherBuilderTwoElement<F, E1, E2, RESULT> {
    override fun and(predicate: F.(E1, E2) -> Boolean): MatcherBuilderTwoElement<F, E1, E2, RESULT> {
        return if (!matchContext.fulfilled && value.predicate(extracted.first, extracted.second)) {
            this
        } else {
            FailedMatcherBuilderExtractedTwoElement()
        }
    }

    override fun then(action: F.(E1, E2) -> RESULT) {
        matchContext.fulfilled = true
        matchContext.result = value.action(extracted.first, extracted.second)
    }
}

class FailedMatcherBuilderExtractedTwoElement<F, E1, E2, RESULT> : MatcherBuilderTwoElement<F, E1, E2, RESULT> {

    override fun and(predicate: F.(E1, E2) -> Boolean) = this

    override fun then(action: F.(E1, E2) -> RESULT) {
        //do nothing
    }
}
