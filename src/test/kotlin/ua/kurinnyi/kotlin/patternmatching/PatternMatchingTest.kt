package ua.kurinnyi.kotlin.patternmatching

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import ua.kurinnyi.kotlin.patternmatching.PatternMatching.match
import ua.kurinnyi.kotlin.patternmatching.extractors.EmptyExtractor
import ua.kurinnyi.kotlin.patternmatching.extractors.PairExtractor
import ua.kurinnyi.kotlin.patternmatching.extractors.SingleExtractor
import java.lang.RuntimeException

class PatternMatchingTest {

    @Test
    fun shouldInvokeThenWithObjectWhenTypeMatches() {
        var result = "Goodbye"
        val someString = "Hello, world!"
        someString.match<Unit> {
            case<String>().then { result = this.toUpperCase() }
        }
        assertThat(result).isEqualTo(someString.toUpperCase())
    }

    @Test(expected = MatchError::class)
    fun shouldNotInvokeThenWhenTypeNotMatches() {
        var result = "Goodbye"
        val someInt = 2
        someInt.match<Unit> {
            case<String>().then { result = this.toUpperCase() }
        }
    }

    @Test
    fun shouldInvokeCorrectHandlerByType() {
        var resultString = "Goodbye"
        var resultInt = 2
        2.match<Unit> {
            case<String>().then { resultString = this.toUpperCase() }
            case<Int>().then { resultInt += this }
        }
        assertThat(resultString).isEqualTo("Goodbye")
        assertThat(resultInt).isEqualTo(4)
    }

    @Test
    fun shouldBeAbleToUseInternalFieldOfObjectWhenMatch() {
        var resultString = "Goodbye"
        SomeDataClass("Hello", 5).match<Unit> {
            case<String>().then { resultString = this.toUpperCase() }
            case<SomeDataClass>().then { resultString = someField + someOtherField }
            case<Int>().then { throw RuntimeException("Fail(((") }
        }
        assertThat(resultString).isEqualTo("Hello5")
    }

    @Test
    fun shouldInvokeHandlerWithExactMatch() {
        var resultString = "Goodbye"
        "Hello".match<Unit> {
            case<String>("Hello").then { resultString = it }
            case<String>().then { resultString = this.toUpperCase() }
            case<SomeDataClass>().then { resultString = someField + someOtherField }
            case<Int>().then { throw RuntimeException("Fail(((") }
        }
        assertThat(resultString).isEqualTo("Hello")
    }

    @Test
    fun shouldInvokeHandlerWithExactMatchOfFewResult() {
        var resultString = "Goodbye"
        "Hello".match<Unit> {
            case<SomeDataClass>().then { resultString = someField + someOtherField }
            case<SomeDataClass>().then { resultString = someField + someOtherField }
            case<String>("Hello1", "Hello2").then { resultString = it }
            case<String>("Hello3", "Hello").then { resultString = it }
            case<Int>().then { throw RuntimeException("Fail(((") }
            case<String>().then { resultString = this.toUpperCase() }
        }
        assertThat(resultString).isEqualTo("Hello")
    }

    @Test
    fun shouldReturnResultFromThenBlock() {
        val resultNumber: Int = "Hello".match {
            case<SomeDataClass>().then { 1 }
            case<String>("Hello1", "Hello2").then { 2 }
            case<String>("Hello3", "Hello").then { length }
            case<Int>().then { throw RuntimeException("Fail(((") }
        }
        assertThat(resultNumber).isEqualTo(5)
    }

    @Test(expected = MatchError::class)
    fun shouldNotReturnResultFromWhenNothingMatch() {
        val resultNumber: Int = "Hello".match {
            case<SomeDataClass>().then { 1 }
            case<String>("Hello1", "Hello2").then { 2 }
            case<String>("Hello3").then { length }
            case<Int>().then { throw RuntimeException("Fail(((") }
        }
    }

    @Test(expected = MatchError::class)
    fun shouldNotMatchTheObjectIfAndBlockFailsToMatch() {
        val setOfString = setOf<String>()
        val resultNumber: Int = "Hello".match {
            case("Hello").and { setOfString.contains(this) }.then { it.length }
        }
    }

    @Test
    fun shouldMatchTheObjectIfAndBlockMatches() {
        val setOfString = setOf("Hello")
        val resultNumber: Int = "Hello".match {
            case("Hello").and { setOfString.contains(this) }.then { it.length }
        }
        assertThat(resultNumber).isEqualTo(5);
    }

    @Test
    fun shouldNotMatchTheObjectByTypeIfAndBlockFailsToMatch() {
        val objectToMatch = SomeDataClass("Hello", 4)
        val resultNumber: Int = objectToMatch.match {
            case<SomeOtherDataClass>().then { 3 }
            case<SomeDataClass>().and { someField == "Goodbye" }.then { someOtherField + 1 }
            otherwise { 6 }
        }
        assertThat(resultNumber).isEqualTo(6)
    }

    @Test
    fun shouldMatchTheObjectByTypeIfAndBlockMatches() {
        val objectToMatch = SomeDataClass("Hello", 4)
        val resultNumber: Int = objectToMatch.match {
            case<SomeOtherDataClass>().then { 3 }
            case<SomeDataClass>().and { someField == "Hello" }.then { someOtherField + 1 }
            otherwise { 6 }
        }
        assertThat(resultNumber).isEqualTo(5)
    }

    @Test
    fun shouldNotMatchIfMultipleAndsAndSomeOfThemFailsToMatch() {
        val objectToMatch = SomeDataClass("Hello", 4)
        val resultNumber = objectToMatch.match<Int> {
            case<SomeOtherDataClass>().then { 3 }
            case<SomeDataClass>()
                    .and { someField == "Hello" }.and { someOtherField == 5 }
                    .then { someOtherField + 1 }
            otherwise { 6 }
        }
        assertThat(resultNumber).isEqualTo(6)
    }

    @Test
    fun shouldMatchIfMultipleAndsAndAllOfThemMatches() {
        val objectToMatch = SomeDataClass("Hello", 4)
        val resultNumber = objectToMatch.match<Int> {
            case<SomeOtherDataClass>().then { 3 }
            case<SomeDataClass>().and { someField == "Hello" }
                    .and { someOtherField == 4 }.then { someOtherField + 1 }
            otherwise { 6 }
        }
        assertThat(resultNumber).isEqualTo(5)
    }

    @Test
    fun shouldWorkWithNullableTypeIfReturnedValueIsNotNull() {
        val objectToMatch = SomeDataClass("Hello", 4)
        val resultNumber: Int? = objectToMatch.match {
            case<SomeOtherDataClass>().then { null }
            case<SomeDataClass>().then { someOtherField + 1 }
            otherwise { null }
        }
        assertThat(resultNumber).isEqualTo(5)
    }

    @Test
    fun shouldWorkWithNullableTypeIfReturnedValueIsNull() {
        val objectToMatch = SomeDataClass("Hello", 4)
        val resultNumber: Int? = objectToMatch.match {
            case<SomeOtherDataClass>().then { 1 }
            case<SomeDataClass>().then { null }
            otherwise { 2 }
        }
        assertThat(resultNumber).isEqualTo(null)
    }

    @Test
    fun shouldBeAbleToUseMatchingOnNull() {
        val objectToMatch = null
        val resultNumber: Int? = objectToMatch.match {
            case<SomeOtherDataClass>().then { 1 }
            otherwise { 2 }
        }
        assertThat(resultNumber).isEqualTo(2)
    }

    @Test
    fun shouldBeAbleToMatchNull() {
        val objectToMatch = null
        val resultNumber: Int = objectToMatch.match {
            caseNull().then { 1 }
            otherwise { 2 }
        }
        assertThat(resultNumber).isEqualTo(1)
    }

    @Test
    fun shouldBeAbleToMatchNullInMultipleArguments() {
        val objectToMatch = null
        val resultNumber: Int = objectToMatch.match {
            case(1, null, 2).then { o: Int? -> 1 }
            otherwise { 2 }
        }
        assertThat(resultNumber).isEqualTo(1)
    }

    @Test
    fun shouldBeAbleToUseCustomExtractor() {
        val resultNumber: Int = "3".match {
            case(INT).then { this.toInt() + it }
            otherwise { 2 }
        }
        assertThat(resultNumber).isEqualTo(6)
    }

    @Test
    fun shouldNotMatchCustomExtractorIfItReturnedNull() {
        val objectToMatch = "a"
        val resultNumber: Int = objectToMatch.match {
            case(INT).then { this.toInt() + it }
            otherwise { 2 }
        }
        assertThat(resultNumber).isEqualTo(2)
    }

    object INT : SingleExtractor<String, Int> {
        override fun unapply(from: String): SingleExtractor.Single<Int>? {
            return try {
                SingleExtractor.Single(from.toInt())
            } catch (e: NumberFormatException) {
                null
            }
        }
    }

    @Test
    fun shouldBeAbleToUseCustomExtractorWithTwoExtractedValue() {
        val objectToMatch = "author@domain.com"
        val result: String = objectToMatch.match {
            case(EMAIL).then { author, domain -> "Email $this consist from $author and domain $domain" }
            otherwise { "Not email" }
        }
        assertThat(result).isEqualTo("Email author@domain.com consist from author and domain domain.com")
    }

    @Test
    fun shouldNotMatchCustomExtractorWithTwoExtractedValueIfItReturnsNull() {
        val objectToMatch = "authordomain.com"
        val result: String = objectToMatch.match {
            case(EMAIL).then { author, domain -> "Email $this consist from $author and domain $domain" }
            otherwise { "Not email" }
        }
        assertThat(result).isEqualTo("Not email")
    }

    object EMAIL : PairExtractor<String, String, String> {
        override fun unapply(from: String): PairExtractor.Pair<String, String>? {
            val split = from.split("@")
            return if (split.size == 2) {
                PairExtractor.Pair(split.first(), split.last())
            } else {
                null
            }
        }
    }

    @Test
    fun shouldBeAbleToUseCustomExtractorWithNoExtractedValue() {
        val result: String = "1234321".match {
            case(PALINDROME).then { "Value $this is palindrome" }
            otherwise { "Not palindrome" }
        }
        assertThat(result).isEqualTo("Value 1234321 is palindrome")
    }


    object PALINDROME : EmptyExtractor<String> {
        override fun unapply(from: String): Boolean = from.reversed() == from
    }


    data class SomeDataClass(
            val someField: String,
            val someOtherField: Int
    )

    data class SomeOtherDataClass(
            val superField: Int
    )
}