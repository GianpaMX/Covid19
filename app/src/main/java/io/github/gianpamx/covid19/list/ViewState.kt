package io.github.gianpamx.covid19.list

data class CountryNumber(val country: String, val number: Int)

sealed class ViewState {
    object Loading : ViewState()

    data class Ready(
        val list: List<CountryNumber>,
        val max: Int,
        val selected: Int,
        val date: Int
    ) : ViewState()

    data class Error(val throwable: Throwable) : ViewState()
}
