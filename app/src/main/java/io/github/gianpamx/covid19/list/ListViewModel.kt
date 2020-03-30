package io.github.gianpamx.covid19.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.gianpamx.covid19.AppSchedulers
import io.github.gianpamx.covid19.usecases.GetCovid19Cases
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

private const val LAST_DAY = -1

class ListViewModel(getCovid19Cases: GetCovid19Cases, appSchedulers: AppSchedulers) : ViewModel() {
    private val _viewState = MutableLiveData<ViewState>(ViewState.Loading)
    val viewState: LiveData<ViewState> get() = _viewState

    private val data: Observable<GetCovid19Cases.CasesByCountry> = getCovid19Cases()
        .toObservable()
        .replay(1)
        .autoConnect(1)
        .subscribeOn(appSchedulers.io())
        .observeOn(appSchedulers.mainThread())

    private val compositeDouble = CompositeDisposable()

    init {
        compositeDouble.add(data.subscribe({ response ->
            val max = response.cases.keys.firstOrNull()?.let {
                response.cases[it]?.size?.isGreaterThanZero()?.minus(1)
            } ?: 0

            _viewState.value = ViewState.Ready(
                list = response.cases.map { it.toListOfCases() }
                    .sortedByCasesAndCountryDescending(),
                max = max,
                selected = max,
                date = response.cases.keys.firstOrNull()
                    ?.let { response.cases[it]?.getOrNull(max)?.date }
                    ?: 0
            )
        }, {
            _viewState.value = ViewState.Error(it)
        }))
    }

    fun selectDate(selectedDate: Int) {
        compositeDouble.add(data.subscribe({ response ->
            _viewState.value = ViewState.Ready(
                list = response.cases.map { it.toListOfCases(selectedDate) }
                    .sortedByCasesAndCountryDescending(),
                max = response.cases.keys.firstOrNull()
                    ?.let { response.cases[it]?.size?.isGreaterThanZero()?.minus(1) }
                    ?: 0,
                selected = selectedDate,
                date = response.cases.keys.firstOrNull()
                    ?.let { response.cases[it]?.getOrNull(selectedDate)?.date }
                    ?: 0
            )
        }, {
            _viewState.value = ViewState.Error(it)
        }))
    }

    override fun onCleared() {
        compositeDouble.clear()
    }

    private fun Map.Entry<String, List<GetCovid19Cases.DateNumber>>.toListOfCases(selectedDay: Int = LAST_DAY) =
        CountryNumber(
            this.key,
            if (selectedDay == LAST_DAY) this.value.lastOrZero()
            else if (selectedDay < this.value.size) this.value[selectedDay].number
            else 0
        )

    private fun List<CountryNumber>.sortedByCasesAndCountryDescending() =
        this.sortedWith(Comparator { a, b -> if (b.number - a.number == 0) a.country.compareTo(b.country) else b.number - a.number })

    private fun Int?.isGreaterThanZero() = if (this != null && this > 0) this else null
    private fun List<GetCovid19Cases.DateNumber>.lastOrZero() = this.lastOrNull()?.number ?: 0
}
