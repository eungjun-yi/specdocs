package im.toss.test.specdocs.examples

import im.toss.test.equalsTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.time.YearMonth

@DisplayName("그레고리력")
class CalendarSpecTest {

    @TestFactory
    @DisplayName("1, 3, 5, 7, 8, 10, 12 월은 31일까지 있다.")
    fun `31daysMonths`(): List<DynamicTest> {
        val months = listOf(1, 3, 5, 7, 8, 10, 12)
        return listOf(2019, 2018, 1).map { year ->
            dynamicTest("${year}년 ${months.joinToString(", ")}월은 31일까지 있다.") {
                months.map { month ->
                    YearMonth.of(year, month)
                }.map {
                    it.atEndOfMonth().dayOfMonth
                }.equalsTo(
                    generateSequence { 31 }.take(months.size).toList()
                )
            }
        }
    }

    @TestFactory
    @DisplayName("4, 6, 9, 11 월은 30일까지 있다.")
    fun `30daysMonths`(): List<DynamicTest> {
        val months = listOf(4, 6, 9, 11)
        return listOf(2019 to 30, 2018 to 30, 1 to 30).map { (year, expectedDays) ->
            test(year, months, expectedDays)
        }
    }

    @TestFactory
    @DisplayName("윤년이 아닌 해의 2월은 28일까지 있다.")
    fun februaryInNonLeapYear(): List<DynamicTest> {
        val months = listOf(2)
        return listOf(2019 to 28, 2018 to 28).map { (year, expectedDays) ->
            test(year, months, expectedDays)
        }
    }

    @TestFactory
    @DisplayName("윤년인 해의 2월은 29일까지 있다.")
    fun februaryInLeapYear(): List<DynamicTest> {
        val months = listOf(2)
        return listOf(2016 to 29, 2012 to 29).map { (year, expectedDays) ->
            test(year, months, expectedDays)
        }
    }

    private fun test(
        year: Int,
        months: List<Int>,
        expectedDays: Int
    ) = dynamicTest("${year}년 ${months.joinToString(", ")}월은 ${expectedDays}일까지 있다.") {
        months.map { month ->
            YearMonth.of(year, month)
        }.map {
            it.atEndOfMonth().dayOfMonth
        }.equalsTo(
            generateSequence { expectedDays }.take(months.size).toList()
        )
    }
}
