package io.github.gianpamx.covid19.usecases

import io.reactivex.Observable

interface Covid19DataSource {
    data class DataRow(val country: String, val date: String, val cases: String)

    fun getData(): Observable<DataRow>
}
