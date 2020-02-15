package com.seiko.tv.di

import com.seiko.tv.vm.*
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val viewModelModule = module {
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { BangumiDetailViewModel(get(), get(), get(), get()) }
    viewModel { SearchBangumiViewModel(get(), get(), get()) }
    viewModel { SearchMagnetViewModel(get(), get()) }
    viewModel { BangumiAreaViewModel(get(), get()) }
}