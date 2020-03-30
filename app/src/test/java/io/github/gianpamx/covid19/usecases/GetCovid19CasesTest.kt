package io.github.gianpamx.covid19.usecases

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class GetCovid19CasesTest {
    @Mock
    lateinit var covid19DataSource: Covid19DataSource

    @Test
    fun `Duplicated dates for the same country`() {
        val covid19Repository = GetCovid19Cases(covid19DataSource)
        val testObserver = TestObserver<GetCovid19Cases.CasesByCountry>()
        whenever(covid19DataSource.getData()).doReturn(
            Observable.just(
                Covid19DataSource.DataRow("Mexico", "3/25/20", "200"),
                Covid19DataSource.DataRow("Mexico", "3/25/20", "205"),
                Covid19DataSource.DataRow("Mexico", "3/24/20", "367"),
                Covid19DataSource.DataRow("Mexico", "3/23/20", "316"),

                Covid19DataSource.DataRow("United Kingdom", "3/23/20", "6650"),
                Covid19DataSource.DataRow("United Kingdom", "3/24/20", "8077"),
                Covid19DataSource.DataRow("United Kingdom", "3/25/20", "9529")
            )
        )

        covid19Repository.invoke().subscribe(testObserver)

        testObserver.assertValue(
            GetCovid19Cases.CasesByCountry(
                mapOf(
                    "Mexico" to listOf(
                        GetCovid19Cases.DateNumber(1584921600, 316),
                        GetCovid19Cases.DateNumber(1585008000, 367),
                        GetCovid19Cases.DateNumber(1585094400, 405)
                    ),
                    "United Kingdom" to listOf(
                        GetCovid19Cases.DateNumber(1584921600, 6650),
                        GetCovid19Cases.DateNumber(1585008000, 8077),
                        GetCovid19Cases.DateNumber(1585094400, 9529)
                    )
                )
            )
        )
    }
}
