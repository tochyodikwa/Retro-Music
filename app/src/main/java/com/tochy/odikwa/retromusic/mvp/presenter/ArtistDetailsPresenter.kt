/*
 * Copyright (c) 2019 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.tochy.odikwa.retromusic.mvp.presenter

import com.tochy.odikwa.retromusic.Result.Error
import com.tochy.odikwa.retromusic.Result.Success
import com.tochy.odikwa.retromusic.model.Artist
import com.tochy.odikwa.retromusic.mvp.BaseView
import com.tochy.odikwa.retromusic.mvp.Presenter
import com.tochy.odikwa.retromusic.mvp.PresenterImpl
import com.tochy.odikwa.retromusic.providers.interfaces.Repository
import com.tochy.odikwa.retromusic.rest.model.LastFmArtist
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 * Created by hemanths on 20/08/17.
 */
interface ArtistDetailsView : BaseView {

    fun artist(artist: Artist)

    fun artistInfo(lastFmArtist: LastFmArtist?)

    fun complete()
}

interface ArtistDetailsPresenter : Presenter<ArtistDetailsView> {

    fun loadArtist(artistId: Int)

    fun loadBiography(
        name: String,
        lang: String? = Locale.getDefault().language,
        cache: String?
    )

    class ArtistDetailsPresenterImpl @Inject constructor(
        private val repository: Repository
    ) : PresenterImpl<ArtistDetailsView>(), ArtistDetailsPresenter, CoroutineScope {

        override val coroutineContext: CoroutineContext
            get() = Dispatchers.IO + job

        private val job = Job()

        override fun loadBiography(name: String, lang: String?, cache: String?) {
            launch {
                when (val result = repository.artistInfo(name, lang, cache)) {
                    is Success -> withContext(Dispatchers.Main) { view?.artistInfo(result.data) }
                    is Error -> withContext(Dispatchers.Main) {}
                }
            }
        }

        override fun loadArtist(artistId: Int) {
            launch {
                when (val result = repository.artistById(artistId)) {
                    is Success -> withContext(Dispatchers.Main) { view?.artist(result.data) }
                    is Error -> withContext(Dispatchers.Main) { view?.showEmptyView() }
                }
            }
        }

        override fun detachView() {
            super.detachView()
            job.cancel()
        }
    }
}