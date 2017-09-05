package com.pain.dev14.deezerwithpain

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.view.View
import android.widget.RemoteViews
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util


/**
 * Created by dev14 on 23.08.17.
 */
class PlayerService : Service() {

    var STOPFOREGROUND_ACTION = "action.stopforeground"
    var MAIN_ACTION = "action.main"
    var PREV_ACTION = "action.prev"
    var PLAY_ACTION = "action.play"
    var NEXT_ACTION = "action.next"

    lateinit var status: Notification

    val player: SimpleExoPlayer by lazy {
        ExoPlayerFactory.newSimpleInstance(
                DefaultRenderersFactory(this),
                DefaultTrackSelector(), DefaultLoadControl()
        )
    }
    var previewUrl: String = ""
    lateinit var tracks: Tracks
    var currentPosition: Int = 0
    lateinit var currentTrack: Data

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action != null) {
            if (intent.action.equals(PREV_ACTION)) {
                playBack()
            } else if (intent.action.equals(PLAY_ACTION)) {
                if (player.playWhenReady) {
                    player.playWhenReady = false
                    loadBitmap(currentTrack.cover_small, object : Callback {
                        override fun bitmapReady(bitmap: Bitmap) {
                            showNotification(currentTrack, false, bitmap)
                        }
                    })
                } else {
                    player.playWhenReady = true
                    loadBitmap(currentTrack.cover_small, object : Callback {
                        override fun bitmapReady(bitmap: Bitmap) {
                            showNotification(currentTrack, true, bitmap)
                        }
                    })
                }
            } else if (intent.action.equals(NEXT_ACTION)) {
                playNext()
            }
        }
        return START_STICKY;
    }

    fun onClick(previewUrl: String, tracks: Tracks) {
        player.playWhenReady = false
        this.previewUrl = previewUrl
        this.tracks = tracks
        findPosition()
        setPlayer()
    }

    fun findPosition() {
        tracks.let { it ->
            it.data.forEach {
                if (it.preview == previewUrl) {
                    currentPosition = tracks.data.indexOf(it)
                }
            }
        }
    }

    fun playNext() {
        if (currentPosition < tracks.data.size - 1) {
            currentPosition = currentPosition + 1
        }
        setPlayer()
    }

    fun playBack() {
        if (currentPosition > 0) {
            currentPosition = currentPosition - 1
        }
        setPlayer()
    }

    fun setPlayer() {
        currentTrack = tracks.data.get(currentPosition)
        loadBitmap(currentTrack.cover_small, object : Callback {
            override fun bitmapReady(bitmap: Bitmap) {
                showNotification(currentTrack, true, bitmap)
                val extractorsFactory = DefaultExtractorsFactory()
                val source = ExtractorMediaSource(Uri.parse(currentTrack.preview),
                        DefaultDataSourceFactory(applicationContext, Util.getUserAgent(applicationContext, getString(R.string.app_name))),
                        extractorsFactory, null, null)

                player.prepare(source)
                player.addListener(object : Player.EventListener {
                    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
                    }

                    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
                    }

                    override fun onPlayerError(error: ExoPlaybackException?) {
                    }

                    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) {
                            currentPosition = currentPosition + 1
                            setPlayer()
                        }
                    }

                    override fun onLoadingChanged(isLoading: Boolean) {
                    }

                    override fun onPositionDiscontinuity() {
                    }

                    override fun onRepeatModeChanged(repeatMode: Int) {
                    }

                    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {
                    }

                })
                player.playWhenReady = true
            }
        })
    }

    fun loadBitmap(imageUrl: String, callback: Callback) {
        Glide.with(applicationContext)
                .load(imageUrl)
                .asBitmap()
                .into(object : SimpleTarget<Bitmap>(300, 300) {
                    override fun onResourceReady(resource: Bitmap, glideAnimation: GlideAnimation<in Bitmap>?) {
                        callback.bitmapReady(resource)
                    }
                })
    }

    fun showNotification(data: Data, isPlaying: Boolean, bitmap: Bitmap) {
        val views = RemoteViews(packageName,
                R.layout.status_bar)
        val bigViews = RemoteViews(packageName,
                R.layout.status_bar_expanded)

// showing default album image
        views.setViewVisibility(R.id.player_view, View.VISIBLE)
        views.setViewVisibility(R.id.status_bar_album_art, View.VISIBLE)
        bigViews.setImageViewBitmap(R.id.status_bar_album_art, BitmapFactory.decodeResource(this
                .getResources(), R.drawable.images, BitmapFactory.Options()))

        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.action = MAIN_ACTION
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val previousIntent = Intent(this, PlayerService::class.java)
        previousIntent.action = PREV_ACTION
        val ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0)

        val playIntent = Intent(this, PlayerService::class.java)
        playIntent.action = PLAY_ACTION
        val pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0)

        val nextIntent = Intent(this, PlayerService::class.java)
        nextIntent.action = NEXT_ACTION
        val pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0)

        val closeIntent = Intent(this, PlayerService::class.java)
        closeIntent.action = STOPFOREGROUND_ACTION
        val pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0)

        views.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent)
        bigViews.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent)

        views.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent)
        bigViews.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent)

        views.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent)
        bigViews.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent)

        bigViews.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent)

        if (isPlaying) {
            views.setImageViewResource(R.id.status_bar_play, R.drawable.exo_controls_pause)
            bigViews.setImageViewResource(R.id.status_bar_play, R.drawable.exo_controls_play)
        } else {
            views.setImageViewResource(R.id.status_bar_play, R.drawable.exo_controls_pause)
            bigViews.setImageViewResource(R.id.status_bar_play, R.drawable.exo_controls_play)
        }

        views.setTextViewText(R.id.status_bar_track_name, data.title)
        bigViews.setTextViewText(R.id.status_bar_track_name, data.title)

        views.setTextViewText(R.id.status_bar_artist_name, data.artist.name)
        bigViews.setTextViewText(R.id.status_bar_artist_name, data.artist.name)

        bigViews.setTextViewText(R.id.status_bar_album_name, "Album Name")

        views.setImageViewBitmap(R.id.status_bar_album_art, bitmap)
        bigViews.setImageViewBitmap(R.id.status_bar_album_art, bitmap)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            status = Notification.Builder(this)
                    .setCustomContentView(views)
                    .setCustomBigContentView(bigViews)
                    .setSmallIcon(R.drawable.images)
                    .build()
        } else {
            status = Notification.Builder(this).build()
            status.contentView = views
            status.bigContentView = bigViews
            status.icon = R.drawable.images
        }

        status.flags = Notification.FLAG_ONGOING_EVENT
        status.contentIntent = pendingIntent
        startForeground(101, status)
    }

    override fun onBind(p0: Intent?): IBinder {
        return PlayerBinder()
    }

    override fun onUnbind(intent: Intent): Boolean {
        return false
    }

    inner class PlayerBinder : Binder() {
        fun getService(): PlayerService = this@PlayerService
    }

}

interface Callback {
    fun bitmapReady(bitmap: Bitmap)
}