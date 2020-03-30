package io.github.gianpamx.covid19.list

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import io.github.gianpamx.covid19.AppSchedulers
import io.github.gianpamx.covid19.R
import io.github.gianpamx.covid19.data.Covid19CsvDataSource
import io.github.gianpamx.covid19.usecases.GetCovid19Cases
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_list.*
import okhttp3.OkHttpClient
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ListFragment : Fragment(R.layout.fragment_list) {
    private val viewModel: ListViewModel by viewModels(factoryProducer = ::factoryProducer)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.selectDate(progress)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) = Unit
            override fun onStopTrackingTouch(p0: SeekBar?) = Unit
        })

        val numberFormat = NumberFormat.getNumberInstance()
        val adapter = CountryNumberAdapter(numberFormat)
        recyclerView.adapter = adapter

        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ViewState.Loading -> showLoading()

                is ViewState.Ready -> {
                    hideLoading()
                    displayList(adapter, it)
                    displaySeekBar(it)
                    displayDate(it)
                }

                is ViewState.Error -> Snackbar
                    .make(view, getString(R.string.error), Snackbar.LENGTH_INDEFINITE)
                    .show()
            }
        })
    }

    private fun displayList(adapter: CountryNumberAdapter, viewState: ViewState.Ready) {
        if (recyclerView.isAnimating) {
            Handler().post {
                adapter.submitList(viewState.list)
            }
        } else {
            adapter.submitList(viewState.list)
        }
    }

    private fun displaySeekBar(viewState: ViewState.Ready) {
        seekBar.max = viewState.max
        seekBar.progress = viewState.selected
    }

    private fun displayDate(viewState: ViewState.Ready) {
        val appCompatActivity = activity as? AppCompatActivity
        appCompatActivity?.supportActionBar?.subtitle = viewState.date.toFormatString()
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.INVISIBLE
        seekBar.visibility = View.INVISIBLE
    }

    private fun hideLoading() {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        seekBar.visibility = View.VISIBLE
    }

    private fun Int.toFormatString() = SimpleDateFormat.getDateInstance().apply {
        timeZone = Calendar.getInstance().timeZone
    }.run {
        format(Date(this@toFormatString * 1_000L))
    }

    @Suppress("UNCHECKED_CAST")
    private fun factoryProducer() = object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val csvUrl =
                "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv"
            val okHttpClient = OkHttpClient()
            val covid19DataSource = Covid19CsvDataSource(okHttpClient, csvUrl)
            val getCovid19Cases = GetCovid19Cases(covid19DataSource)
            val appSchedulers = object : AppSchedulers {
                override fun io() = Schedulers.io()
                override fun mainThread() = AndroidSchedulers.mainThread()
            }
            return ListViewModel(getCovid19Cases, appSchedulers) as T
        }
    }
}
