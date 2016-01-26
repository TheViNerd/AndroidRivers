/*
Android Rivers is an app to read and discover news using RiverJs, RSS and OPML format.
Copyright (C) 2012 Dody Gunawinata (dodyg@silverkeytech.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package com.silverkeytech.android_rivers.activities

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import com.silverkeytech.android_rivers.*
import com.silverkeytech.android_rivers.db.Bookmark
import com.silverkeytech.android_rivers.db.removeBookmarkFromCollection

class BookmarkCollectionRenderer(val context: BookmarkCollectionActivity){
    companion object {
        val TAG: String = BookmarkCollectionRenderer::class.java.simpleName
    }

    fun handleListing(bookmarks: List<Bookmark>) {
        val textSize = context.getVisualPref().listTextSize

        if (bookmarks.count() == 0){
            var msg = context.findView<TextView>(R.id.collection_message_tv)
            handleFontResize(msg, context.getString(R.string.empty_bookmark_collection_items_list), textSize.toFloat())
        }

        val adapter = object : ArrayAdapter<Bookmark>(context, android.R.layout.simple_list_item_1, android.R.id.text1, bookmarks){
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val text = bookmarks[position].toString()
                return currentListItem(text, convertView, parent, textSize.toFloat())
            }
        }

        val list = context.findView<ListView>(android.R.id.list)
        list.adapter = adapter
        list.onItemClickListener = object : OnItemClickListener{
            override fun onItemClick(p0: AdapterView<out Adapter?>, p1: View, p2: Int, p3: Long) {
                val bookmark = bookmarks.get(p2)
                Log.d(TAG, "Downloading feed ${bookmark.title} - ${bookmark.url}")
                startFeedActivity(context, bookmark.url, bookmark.title, bookmark.language)
            }
        }

        list.onItemLongClickListener = AdapterView.OnItemLongClickListener { p0, p1, p2, p3 ->
            val currentBookmark = bookmarks.get(p2)
            showCollectionQuickActionPopup(context, currentBookmark, p1!!, list)
            true
        }
    }

    data class ViewHolder (var name: TextView)

    fun currentListItem(text: String, convertView: View?, parent: ViewGroup?, textSize: Float): View {
        var holder: ViewHolder?

        var vw: View? = convertView

        if (vw == null){
            vw = inflater().inflate(android.R.layout.simple_list_item_1, parent, false)

            holder = ViewHolder(vw.findView<TextView>(android.R.id.text1))
            vw!!.tag = holder
        }else{
            holder = vw.tag as ViewHolder
        }

        handleFontResize(holder.name, text, textSize)
        return vw
    }

    fun inflater(): LayoutInflater {
        val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return inflater
    }
}

fun showCollectionQuickActionPopup(context: BookmarkCollectionActivity, bookmark: Bookmark, item: View, list: View) {
    //overlay popup at top of clicked overview position
    val popupWidth = item.width
    val popupHeight = item.height

    val x = context.layoutInflater.inflate(R.layout.collection_quick_actions, null, false)!!
    val pp = PopupWindow(x, popupWidth, popupHeight, true)

    x.setBackgroundColor(android.graphics.Color.LTGRAY)

    x.setOnClickListener {
        pp.dismiss()
    }

    val icon = x.findView<ImageView>(R.id.collection_quick_action_delete_icon)
    icon.setOnClickListener {
        val dlg = createConfirmationDialog(context = context, message = "Are you sure about removing this collection bookmark?", positive = {
            try{
                var res = removeBookmarkFromCollection(bookmark.collection!!.id, bookmark.id)

                if (res.isFalse())
                    context.toastee("Error in removing this collection bookmark  ${res.exception?.message}")
                else {
                    context.toastee(context.getString(R.string.bookmark_removed))
                    context.refreshCollection()
                }
            }
            catch(e: Exception){
                context.toastee("Error in trying to remove this collection bookmark ${e.message}")
            }
            pp.dismiss()
        }, negative = {
            pp.dismiss()
        })

        dlg.show()
    }

    val itemLocation = getLocationOnScreen(item)
    pp.showAtLocation(list, Gravity.TOP or Gravity.LEFT, itemLocation.x, itemLocation.y)
}
