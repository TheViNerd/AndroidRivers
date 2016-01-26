package com.silverkeytech.android_rivers.meta_weblog

import java.util.*

//ref http://codex.wordpress.org/XML-RPC_MetaWeblog_API#metaWeblog.newPost

class PostPayload {
    var title: String? = null
    var description: String? = null
    var post_type: String? = "post"
    var categories: List<String>? = null
    var mt_keywords: List<String>? = null
    var mt_excerpt: String? = null
    var mt_text_more: String? = null
    var mt_allow_comments: Allow? = null
    var mt_allow_pings: Allow? = null
    var wp_slug: String? = null
    var post_status: String? = "publish"
    var wp_post_format: PostFormat? = null
    var sticky: Boolean? = null
    var publish: Boolean? = null

    fun toMap(): HashMap<String, Any> {
        val map = HashMap<String, Any>()
        if (!title.isNullOrBlank())
            map.put("title", title!!)
        if (!description.isNullOrBlank())
            map.put("description", description!!)
        if (!post_type.isNullOrBlank())
            map.put("post_type", post_type!!)

        if (categories != null && categories!!.size > 0)
            map.put("categories", categories!!)
        if (mt_keywords != null && mt_keywords!!.size > 0)
            map.put("mt_keywords", mt_keywords!!)
        if (!mt_excerpt.isNullOrBlank())
            map.put("mt_excerpt", mt_excerpt!!)
        if (!mt_text_more.isNullOrBlank())
            map.put("mt_text_more", mt_text_more!!)
        if (mt_allow_comments != null)
            map.put("mt_allow_comments", mt_allow_comments!!.toString().toLowerCase())
        if (mt_allow_pings != null)
            map.put("mt_allow_pings", mt_allow_pings!!.toString().toLowerCase())

        if (!post_status.isNullOrBlank())
            map.put("post_status", post_status!!)
        if (wp_post_format != null)
            map.put("wp_post_format", wp_post_format!!.toString().toLowerCase())
        if (sticky != null)
            map.put("sticky", sticky!!)

        return map
    }
}

fun simplePost(title: String, description: String): PostPayload {
    val payload = PostPayload()
    payload.title = title
    payload.description = description
    return payload
}

fun linkPost(description: String, link: String): PostPayload {
    val payload = PostPayload()
    payload.description = "<a href=\"$link\">$description</a>"
    payload.wp_post_format = PostFormat.LINK
    return payload
}

fun statusPost(description: String): PostPayload {
    val payload = PostPayload()
    payload.description = description
    payload.wp_post_format = PostFormat.STATUS
    return payload
}

fun imageLinkPost(title: String, link: String): PostPayload {
    val payload = PostPayload()
    payload.title = title
    payload.description = link
    payload.wp_post_format = PostFormat.IMAGE
    return payload
}
