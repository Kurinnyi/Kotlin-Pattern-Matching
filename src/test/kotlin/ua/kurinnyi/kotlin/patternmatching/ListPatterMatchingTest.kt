package ua.kurinnyi.kotlin.patternmatching

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import ua.kurinnyi.kotlin.patternmatching.ListPatterMatching.matchList

class ListPatterMatchingTest {

    @Test
    fun shouldMatchIfListIsEmpty() {
        val emptyList = emptyList<String>()
        val resultNumber: Int = emptyList.matchList {
            case(nil).then { 2 + 2 }
        }
        assertThat(resultNumber).isEqualTo(4)
    }

    @Test(expected = MatchError::class)
    fun shouldNotMatchIfListIsNotEmpty() {
        val list = listOf("")
        val resultNumber: Int = list.matchList {
            case(nil).then { 2 + 2 }
        }
    }

    @Test
    fun shouldMatchIfListHasOneElement() {
        val list = listOf("Hello")
        val resultString: String = list.matchList {
            case(head, end).then { head -> head.toUpperCase() }
        }
        assertThat(resultString).isEqualTo("HELLO")
    }

    @Test
    fun shouldNotMatchIfListHasNoElement() {
        val list = listOf<String>()
        val resultString: String = list.matchList {
            case(head, end).then { head -> head.toUpperCase() }
            otherwise { "Goodbye" }
        }
        assertThat(resultString).isEqualTo("Goodbye")
    }

    @Test(expected = MatchError::class)
    fun shouldNotMatchIfListHasMoreThanOneElement() {
        val list = listOf<String>("Hi", "Hello");
        val resultString: String = list.matchList {
            case(head, end).then { head -> head.toUpperCase() }
        }
    }


    @Test
    fun shouldMatchIfListHasOneExactMatchingElement() {
        val list = listOf("Hello")
        val resultString: String = list.matchList {
            case("Hi", end).then { head -> head }
            case("Hello", end).then { head -> head.toUpperCase() }
            case("Goodbye", end).then { head -> head }
        }
        assertThat(resultString).isEqualTo("HELLO")
    }

    @Test
    fun shouldNotMatchIfListHasNotExactMatchElement() {
        val list = listOf<String>("Hi")
        val resultString: String = list.matchList {
            case("Hello", end).then { head -> head.toUpperCase() }
            otherwise { "Goodbye" }
        }
        assertThat(resultString).isEqualTo("Goodbye")
    }

    @Test(expected = MatchError::class)
    fun shouldNotMatchIfListHasMoreThanOneElementEvenWithExactMatch() {
        val list = listOf<String>("Hi", "Hello");
        val resultString: String = list.matchList {
            case("Hi", end).then { head -> head.toUpperCase() }
            case("Hello", end).then { head -> head.toUpperCase() }
        }
    }

    @Test
    fun shouldMatchIfListHasAtLeastOneElement() {
        val list = listOf<String>("Hello", "Hi")
        val resultList: List<String> = list.matchList {
            case("Hello", tail).then { head, tail ->
                val list = mutableListOf(head)
                list.addAll(tail)
                list
            }
            otherwise { emptyList() }
        }
        assertThat(resultList).isEqualTo(list)
    }

    @Test
    fun shouldMatchIfListHasExactlyOneElementThenTailIsEmpty() {
        val list = listOf<String>("Hello")
        val resultString: String = list.matchList {
            case("Hello", tail).then { head, tail ->
                assertThat(tail).isEmpty()
                head.toUpperCase()
            }
            otherwise { "Goodbye" }
        }
        assertThat(resultString).isEqualTo("HELLO")
    }

    @Test
    fun shouldMatchIfListHasExactlyTwoElements() {
        val list = listOf<String>("Hello", "Hi")
        val resultString: String = list.matchList {
            case(head, "Hi", end).then { head, mid -> head + mid }
            otherwise { "Goodbye" }
        }
        assertThat(resultString).isEqualTo("HelloHi")
    }

    @Test
    fun shouldNotMatchIfListHasMoreThenTwoElements() {
        val list = listOf<String>("Hello", "Hi", "End")
        val resultString: String = list.matchList {
            case(head, "Hi", end).then { head, mid -> head + mid }
            otherwise { "Goodbye" }
        }
        assertThat(resultString).isEqualTo("Goodbye")
    }

    @Test
    fun shouldMatchIfListHasExactlyTwoElementsThenTailIsEmpty() {
        val list = listOf<String>("Hello", "Hi")
        val resultString: String = list.matchList {
            case("Hello", "Hi", tail).then { head, mid, tail ->
                assertThat(tail).isEmpty()
                head + mid
            }
            otherwise { "Goodbye" }
        }
        assertThat(resultString).isEqualTo("HelloHi")
    }

    @Test
    fun shouldMatchIfListHasMoreThanTwoElements() {
        val list = listOf<String>("Hello", "Hi", "Not end", "End")
        val resultString: String = list.matchList {
            case(head, mid, tail).then { head, mid, tail ->
                assertThat(tail).containsExactly("Not end", "End")
                head + mid
            }
            otherwise { "Goodbye" }
        }
        assertThat(resultString).isEqualTo("HelloHi")
    }

    @Test
    fun shouldNotMatchIfAndPartIsNotMatches() {
        val list = listOf<String>("Hello", "Hi")
        val resultString: String = list.matchList {
            case("Hello", mid, end).and { _, mid -> mid != "Hi" }.then { head, mid -> head + mid }
            case("Hello", "Hi", end).then { _, _ -> "Result" }
            otherwise { "Goodbye" }
        }
        assertThat(resultString).isEqualTo("Result")
    }

    @Test
    fun shouldMatchIfAndPartMatches() {
        val list = listOf<String>("Hello", "Hi", "HI")
        val resultInt: Int = list.matchList {
            case("Hello", mid, tail)
                    .and { _, mid, tail -> tail.contains(mid.toUpperCase()) }
                    .then { head, _, tail -> head.length + tail.size }
            case(head, "Hi", tail).then { _, _, _ -> 4 }
        }
        assertThat(resultInt).isEqualTo(6)
    }

    @Test
    fun shouldWorkIfNoReturnValueExpected() {
        val list = listOf<String>("Hello", "Hi", "HI")
        val resultList = mutableListOf<String>()
        list.matchList<String, Unit> {
            case("Hello", mid, tail).then { head, _, tail -> tail.forEach { resultList.add(it) } }
            case(head, "Hi", tail).then { head, _, _ -> resultList.add(head) }
        }
        assertThat(resultList).containsExactly("HI")
    }


    @Test
    fun shouldWorkWithNullableTypesIfReturnValueIsNotNull() {
        val list = listOf<String>("Hello", "Hi", "HI")
        val resultInt: Int? = list.matchList {
            case("Hello", mid, tail).then { head, _, tail -> head.length + tail.size }
            case(head, "Hi", tail).then { _, _, _ -> null }
        }
        assertThat(resultInt).isEqualTo(6)
    }

    @Test
    fun shouldWorkWithNullableTypesIfReturnValueIsNull() {
        val list = listOf<String>("Hello", "Hi", "HI")
        val resultInt: Int? = list.matchList {
            case("Hello", mid, tail).then { head, _, tail -> null }
            case(head, "Hi", tail).then { _, _, _ -> 1 }
        }
        assertThat(resultInt).isEqualTo(null)
    }

    @Test
    fun shouldWorkWithListOfNullableValues() {
        val list = listOf<String?>("Hello", null, "HI")
        val resultInt: Int = list.matchList {
            case("Hello", mid, tail).then { _, _, _ -> 1 }
            case(head, null, tail).then { _, _, _ -> 2 }
        }
        assertThat(resultInt).isEqualTo(1)
    }

    @Test
    fun shouldBeAbleToUseNullToMatchAgainstIt() {
        val list = listOf<String?>("Hello", null, "HI")
        val resultInt: Int = list.matchList {
            case("Hello", "Hi", tail).then { _, _, _ -> 1 }
            case(head, null, tail).then { _, _, _ -> 2 }
        }
        assertThat(resultInt).isEqualTo(2)
    }
}