package ua.kurinnyi.kotlin.patternmatching.extractors

import ua.kurinnyi.kotlin.patternmatching.PatternMatching


interface TripleExtractor<From, To1, To2, To3> {

    fun unapply(from: From): Triple<To1, To2, To3>?

    data class Triple<To1, To2, To3>(val first: To1, val second: To2, val third: To3)
}

inline fun <reified FROM, RESULT, E1, E2, E3> PatternMatching.MatchContext<RESULT>.caseE(extractor: TripleExtractor<FROM, E1, E2, E3>)
        : MatcherBuilderThreeElement<FROM, E1, E2, E3, RESULT> {
    if (!fulfilled && value is FROM) {
        val unapplied = extractor.unapply(value)
        if (unapplied != null) {
            return MatcherBuilderExtractedThreeElement(this, unapplied, value)
        }
    }
    return FailedMatcherBuilderExtractedThreeElement()
}

interface MatcherBuilderThreeElement<F, E1, E2, E3, RESULT> {
    fun and(predicate: F.(E1, E2, E3) -> Boolean): MatcherBuilderThreeElement<F, E1, E2, E3, RESULT>

    fun then(action: F.(E1, E2, E3) -> RESULT)
}

class MatcherBuilderExtractedThreeElement<F, E1, E2, E3, RESULT>(
        private val matchContext: PatternMatching.MatchContext<RESULT>,
        private val extracted: TripleExtractor.Triple<E1, E2, E3>,
        private val value: F
) : MatcherBuilderThreeElement<F, E1, E2, E3, RESULT> {
    override fun and(predicate: F.(E1, E2, E3) -> Boolean): MatcherBuilderThreeElement<F, E1, E2, E3, RESULT> {
        return if (!matchContext.fulfilled && value.predicate(extracted.first, extracted.second, extracted.third)) {
            this
        } else {
            FailedMatcherBuilderExtractedThreeElement()
        }
    }

    override fun then(action: F.(E1, E2, E3) -> RESULT) {
        matchContext.fulfilled = true
        matchContext.result = value.action(extracted.first, extracted.second, extracted.third)
    }
}

class FailedMatcherBuilderExtractedThreeElement<F, E1, E2, E3, RESULT> : MatcherBuilderThreeElement<F, E1, E2, E3, RESULT> {

    override fun and(predicate: F.(E1, E2, E3) -> Boolean) = this

    override fun then(action: F.(E1, E2, E3) -> RESULT) {
        //do nothing
    }
}
