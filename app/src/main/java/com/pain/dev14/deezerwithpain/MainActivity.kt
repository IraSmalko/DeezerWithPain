package com.pain.dev14.deezerwithpain

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.github.nitrico.lastadapter.LastAdapter
import com.github.nitrico.lastadapter.Type
import com.pain.dev14.deezerwithpain.databinding.RvItemBinding
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus


class MainActivity : AppCompatActivity() {

    val PLAYER_FRAGMENT_TAG = PlayerFragment::class.java.simpleName
    var listData = mutableListOf<Data>()
    lateinit var lastAdapter: LastAdapter
    lateinit var playerFragment: PlayerFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerFragment = PlayerFragment().newInstance()
        supportFragmentManager.beginTransaction()
                .add(R.id.player_container, playerFragment, PLAYER_FRAGMENT_TAG)
                .commit()

        Repository().getAlbum()
                .subscribe({ r -> displayResult(r) },
                        { error -> Log.e("TAG", "{$error.message}") })

        lastAdapter = LastAdapter(listData, BR.item)
                .type { _, _ ->
                    Type<RvItemBinding>(R.layout.rv_item)
                            .onClick {
                                EventBus.getDefault().post(EBEvent(it.binding.item!!.preview, Tracks(listData)))
                            }
                }
                .into(rv)
    }

    private fun displayResult(result: List<Data>) {
        listData.addAll(result)
        lastAdapter.notifyDataSetChanged()
    }
}

interface OnClickListener {
    fun onClick(previewUrl: String, tracks: Tracks)
}
