package ua.kurinnyi.kotlin.patternmatching

class MatchError(value: Any?) : Exception("None of provided matchers matches: $value")

object PatternMatching{

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

        inline fun <reified T> case() = MatcherBuilder<T, RESULT>(this) {
            value is T
        }

        fun <T> case(vararg value: T) = MatcherBuilder<T, RESULT>(this) {
            value.any { it == this.value }
        }

        fun otherwise(action: () -> RESULT) {
            if (!fulfilled) {
                fulfilled = true
                result = action()
            }
        }
    }

    open class AbstractMatcherBuilder<T, RESULT>(
            open val matchContext: MatchContext<RESULT>,
            open val predicate: () -> Boolean) {

        fun and(predicate: T.() -> Boolean): MatcherBuilder<T, RESULT> {
            return MatcherBuilder(matchContext) {
                val value = matchContext.value as T
                predicate() && predicate(value)
            }
        }

        protected fun ifApplies(action: () -> RESULT) {
            if (matchContext.fulfilled) return
            if (predicate()) {
                matchContext.fulfilled = true
                matchContext.result = action()
            }
        }
    }

    open class MatcherBuilder<T, RESULT>(
            override val matchContext: MatchContext<RESULT>,
            override val predicate: () -> Boolean
    ) : AbstractMatcherBuilder<T, RESULT>(matchContext, predicate) {

        fun then(action: T.(T) -> RESULT) = ifApplies {
            val value = matchContext.value as T
            value.action(value)
        }

    }
}
