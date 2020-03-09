package com.seiko.torrent.di

import com.seiko.torrent.util.constants.TORRENT_DOWNLOAD_DIR
import com.seiko.torrent.vm.AddTorrentViewModel
import com.seiko.torrent.vm.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal val viewModelModule = module {

    viewModel { MainViewModel(get(), get()) }

    viewModel {
        AddTorrentViewModel(get(), get(named(TORRENT_DOWNLOAD_DIR)),
            get(), get(), get(), get())
    }

}