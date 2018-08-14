package ua.kurinnyi.kotlin.patternmatching

import ua.kurinnyi.kotlin.patternmatching.PatternMatching.MatchContext

typealias head = ListPatterMatching.ListElement.HEAD
typealias tail = ListPatterMatching.ListElement.TAIL
typealias end = ListPatterMatching.ListElement.END
typealias nil = ListPatterMatching.ListElement.END
typealias mid = ListPatterMatching.ListElement.MID

object ListPatterMatching{

    sealed class ListElement {
        object HEAD : ListElement()
        object TAIL : ListElement()
        object MID : ListElement()
        object END : ListElement()
    }

    fun <LIST_TYPE, RESULT> List<LIST_TYPE>.matchList(patterns: ListMatchContext<LIST_TYPE, RESULT>.() -> Unit): RESULT {
        val matchContext = MatchContext<RESULT>(this)
        val listMatchContext = ListMatchContext(this, matchContext)
        patterns(listMatchContext)
        if (!matchContext.fulfilled)
            throw MatchError(this)

        return matchContext.result as RESULT
    }


    class ListMatchContext<LIST_TYPE, RESULT>(
            private val list: List<LIST_TYPE>,
            private var matchContext: MatchContext<RESULT>
    ) {
        fun case(first: end) = MatcherBuilderListEmpty<Nothing, RESULT>(matchContext) {
            list.isEmpty()
        }

        fun case(first: head, last: end) =
                MatcherBuilderListOneElement<LIST_TYPE, RESULT>(matchContext) {
                    list.size == 1
                }

        fun case(first: head, last: tail) =
                MatcherBuilderListOneElementAndTail<LIST_TYPE, RESULT>(matchContext) {
                    list.isNotEmpty()
                }

        fun case(first: head, mid: mid, last: end) =
                MatcherBuilderListTwoElement<LIST_TYPE, RESULT>(matchContext) {
                    list.size == 2
                }

        fun case(first: head, mid: mid, last: tail) =
                MatcherBuilderListTwoElementAndTail<LIST_TYPE, RESULT>(matchContext) {
                    list.size > 1
                }

        fun case(first: LIST_TYPE, last: end) =
                case(head, end).and { head -> head == first }

        fun case(first: LIST_TYPE, last: tail) =
                case(head, tail).and { head, _ -> head == first }

        fun case(first: LIST_TYPE, mid: mid, last: end) =
                case(head, mid, end).and { head,_ -> head == first}

        fun case(first: LIST_TYPE, second: LIST_TYPE, last: end) =
                case(head, mid, end).and { head, mid -> head == first && second == mid }

        fun case(first: head, second: LIST_TYPE, last: end) =
                case(head, mid, end).and { _, mid -> second == mid}

        fun case(first: LIST_TYPE, mid: mid, last: tail) =
                case(head, mid, tail).and{ head, _, _ -> first == head}

        fun case(first: LIST_TYPE, second: LIST_TYPE, last: tail) =
                case(head, mid, tail).and{ head, mid, _ -> first == head && mid == second}

        fun case(first: head, second: LIST_TYPE, last: tail) =
                case(head, mid, tail).and{ _, mid, _ -> mid == second}

        fun otherwise(action: () -> RESULT) {
            if (!matchContext.fulfilled) {
                matchContext.fulfilled = true
                matchContext.result = action()
            }
        }
    }

    class MatcherBuilderListEmpty<T, R>(
            override val matchContext: MatchContext<R>,
            override val predicate: () -> Boolean
    ) : AbstractMatcherListBuilder<T, R>(matchContext, predicate) {
        fun then(action: () -> R) = ifApplies(action)
    }

    open class AbstractMatcherListBuilder<T, RESULT>(
            override val matchContext: MatchContext<RESULT>,
            override val predicate: () -> Boolean
    ) : PatternMatching.AbstractMatcherBuilder<T, RESULT>(matchContext, predicate) {

        protected fun <R> withList(action: (List<T>) -> R): () -> R = {
            action(matchContext.value as List<T>)
        }
    }

    class MatcherBuilderListOneElement<T, RESULT>(
            override val matchContext: MatchContext<RESULT>,
            override val predicate: () -> Boolean
    ) : AbstractMatcherListBuilder<T, RESULT>(matchContext, predicate) {

        fun and(predicate: List<T>.(T) -> Boolean): MatcherBuilderListOneElement<T, RESULT> =
                MatcherBuilderListOneElement(matchContext,
                        withList { predicate() && it.predicate(it.first()) })

        fun then(action: List<T>.(T) -> RESULT) = ifApplies(withList { action(it, it.first()) })
    }


    class MatcherBuilderListOneElementAndTail<T, RESULT>(
            override val matchContext: MatchContext<RESULT>,
            override val predicate: () -> Boolean
    ) : AbstractMatcherListBuilder<T, RESULT>(matchContext, predicate) {

        fun and(predicate: List<T>.(T, List<T>) -> Boolean) =
                MatcherBuilderListOneElementAndTail<T, RESULT>(matchContext,
                        withList { predicate() && predicate(it, it.first(), it.takeLast(it.size - 1)) })

        fun then(action: List<T>.(T, List<T>) -> RESULT) =
                ifApplies(withList { action(it, it.first(), it.takeLast(it.size - 1)) })
    }

    class MatcherBuilderListTwoElement<T, RESULT>(
            override val matchContext: MatchContext<RESULT>,
            override val predicate: () -> Boolean
    ) : AbstractMatcherListBuilder<T, RESULT>(matchContext, predicate) {

        fun and(predicate: List<T>.(T, T) -> Boolean): MatcherBuilderListTwoElement<T, RESULT> =
                MatcherBuilderListTwoElement(matchContext,
                        withList { predicate() && predicate(it, it[0], it[1]) })

        fun then(action: List<T>.(T, T) -> RESULT) =
                ifApplies(withList { action(it, it[0], it[1]) })
    }

    class MatcherBuilderListTwoElementAndTail<T, RESULT>(
            override val matchContext: MatchContext<RESULT>,
            override val predicate: () -> Boolean
    ) : AbstractMatcherListBuilder<T, RESULT>(matchContext, predicate) {

        fun and(predicate: List<T>.(T, T, List<T>) -> Boolean) =
                MatcherBuilderListTwoElementAndTail<T, RESULT>(matchContext,
                        withList { predicate() && predicate(it, it[0], it[1], it.takeLast(it.size - 2)) })

        fun then(action: List<T>.(T, T, List<T>) -> RESULT) =
                ifApplies(withList { action(it, it[0], it[1], it.takeLast(it.size - 2)) })
    }
}