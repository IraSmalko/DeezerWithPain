package com.pain.dev14.deezerwithpain


import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.player.*


/**
 * Created by dev14 on 15.08.17.
 */
class PlayerFragment : Fragment() {

    lateinit var player: SimpleExoPlayer
    var previewUrl: String = ""
    var tracks: Tracks? = null

    fun newInstance(): PlayerFragment {
        return PlayerFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.player, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setPlayer()
    }

    private fun setPlayer() {
        val extractorsFactory = DefaultExtractorsFactory()
        val firstSource = ExtractorMediaSource(Uri.parse(previewUrl),
                DefaultDataSourceFactory(activity, Util.getUserAgent(activity, getString(R.string.app_name))),
                extractorsFactory, null, null)

        var sourceList = Array(1, { firstSource })

        tracks?.let {
            sourceList = Array(it.data.size, { i ->
                ExtractorMediaSource(Uri.parse(it.data.get(i).preview),
                        DefaultDataSourceFactory(activity, Util.getUserAgent(activity, getString(R.string.app_name))),
                        extractorsFactory, null, null)
            })
        }

        player = ExoPlayerFactory.newSimpleInstance(
                DefaultRenderersFactory(activity),
                DefaultTrackSelector(), DefaultLoadControl())
        player_view.player = player
        player.prepare(ConcatenatingMediaSource(*sourceList))
        player.playWhenReady = true

    }

//    override fun onAttach(context: Context?) {
//        super.onAttach(context)
//        if (!EventBus.getDefault().isRegistered(this)) {
//            EventBus.getDefault().register(this);
//        }
//    }

//    override fun onDestroy() {
//        super.onDestroy()
//        player.playWhenReady = false
//        EventBus.getDefault().unregister(this)
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onEvent(ebEvent: EBEvent) {
//        player.playWhenReady = false
//        this.previewUrl = ebEvent.preview
//        this.tracks = ebEvent.tracks
//        setPlayer()
//    }
}