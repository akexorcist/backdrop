package com.akexorcist.screensharing.di

import com.akexorcist.screensharing.data.AudioRepository
import com.akexorcist.screensharing.data.DefaultAudioRepository
import com.akexorcist.screensharing.data.DefaultVideoRepository
import com.akexorcist.screensharing.data.VideoRepository
import com.akexorcist.screensharing.ui.main.MainViewModel
import org.koin.dsl.module

object AppModule {
    val modules = module {
        single<VideoRepository> { DefaultVideoRepository() }
        single<AudioRepository> { DefaultAudioRepository() }
        factory { MainViewModel(get(), get()) }
    }
}
