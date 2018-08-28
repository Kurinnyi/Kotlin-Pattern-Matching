package ua.kurinnyi.kotlin.patternmatching.extractors

import ua.kurinnyi.kotlin.patternmatching.PatternMatching

interface SingleExtractor<From, To> {

    fun unapply(from: From): Single<To>?

    data class Single<To>(val value: To)
}

inline fun <reified FROM, RESULT, EXTRACTED> PatternMatching.MatchContext<RESULT>.caseE(extractor: SingleExtractor<FROM, EXTRACTED>)
        : MatcherBuilderOneElement<FROM, EXTRACTED, RESULT> {
    if (!fulfilled && value is FROM) {
        val unapplied = extractor.unapply(value)
        if (unapplied != null)
            return MatcherBuilderExtractedOneElement(this, unapplied.value, value)
    }
    return FailedMatcherBuilderExtractedOneElement()
}

interface MatcherBuilderOneElement<F, E, RESULT> {
    fun and(predicate: F.(E) -> Boolean): MatcherBuilderOneElement<F, E, RESULT>

    fun then(action: F.(E) -> RESULT)
}

class MatcherBuilderExtractedOneElement<F, E, RESULT>(
        private val matchContext: PatternMatching.MatchContext<RESULT>,
        private val extractedElement: E,
        private val value: F
) : MatcherBuilderOneElement<F, E, RESULT> {
    override fun and(predicate: F.(E) -> Boolean): MatcherBuilderOneElement<F, E, RESULT> {
        return if (!matchContext.fulfilled && value.predicate(extractedElement)) {
            this
        } else {
            FailedMatcherBuilderExtractedOneElement()
        }
    }

    override fun then(action: F.(E) -> RESULT) {
        matchContext.fulfilled = true
        matchContext.result = value.action(extractedElement)
    }
}

class FailedMatcherBuilderExtractedOneElement<F, E, RESULT> : MatcherBuilderOneElement<F, E, RESULT> {

    override fun and(predicate: F.(E) -> Boolean) = this

    override fun then(action: F.(E) -> RESULT) {
        //do nothing
    }
}

