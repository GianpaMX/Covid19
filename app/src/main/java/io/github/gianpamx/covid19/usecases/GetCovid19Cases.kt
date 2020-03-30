package io.github.gianpamx.covid19.usecases

import java.util.*

class GetCovid19Cases(private val covid19DataSource: Covid19DataSource) {
    data class DateNumber(val date: Int, val number: Int)
    data class CasesByCountry(val cases: Map<String, List<DateNumber>>)

    operator fun invoke() = covid19DataSource
        .getData()
        .groupBy { it.country }
        .flatMapSingle {
            it.collect(
                { mutableMapOf() },
                { response: MutableMap<String, MutableList<DateNumber>>, dataRow: Covid19DataSource.DataRow ->
                    val countryKey = it.key!!

                    if (!response.containsKey(countryKey)) {
                        response[countryKey] = mutableListOf()
                    }

                    val existingIndex = response[countryKey]?.indexOfFirst { dateNumber ->
                        dateNumber.date == dataRow.date.toTimeInSeconds()
                    } ?: -1

                    if (existingIndex == -1) {
                        response[countryKey]?.add(dataRow.toDateNumber())
                    } else {
                        val existingDateNumber = response[countryKey]?.get(existingIndex)!!
                        response[countryKey]?.set(
                            existingIndex,
                            existingDateNumber.add(newCases = dataRow.cases.toInt())
                        )
                    }

                    response[countryKey]?.sortBy { dateNumber -> dateNumber.date }
                })
        }.reduceWith({ CasesByCountry(emptyMap()) },
            { response: CasesByCountry, map: MutableMap<String, MutableList<DateNumber>> ->
                response.copy(cases = response.cases.toMutableMap().apply {
                    putAll(map)
                })
            })

    private fun Covid19DataSource.DataRow.toDateNumber() = DateNumber(
        date = this.date.toTimeInSeconds(),
        number = this.cases.toInt()
    )

    private fun String.toTimeInSeconds(): Int {
        val (year, month, day) = this.splitInDateInParts()

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(year, month, day, 0, 0, 0)
        }

        return (calendar.timeInMillis / 1_000).toInt()
    }

    private fun String.splitInDateInParts() = split('/').run {
        listOf(this[2].toInt() + 2000, this[0].toInt() - 1, this[1].toInt())
    }

    private fun DateNumber.add(newCases: Int) = copy(number = this.number + newCases)
}
