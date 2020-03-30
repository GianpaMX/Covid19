package io.github.gianpamx.covid19

import io.reactivex.Scheduler

interface AppSchedulers {
    fun io(): Scheduler
    fun mainThread(): Scheduler
}
