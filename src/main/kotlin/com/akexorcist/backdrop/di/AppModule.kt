package com.akexorcist.backdrop.di

import com.akexorcist.backdrop.data.*
import com.akexorcist.backdrop.ui.main.MainViewModel
import org.koin.dsl.module

object AppModule {
    val modules = module {
        factory { RawFrameRateCounter() }
        single<VideoRepository> { DefaultVideoRepository(get()) }
        single<AudioRepository> { DefaultAudioRepository() }
        single { MainViewModel(get(), get()) }
    }
}
