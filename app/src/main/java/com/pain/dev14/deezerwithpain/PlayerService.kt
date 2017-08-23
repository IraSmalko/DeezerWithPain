package com.pain.dev14.deezerwithpain

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.Allocator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * Created by dev14 on 23.08.17.
 */
class PlayerService : Service(), LoadControl {

    lateinit var player: SimpleExoPlayer
    var previewUrl: String = ""
    var tracks: Tracks? = null
    val NOTIFY_ID: Int = 1

    override fun onCreate() {
        super.onCreate()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        val backgroundThread = Thread(Runnable { setPlayer() })
        setPlayer()
    }

    fun setPlayer() {
        val extractorsFactory = DefaultExtractorsFactory()
        val firstSource = ExtractorMediaSource(Uri.parse(previewUrl),
                DefaultDataSourceFactory(applicationContext, Util.getUserAgent(applicationContext, getString(R.string.app_name))),
                extractorsFactory, null, null)

        var sourceList = Array(1, { firstSource })

        tracks?.let {
            sourceList = Array(it.data.size, { i ->
                ExtractorMediaSource(Uri.parse(it.data.get(i).preview),
                        DefaultDataSourceFactory(applicationContext, Util.getUserAgent(applicationContext, getString(R.string.app_name))),
                        extractorsFactory, null, null)
            })
        }

        player = ExoPlayerFactory.newSimpleInstance(
                DefaultRenderersFactory(applicationContext),
                DefaultTrackSelector(), DefaultLoadControl())
        player_view.player = player
        player.prepare(ConcatenatingMediaSource(*sourceList))
        player.playWhenReady = true

    }

    override fun onBind(p0: Intent?): IBinder = PlayerBinder()

    override fun onUnbind(intent: Intent): Boolean {
        player.playWhenReady = false
        EventBus.getDefault().unregister(this)
        return false
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(ebEvent: EBEvent) {
        player.playWhenReady = false
        this.previewUrl = ebEvent.preview
        this.tracks = ebEvent.tracks
        setPlayer()
    }

    override fun onPrepared() {
        setPlayer()
        val notIntent = Intent(this, MainActivity::class.java)
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = Notification.Builder(this)

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setTicker("")
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText("")
        val not = builder.build()
        startForeground(NOTIFY_ID, not)
    }

    override fun onTracksSelected(renderers: Array<out Renderer>?, trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun shouldContinueLoading(bufferedDurationUs: Long): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onReleased() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAllocator(): Allocator {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun shouldStartPlayback(bufferedDurationUs: Long, rebuffering: Boolean): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStopped() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class PlayerBinder : Binder() {
    fun getService(): PlayerService = PlayerService()
}
