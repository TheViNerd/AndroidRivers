package com.silverkeytech.android_rivers.meta_weblog

import android.util.Log
import org.xmlrpc.android.XMLRPCClient

//reference implementation
//http://codex.wordpress.org/XML-RPC_MetaWeblog_API
class Blog(val blogId: Int?, val server: String, val username: String, val password: String){
    companion object {
        val TAG: String = Blog::class.java.simpleName
    }

    fun newPost(payload: PostPayload) {
        val rpc = XMLRPCClient(server, "", "")

        val res = rpc.call("metaWeblog.newPost", "blogid", username, password, payload.toMap(), true)
        Log.d(TAG, "Return content $res")

    }

    fun editPost(postId: Int) {

    }

    fun getPost(postId: Int) {

    }

    fun newMediaObject() {

    }

    fun getCategories() {

    }

    fun getRecentPosts() {

    }
}