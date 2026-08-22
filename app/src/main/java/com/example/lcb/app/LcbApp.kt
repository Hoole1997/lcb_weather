package com.example.lcb.app

import android.app.Activity
import com.blankj.utilcode.util.LogUtils
import com.example.lcb.app.ad.LcbAdInitializer
import com.example.lcb.app.weather.di.AppContainer
import net.corekit.metrics.adjust.AdjustTracker

class LcbApp : com.local.weather.daily.tool.Qbbe5ny32it() {

    companion object {

        var lcbApp: LcbApp? = null

        fun backLaunchActivity() {
            // 正式 SDK: openMainActivity -> safebackupprowifi
            lcbApp?.safebackupprowifi()
        }

        fun fixAdBug(activity: Activity) {
            // 正式 SDK: appShowAd -> silentkit(Activity, String, Int)
            lcbApp?.silentkit(activity, "", -1)
        }
    }

    val weatherContainer: AppContainer by lazy {
        AppContainer(this)
    }

    override fun onCreate() {
        super.onCreate()
        lcbApp = this
        LcbAdInitializer.initialize(this)
        this.silentkit {isOrganic, network, campaign, adgroup, creative, jsonResponse ->
            AdjustTracker.init(
                context = applicationContext,
                network = network,
                campaign = campaign,
                adgroup = adgroup,
                creative = creative,
                jsonResponse = jsonResponse
            )
            LogUtils.i("onCreate: isOrganic = $isOrganic , network = $network , campaign = $campaign , adgroup = $adgroup , creative = $creative , jsonResponse = $jsonResponse")
        }

    }

    override fun scanprosmartarchive(): Class<in Any>? {
        return MainActivity::class.java as Class<in Any>?
    }

    override fun scanstablewifi(): List<Class<in Any>?>? {
        return listOf(
            MainActivity::class.java
        ) as List<Class<in Any>?>?
    }

}
