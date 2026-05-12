package com.example.lcb.app.weather.ui.ads

import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import com.android.common.bill.ui.NativeAdStyleType
import com.example.lcb.app.utils.loadInterstitial
import com.example.lcb.app.utils.loadNative

/**
 * Walk the ContextWrapper chain to find the hosting FragmentActivity.
 * Returns null if not embedded in one (e.g. preview).
 */
internal fun Context.findFragmentActivity(): FragmentActivity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is FragmentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Stable
class NativeAdSlotState internal constructor(
    private val activity: FragmentActivity,
    private val styleType: NativeAdStyleType
) {
    private var container: FrameLayout? = null
    private var hasRequestedAd = false

    internal fun obtainContainer(context: Context): FrameLayout {
        val existing = container
        if (existing != null) {
            (existing.parent as? ViewGroup)?.removeView(existing)
            return existing
        }

        return FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            visibility = View.GONE
        }.also { newContainer ->
            container = newContainer
            loadOnce(newContainer)
        }
    }

    private fun loadOnce(container: FrameLayout) {
        if (hasRequestedAd) return
        hasRequestedAd = true
        activity.loadNative(container = container, styleType = styleType)
    }
}

@Composable
fun rememberNativeAdSlotState(
    styleType: NativeAdStyleType = NativeAdStyleType.STANDARD
): NativeAdSlotState? {
    val context = LocalContext.current
    val activity = context.findFragmentActivity() ?: return null
    return remember(activity, styleType) {
        NativeAdSlotState(activity = activity, styleType = styleType)
    }
}

/**
 * Native ad slot embedded inline in Compose UI. Renders an Android FrameLayout
 * via AndroidView and lets BusinessAdExt populate it. Visibility (show / hide)
 * is fully controlled inside `loadNative` — the container starts collapsed and
 * only becomes visible once an ad successfully loads, so no placeholder space
 * is reserved when there is nothing to show.
 *
 * When placing this inside LazyColumn / LazyRow, create the state outside the
 * lazy item with rememberNativeAdSlotState() and pass it to NativeAdSlot(state).
 * Otherwise the lazy item can be disposed off-screen and request the ad again.
 */
@Composable
fun NativeAdSlot(
    modifier: Modifier = Modifier,
    styleType: NativeAdStyleType = NativeAdStyleType.STANDARD
) {
    val state = rememberNativeAdSlotState(styleType = styleType) ?: return
    NativeAdSlot(state = state, modifier = modifier)
}

@Composable
fun NativeAdSlot(
    state: NativeAdSlotState,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx -> state.obtainContainer(ctx) }
    )
}

/**
 * Runs the next UI action from the interstitial callback only. BusinessAdExt
 * owns request, display, timeout, and failure handling, so callers only provide
 * the navigation/open action that should continue after that flow completes.
 */
fun Context.runAfterInterstitial(action: () -> Unit) {
    val activity = findFragmentActivity()
    if (activity == null) {
        action()
        return
    }
    activity.loadInterstitial(call = { _ -> action() })
}
