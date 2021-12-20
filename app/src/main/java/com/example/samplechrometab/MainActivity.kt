package com.example.samplechrometab

import android.content.ComponentName
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.*


class MainActivity : AppCompatActivity() {

    private val TAG = "CustomTabs"

    private var mCustomTabsSession: CustomTabsSession? = null
    private var mClient: CustomTabsClient? = null
    private var mConnection: CustomTabsServiceConnection? = null
    private var mPackageNameToBind: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val b = findViewById(R.id.button) as Button
        b.setOnClickListener { // URLをひらく
            lunchCustomTabs("https://google.co.jp/")
        }
        //
        bindCustomTabsService()
    }

    override fun onDestroy() {
        unbindCustomTabsService()
        super.onDestroy()
    }

    private fun bindCustomTabsService() {
        if (mClient != null) return

        //　接続先はChromeのバージョンによって異なる
        // "com.android.chrome", "com.chrome.beta",
        // "com.chrome.dev",  "com.google.android.apps.chrome"
        mPackageNameToBind = CustomTabsHelper.getPackageNameToUse(this)
        if (mPackageNameToBind == null) return

        // ブラウザ側と接続したときの処理
        mConnection = object : CustomTabsServiceConnection() {
            override fun onServiceDisconnected(name: ComponentName?) {
                TODO("Not yet implemented")
            }

            override fun onCustomTabsServiceConnected(
                name: ComponentName?,
                client: CustomTabsClient?
            ) {
                mClient = client
                mCustomTabsSession = mClient?.newSession(object : CustomTabsCallback() {
                    override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
                        super.onNavigationEvent(navigationEvent, extras)
                    }
                    //                    fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
//                        Log.w(TAG, "onNavigationEvent: Code = $navigationEvent")
//                    }
                })
            }
            //            fun onCustomTabsServiceConnected(
//                name: ComponentName?,
//                client: CustomTabsClient?
//            ) {
//                // セッションを確立する
//                mClient = client
//                mCustomTabsSession = mClient.newSession(object : CustomTabsCallback() {
//                    fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
//                        Log.w(TAG, "onNavigationEvent: Code = $navigationEvent")
//                    }
//                })
//            }
//
//            fun onServiceDisconnected(name: ComponentName?) {
//                mClient = null
//            }
        }

        // バインドの開始
        CustomTabsClient.bindCustomTabsService(
            this,
            mPackageNameToBind,
            mConnection
        )
    }

    private fun unbindCustomTabsService() {
        mConnection?.let {
            unbindService(it)
            mClient = null
            mCustomTabsSession = null
        }
    }

    private fun lunchCustomTabs(url: String) {
        // ビルダーを使って表示方法を指定する
        val builder: CustomTabsIntent.Builder = CustomTabsIntent.Builder(mCustomTabsSession)
        builder.setToolbarColor(Color.BLUE).setShowTitle(true)
        builder.setStartAnimations(this, R.anim.slide_in_right, R.anim.slide_out_left)
        builder.setExitAnimations(this, R.anim.slide_in_left, R.anim.slide_out_right)
        // CustomTabsでURLをひらくIntentを発行
        val customTabsIntent: CustomTabsIntent = builder.build()
        customTabsIntent.launchUrl(this, Uri.parse(url))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)
    }
}