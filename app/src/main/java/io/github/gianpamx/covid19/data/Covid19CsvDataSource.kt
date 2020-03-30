package io.github.gianpamx.covid19.data

import au.com.bytecode.opencsv.CSVReader
import io.github.gianpamx.covid19.usecases.Covid19DataSource
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class Covid19CsvDataSource(private val okHttpClient: OkHttpClient, private val csvUrl: String) :
    Covid19DataSource {
    override fun getData() = Observable.create<Covid19DataSource.DataRow> {
        val request = Request.Builder()
            .url(csvUrl)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val reader = CSVReader(response.body!!.byteStream().reader())

            val headers = reader.readNext()
            val country = headers.indexOf("Country/Region")
            val dates = mutableListOf<Pair<Int, String>>()

            headers.forEachIndexed { index, header ->
                if (header.split('/').size == 3) {
                    dates.add(Pair(index, header))
                }
            }

            var nextLine: Array<String>?
            while (reader.readNext().also { nextLine = it } != null) {
                dates.forEach { dateColumn ->
                    it.onNext(
                        Covid19DataSource.DataRow(
                            nextLine!![country],
                            dateColumn.second,
                            nextLine!![dateColumn.first]
                        )
                    )
                }
            }

            reader.close()

            it.onComplete()
        }
    }
}
