package ua.kurinnyi.kotlin.patternmatching

import ua.kurinnyi.kotlin.patternmatching.extractors.*

class MatchError(value: Any?) : Exception("None of provided matchers matches: $value")

object PatternMatching {

    fun <RESULT> Any?.match(patterns: MatchContext<RESULT>.() -> Unit): RESULT {
        val matchContext = MatchContext<RESULT>(this)
        patterns(matchContext)
        if (!matchContext.fulfilled)
            throw MatchError(this)

        return matchContext.result as RESULT
    }

    class MatchContext<RESULT>(
            val value: Any?,
            var result: RESULT? = null,
            var fulfilled: Boolean = false
    ) {

        inline fun <reified T> case(): MatcherBuilderOneElement<T, T, RESULT> =
                case(TypeCheckSingleExtractor<T>())

        fun caseNull() = case(NullEmptyExtractor())

        inline fun <reified T> case(vararg values: T): MatcherBuilderOneElement<T, T, RESULT> =
                caseE<T, RESULT, T>(VarArgAnyMatchSingleExtractor(values))

        inline fun <reified FROM, EXTRACTED> case(extractor: SingleExtractor<FROM, EXTRACTED>) =
                caseE<FROM, RESULT, EXTRACTED>(extractor)

        inline fun <reified FROM, E1, E2> case(extractor: PairExtractor<FROM, E1, E2>) =
                caseE<FROM, RESULT, E1, E2>(extractor)

        inline fun <reified FROM, E1, E2, E3> case(extractor: TripleExtractor<FROM, E1, E2, E3>) =
                caseE<FROM, RESULT, E1, E2, E3>(extractor)

        inline fun <reified FROM> case(extractor: EmptyExtractor<FROM>) = caseE(extractor)

        fun otherwise(action: () -> RESULT) = case(AlwaysMatchingExtractor()).then { action() }
    }

    class TypeCheckSingleExtractor<T> : SingleExtractor<T, T> {
        override fun unapply(from: T): SingleExtractor.Single<T>? = SingleExtractor.Single(from)
    }

    class VarArgAnyMatchSingleExtractor<T>(private val values: Array<out T>) : SingleExtractor<T, T> {

        override fun unapply(from: T): SingleExtractor.Single<T>? =
                values.takeIf { it.contains(from) }?.let {
                    SingleExtractor.Single(from)
                }
    }

    class NullEmptyExtractor : EmptyExtractor<Any?> {

        override fun unapply(from: Any?): Boolean = from == null
    }

    class AlwaysMatchingExtractor : EmptyExtractor<Any?> {

        override fun unapply(from: Any?) = true
    }

}
