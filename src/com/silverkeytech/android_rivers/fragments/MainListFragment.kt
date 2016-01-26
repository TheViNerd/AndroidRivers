package com.silverkeytech.android_rivers.fragments

import android.os.Bundle
import android.util.Log
import com.silverkeytech.android_rivers.Bus
import com.silverkeytech.android_rivers.MessageEvent
import org.holoeverywhere.app.Activity
import org.holoeverywhere.app.ListFragment

abstract class MainListFragment : ListFragment () {
    companion object {
        open val TAG: String = MainListFragment::class.java.simpleName
    }

    protected var parent: Activity by kotlin.properties.Delegates.notNull()

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        parent = activity!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bus.register(this)
    }

    open fun onEvent(msg : MessageEvent){
        Log.d(TAG, "Event Bus ${msg.message}")
    }

    override fun onDestroy(){
        Bus.unregister(this)
        super.onDestroy()
    }
}
