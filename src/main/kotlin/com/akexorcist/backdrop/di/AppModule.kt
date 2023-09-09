package com.akexorcist.backdrop.di

import com.akexorcist.backdrop.data.AudioRepository
import com.akexorcist.backdrop.data.DefaultAudioRepository
import com.akexorcist.backdrop.data.DefaultVideoRepository
import com.akexorcist.backdrop.data.VideoRepository
import com.akexorcist.backdrop.ui.main.MainViewModel
import org.koin.dsl.module

object AppModule {
    val modules = module {
        single<VideoRepository> { DefaultVideoRepository() }
        single<AudioRepository> { DefaultAudioRepository() }
        single { MainViewModel(get(), get()) }
    }
}
