package com.example.lcb.app

import com.blankj.utilcode.util.LogUtils
import com.example.lcb.app.ad.LcbAdInitializer
import com.example.lcb.app.weather.di.AppContainer
import net.corekit.metrics.adjust.AdjustTracker

class LcbApp : com.local.weather.daily.tool.Qbbe5ny32it() {

    companion object {

        var lcbApp: LcbApp? = null

        fun backLaunchActivity() {
            lcbApp?.sharetoolpanel()
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

    override fun scanstablewifi(): Class<in Any>? {
        return MainActivity::class.java as Class<in Any>?
    }

    override fun safesecureprosignal(): List<Class<in Any>?>? {
        return listOf(
            MainActivity::class.java
        ) as List<Class<in Any>?>?
    }

}
