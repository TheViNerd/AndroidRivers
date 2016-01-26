package com.silverkeytech.android_rivers.activities

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.actionbarsherlock.view.Window
import com.silverkeytech.android_rivers.Params
import org.holoeverywhere.app.Activity

class WebViewActivity: Activity() {
    companion object {
        val TAG: String = WebViewActivity::class.java.simpleName
    }

    var uri: String? = null
    var web: WebView? = null
    public override fun onCreate(savedInstanceState: Bundle?): Unit {
        //setTheme(this.getVisualPref().getTheme())

        web = WebView(applicationContext!!)
        requestWindowFeature(Window.FEATURE_PROGRESS)
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
        setSupportProgressBarVisibility(true)
        setSupportProgressBarIndeterminateVisibility(true)

        web!!.settings!!.javaScriptEnabled = true

        web!!.setWebChromeClient(object: WebChromeClient(){
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                this@WebViewActivity.setSupportProgress(newProgress * 100)
                if (newProgress == 100){
                    setSupportProgressBarIndeterminateVisibility(false)
                    setProgressBarVisibility(false)
                }
            }
        })

        web!!.setWebViewClient(object : WebViewClient(){
            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                this@WebViewActivity.toastee("Sorry, I have problem loading this web page")
            }
        })

        super.onCreate(savedInstanceState)
        setContentView(web!!)

        val actionBar = supportActionBar!!
        actionBar.setDisplayShowHomeEnabled(false) //hide the app icon.


        val i = intent!!
        uri = i.getStringExtra(Params.BUILT_IN_BROWSER_URI)!!
        web!!.loadUrl(uri!!)
    }


    var isPaused: Boolean = false

    //ref http://www.anddev.org/other-coding-problems-f5/webviewcorethread-problem-t10234.html
    public override fun onPause() {
        if (!isPaused){
            val pause = WebView::class.java.getMethod("onPause")
            pause.invoke(web)
            web!!.pauseTimers()
        }
        super.onPause()
    }

    public override fun onResume() {
        if (isPaused){
            val resume = WebView::class.java.getMethod("onResume")
            resume.invoke(web)
            web!!.resumeTimers()
            isPaused = false
        }
        super.onResume()
    }
}