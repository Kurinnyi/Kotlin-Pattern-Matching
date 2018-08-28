package ua.kurinnyi.kotlin.patternmatching.extractors

import ua.kurinnyi.kotlin.patternmatching.PatternMatching


interface EmptyExtractor<From> {

    fun unapply(from: From): Boolean
}

inline fun <reified FROM, RESULT> PatternMatching.MatchContext<RESULT>.caseE(extractor: EmptyExtractor<FROM>)
        : MatcherBuilderNoElement<FROM, RESULT> {
    if (!fulfilled && value is FROM) {
        if (extractor.unapply(value))
            return MatcherBuilderExtractedNoElement(this, value)
    }
    return FailedMatcherBuilderExtractedNoElement()
}


interface MatcherBuilderNoElement<FROM, RESULT> {
    fun and(predicate: FROM.() -> Boolean): MatcherBuilderNoElement<FROM, RESULT>

    fun then(action: FROM.() -> RESULT)
}

class MatcherBuilderExtractedNoElement<FROM, RESULT>(
        private val matchContext: PatternMatching.MatchContext<RESULT>,
        private val value: FROM
) : MatcherBuilderNoElement<FROM, RESULT> {
    override fun and(predicate: FROM.() -> Boolean): MatcherBuilderNoElement<FROM, RESULT> {
        return if (!matchContext.fulfilled && predicate(value)) {
            this
        } else {
            FailedMatcherBuilderExtractedNoElement()
        }
    }

    override fun then(action: FROM.() -> RESULT) {
        matchContext.fulfilled = true
        matchContext.result = action(value)
    }
}

class FailedMatcherBuilderExtractedNoElement<FROM, RESULT> : MatcherBuilderNoElement<FROM, RESULT> {

    override fun and(predicate: FROM.() -> Boolean) = this

    override fun then(action: FROM.() -> RESULT) {
        //do nothing
    }
}