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
package com.silverkeytech.android_rivers.asyncs

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException
import com.silverkeytech.android_rivers.*
import com.silverkeytech.android_rivers.activities.Duration
import com.silverkeytech.android_rivers.activities.getMain
import com.silverkeytech.android_rivers.activities.toastee
import com.silverkeytech.news_engine.outliner.OutlineContent
import com.silverkeytech.news_engine.outlines.Opml
import com.silverkeytech.news_engine.outlines.Outline
import com.silverkeytech.news_engine.transformXmlToOpml
import java.util.*

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE") class DownloadOpmlAsync(it: Context?): AsyncTask<String, Int, Pair<String, Result<Opml>>>(){
    companion object {
        val TAG: String = DownloadOpmlAsync::class.java.simpleName
    }

    var context: Activity = it!! as Activity
    var dialog: InfinityProgressDialog = InfinityProgressDialog(context, context.getString(R.string.please_wait_while_downloading_outlines))

    override fun onPreExecute() {
        dialog.onCancel {
            dlg ->
            dlg.dismiss()
            this@DownloadOpmlAsync.cancel(true)
        }
        dialog.show()
    }

    override fun doInBackground(vararg url: String?): Pair<String, Result<Opml>>? {
        var link = url[0]!!
        var req: String?
        try{
            req = httpGet(link).body()

            Log.d(TAG, "Source $link Raw OPML ${req?.length}")

            if (req!!.contains("<html>")){
                throw IllegalArgumentException("Document is not a valid OPML file (it might be a HTML document)")
            }

            val opml = transformXmlToOpml(req.replace("<?xml version=\"1.0\" encoding=\"utf-8\" ?>", "")
            ?.replace("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>", "")
            )
            return Pair(link, Result(opml.value, opml.exception))
        }
        catch(e: HttpRequestException)
        {
            return Pair(link, Result.wrong(e))
        }
        catch(e: IllegalArgumentException)
        {
            return Pair(link, Result.wrong(e))
        }
    }

    private var rawCallback: ((Result<Opml>) -> Unit)? = null
    private var processedCallBack: ((Result<ArrayList<OutlineContent>>) -> Unit)? = null
    private var processingFilter: ((Outline) -> Boolean)? = null

    override fun onPostExecute(result: Pair<String, Result<Opml>>) {
        dialog.dismiss()

        if (rawCallback != null)
            rawCallback!!(result.second)

        if (processedCallBack != null){
            if (result.second.isTrue()){
                try{
                    val opml = result.second.value!!
                    val processed = opml.traverse(processingFilter)
                    Log.d(TAG, "Length of opml outlines ${opml.body?.outline?.get(0)?.outline?.size} compared to processed outlines ${processed.size}")

                    context.getMain().setOpmlCache(result.first, processed, PreferenceDefaults.OPML_NEWS_SOURCES_LISTING_CACHE_IN_MINUTES)
                    val res = Result.right(processed)
                    processedCallBack!!(res)
                }catch (e: Exception){
                    val res = Result.wrong<ArrayList<OutlineContent>>(e)
                    processedCallBack!!(res)
                }
            }else
                processedCallBack!!(Result.wrong<ArrayList<OutlineContent>>(result.second.exception))
        }
    }

    //Set up function to call when download is done
    fun executeOnRawCompletion(action: ((Result<Opml>) -> Unit)?): DownloadOpmlAsync {
        rawCallback = action
        return this
    }

    //set up function to call when download is done, include optional processing filter
    fun executeOnProcessedCompletion(action: ((Result<ArrayList<OutlineContent>>) -> Unit)?,
                                            filter: ((Outline) -> Boolean)? = null): DownloadOpmlAsync {
        processedCallBack = action
        processingFilter = filter
        return this
    }
}


fun downloadOpmlAsync(context: Activity, url: String, title: String) {
    val cache = context.getMain().getOpmlCache(url)

    if (cache != null){
        startOutlinerActivity(context, cache, title, url, false)
    }
    else{
        DownloadOpmlAsync(context)
                .executeOnProcessedCompletion({
            res ->
            if (res.isTrue()){
                startOutlinerActivity(context, res.value!!, title, url, false)
            }
            else{
                context.toastee("Downloading url fails because of ${res.exception?.message}", Duration.LONG)
            }
        }, { outline -> outline.text != "<rules>" })
                .execute(url)
    }
}
