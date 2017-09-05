package com.pain.dev14.deezerwithpain

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.MediaController
import com.github.nitrico.lastadapter.LastAdapter
import com.github.nitrico.lastadapter.Type
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.pain.dev14.deezerwithpain.databinding.RvItemBinding
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    val PLAYER_FRAGMENT_TAG = PlayerFragment::class.java.simpleName
    var listData = mutableListOf<Data>()
    lateinit var lastAdapter: LastAdapter
    //    lateinit var playerFragment: PlayerFragment
    // TODO: Use service methods directly
    lateinit var service: PlayerService
    var bound = false

    lateinit var player: SimpleExoPlayer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val intent = Intent(this, PlayerService::class.java)
        startService(intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)

//        playerFragment = PlayerFragment().newInstance()
//        supportFragmentManager.beginTransaction()
//                .add(R.id.player_container, playerFragment, PLAYER_FRAGMENT_TAG)
//                .commit()
//        player = ExoPlayerFactory.newSimpleInstance(
//                DefaultRenderersFactory(this),
//                DefaultTrackSelector(), DefaultLoadControl())
//        player_view.player = player

        Repository().getAlbum()
                .subscribe({ r -> displayResult(r.tracks.data, r.cover_small) },
                        { error -> Log.e("TAG", "{$error.message}") })

        lastAdapter = LastAdapter(listData, BR.item)
                .type { _, _ ->
                    Type<RvItemBinding>(R.layout.rv_item)
                            .onClick {
                                service.onClick(it.binding.item!!.preview, Tracks(listData))
                                //                                EventBus.getDefault().post(EBEvent(it.binding.item!!.preview, Tracks(listData)))
                            }
                }
                .into(rv)
    }

    fun bounded(p: SimpleExoPlayer) {
        player_view.player = p
    }

    override fun onStop() {
        super.onStop()
        if (bound) {
            unbindService(connection)
            bound = false
        }
    }


    private fun displayResult(result: List<Data>, coverSmall: String) {
        result.forEach {it.cover_small = coverSmall}
        listData.addAll(result)
        lastAdapter.notifyDataSetChanged()
    }


    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, ibBinder: IBinder) {
            val binder = ibBinder as PlayerService.PlayerBinder
            service = binder.getService()
            bounded(service.player)
            bound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bound = false
        }
    }

}