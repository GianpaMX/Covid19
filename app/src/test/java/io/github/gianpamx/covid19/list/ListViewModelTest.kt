package io.github.gianpamx.covid19.list

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import io.github.gianpamx.covid19.AppSchedulers
import io.github.gianpamx.covid19.usecases.GetCovid19Cases
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

private const val DAY_0 = 0
private const val DAY_1 = 86400

@RunWith(MockitoJUnitRunner::class)
class ListViewModelTest {
    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Mock
    lateinit var getCovid19Cases: GetCovid19Cases

    private val dummyListOfCases = listOf(
        GetCovid19Cases.DateNumber(DAY_0, 1),
        GetCovid19Cases.DateNumber(DAY_1, 2)
    )

    private lateinit var viewModel: ListViewModel

    @Test
    fun `Initial value`() {
        whenever(getCovid19Cases.invoke()).doReturn(Single.never())

        viewModel = ListViewModel(getCovid19Cases, testSchedulers)

        assertThat(viewModel.viewState.value).isInstanceOf(ViewState.Loading::class.java)
    }

    @Test
    fun `Empty response`() {
        whenever(getCovid19Cases.invoke()).doReturn(
            Single.just(GetCovid19Cases.CasesByCountry(emptyMap()))
        )

        viewModel = ListViewModel(getCovid19Cases, testSchedulers)

        assertThat(viewModel.viewState.value).isInstanceOf(ViewState.Ready::class.java)
    }

    @Test
    fun `Latest List of Cases`() {
        whenever(getCovid19Cases.invoke()).doReturn(
            Single.just(GetCovid19Cases.CasesByCountry(mapOf(Pair("any", dummyListOfCases))))
        )

        viewModel = ListViewModel(getCovid19Cases, testSchedulers)

        assertThat((viewModel.viewState.value as ViewState.Ready).list).isEqualTo(
            listOf(
                CountryNumber("any", 2)
            )
        )
    }

    @Test
    fun `Country with no cases`() {
        whenever(getCovid19Cases.invoke()).doReturn(
            Single.just(
                GetCovid19Cases.CasesByCountry(mapOf(Pair("any", emptyList())))
            )
        )

        viewModel = ListViewModel(getCovid19Cases, testSchedulers)

        assertThat((viewModel.viewState.value as ViewState.Ready).list).isEqualTo(
            listOf(
                CountryNumber("any", 0)
            )
        )
    }

    @Test
    fun `First Date in the list of Cases`() {
        whenever(getCovid19Cases.invoke()).doReturn(
            Single.just(
                GetCovid19Cases.CasesByCountry(mapOf(Pair("any", dummyListOfCases)))
            )
        )
        viewModel = ListViewModel(getCovid19Cases, testSchedulers)

        viewModel.selectDate(DAY_0)

        assertThat((viewModel.viewState.value as ViewState.Ready).list).isEqualTo(
            listOf(
                CountryNumber("any", 1)
            )
        )
    }

    @Test
    fun `Empty list of Cases after selecting a date`() {
        whenever(getCovid19Cases.invoke()).doReturn(
            Single.just(GetCovid19Cases.CasesByCountry(emptyMap()))
        )
        viewModel = ListViewModel(getCovid19Cases, testSchedulers)

        viewModel.selectDate(DAY_0)

        assertThat((viewModel.viewState.value as ViewState.Ready)).isEqualTo(
            ViewState.Ready(emptyList(), 0, 0, 0)
        )
    }

    @Test
    fun `Country with no cases after selecting a date`() {
        whenever(getCovid19Cases.invoke()).doReturn(
            Single.just(GetCovid19Cases.CasesByCountry(mapOf("any" to emptyList())))
        )
        viewModel = ListViewModel(getCovid19Cases, testSchedulers)

        viewModel.selectDate(DAY_0)

        assertThat((viewModel.viewState.value as ViewState.Ready).list).isEqualTo(
            listOf(
                CountryNumber("any", 0)
            )
        )
    }

    @Test
    fun `Error in response`() {
        whenever(getCovid19Cases.invoke()).doReturn(Single.error(RuntimeException("Expected Error")))

        viewModel = ListViewModel(getCovid19Cases, testSchedulers)

        assertThat(viewModel.viewState.value).isInstanceOf(ViewState.Error::class.java)
    }

    private val testSchedulers = object : AppSchedulers {
        override fun io() = Schedulers.trampoline()

        override fun mainThread() = Schedulers.trampoline()
    }

}
