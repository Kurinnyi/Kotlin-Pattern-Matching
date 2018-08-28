package ua.kurinnyi.kotlin.patternmatching

import ua.kurinnyi.kotlin.patternmatching.PatternMatching.MatchContext
import ua.kurinnyi.kotlin.patternmatching.extractors.*

typealias head = ListPatterMatching.ListElement.HEAD
typealias tail = ListPatterMatching.ListElement.TAIL
typealias end = ListPatterMatching.ListElement.END
typealias nil = ListPatterMatching.ListElement.END
typealias mid = ListPatterMatching.ListElement.MID

object ListPatterMatching {

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
        fun case(first: end) = matchContext.case(NoElementListExtractor<LIST_TYPE>())

        fun case(first: head, last: end) = matchContext.case(OneElementListExtractor<LIST_TYPE>())

        fun case(first: LIST_TYPE, last: end) =
                case(head, end).and { head -> head == first }

        fun case(first: head, last: tail) =
                matchContext.case(OneElementAndTailListExtractor<LIST_TYPE>())

        fun case(first: head, mid: mid, last: end) =
                matchContext.case(TwoElementListExtractor<LIST_TYPE>())

        fun case(first: head, mid: mid, last: tail) =
                matchContext.case(TwoElementAndTailListExtractor<LIST_TYPE>())

        fun case(first: LIST_TYPE, last: tail) =
                case(head, tail).and { head, _ -> head == first }

        fun case(first: LIST_TYPE, mid: mid, last: end) =
                case(head, mid, end).and { head, _ -> head == first }

        fun case(first: LIST_TYPE, second: LIST_TYPE, last: end) =
                case(first, mid, end).and { _, mid -> second == mid }

        fun case(first: head, second: LIST_TYPE, last: end) =
                case(head, mid, end).and { _, mid -> second == mid }

        fun case(first: LIST_TYPE, mid: mid, last: tail) =
                case(head, mid, tail).and { head, _, _ -> first == head }

        fun case(first: LIST_TYPE, second: LIST_TYPE, last: tail) =
                case(first, mid, tail).and { _, mid, _ -> mid == second }

        fun case(first: head, second: LIST_TYPE, last: tail) =
                case(head, mid, tail).and { _, mid, _ -> mid == second }

        fun otherwise(action: () -> RESULT) = matchContext.case(PatternMatching.AlwaysMatchingExtractor()).then { action() }
    }
}

class OneElementListExtractor<T>() : SingleExtractor<List<T>, T> {
    override fun unapply(from: List<T>) = from.takeIf { it.size == 1 }?.let {
        SingleExtractor.Single(it.first())
    }
}

class NoElementListExtractor<T>() : EmptyExtractor<List<T>> {
    override fun unapply(from: List<T>) = from.isEmpty()
}

class TwoElementListExtractor<T>() : PairExtractor<List<T>, T, T> {
    override fun unapply(from: List<T>) = from.takeIf { it.size == 2 }?.let {
        PairExtractor.Pair(it.first(), it.last())
    }
}

class OneElementAndTailListExtractor<T>() : PairExtractor<List<T>, T, List<T>> {
    override fun unapply(from: List<T>) = from.takeIf { it.isNotEmpty() }?.let {
        PairExtractor.Pair(it.first(), it.takeLast(it.size - 1))
    }
}

class TwoElementAndTailListExtractor<T>() : TripleExtractor<List<T>, T, T, List<T>> {
    override fun unapply(from: List<T>) = from.takeIf { it.size > 1 }?.let {
        TripleExtractor.Triple(it[0], it[1], it.takeLast(it.size - 2))
    }
}

