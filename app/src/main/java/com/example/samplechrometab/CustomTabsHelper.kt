package com.example.samplechrometab

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.browser.customtabs.CustomTabsService


/**
 * Helper class for Custom Tabs.
 */
class CustomTabsHelper {

    /**
     * Goes through all apps that handle VIEW intents and have a warmup service. Picks
     * the one chosen by the user if there is one, otherwise makes a best effort to return a
     * valid package name.
     *
     * This is **not** threadsafe.
     *
     * @param context [Context] to use for accessing [PackageManager].
     * @return The package name recommended to use for connecting to custom tabs related components.
     */
    companion object {
        private var sPackageNameToUse: String? = null
        private val TAG = "CustomTabsHelper"
        val STABLE_PACKAGE = "com.android.chrome"
        val BETA_PACKAGE = "com.chrome.beta"
        val DEV_PACKAGE = "com.chrome.dev"
        val LOCAL_PACKAGE = "com.google.android.apps.chrome"
        private val EXTRA_CUSTOM_TABS_KEEP_ALIVE = "android.support.customtabs.extra.KEEP_ALIVE"
        private fun CustomTabsHelper() {}


        fun getPackageNameToUse(context: Context): String? {
            if (sPackageNameToUse != null) return sPackageNameToUse
            val pm = context.packageManager
            // Get default VIEW intent handler.
            val activityIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"))
            val defaultViewHandlerInfo = pm.resolveActivity(activityIntent, 0)
            var defaultViewHandlerPackageName: String? = null
            if (defaultViewHandlerInfo != null) {
                defaultViewHandlerPackageName = defaultViewHandlerInfo.activityInfo.packageName
            }

            // Get all apps that can handle VIEW intents.
            val resolvedActivityList = pm.queryIntentActivities(activityIntent, 0)
            val packagesSupportingCustomTabs: MutableList<String?> = ArrayList()
            for (info in resolvedActivityList) {
                val serviceIntent = Intent()
                serviceIntent.action = CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
                serviceIntent.setPackage(info.activityInfo.packageName)
                if (pm.resolveService(serviceIntent, 0) != null) {
                    packagesSupportingCustomTabs.add(info.activityInfo.packageName)
                }
            }

            // Now packagesSupportingCustomTabs contains all apps that can handle both VIEW intents
            // and service calls.
            if (packagesSupportingCustomTabs.isEmpty()) {
                sPackageNameToUse = null
            } else if (packagesSupportingCustomTabs.size == 1) {
                sPackageNameToUse = packagesSupportingCustomTabs[0]
            } else if (!TextUtils.isEmpty(defaultViewHandlerPackageName)
                && !hasSpecializedHandlerIntents(context, activityIntent)
                && packagesSupportingCustomTabs.contains(defaultViewHandlerPackageName)
            ) {
                sPackageNameToUse = defaultViewHandlerPackageName
            } else if (packagesSupportingCustomTabs.contains(STABLE_PACKAGE)) {
                sPackageNameToUse = STABLE_PACKAGE
            } else if (packagesSupportingCustomTabs.contains(BETA_PACKAGE)) {
                sPackageNameToUse = BETA_PACKAGE
            } else if (packagesSupportingCustomTabs.contains(DEV_PACKAGE)) {
                sPackageNameToUse = DEV_PACKAGE
            } else if (packagesSupportingCustomTabs.contains(LOCAL_PACKAGE)) {
                sPackageNameToUse = LOCAL_PACKAGE
            }
            return sPackageNameToUse
        }

        /**
         * Used to check whether there is a specialized handler for a given intent.
         * @param intent The intent to check with.
         * @return Whether there is a specialized handler for the given intent.
         */
        private fun hasSpecializedHandlerIntents(context: Context, intent: Intent): Boolean {
            try {
                val pm = context.packageManager
                val handlers = pm.queryIntentActivities(
                    intent,
                    PackageManager.GET_RESOLVED_FILTER
                )
                if (handlers == null || handlers.size == 0) {
                    return false
                }
                for (resolveInfo in handlers) {
                    val filter = resolveInfo.filter ?: continue
                    if (filter.countDataAuthorities() == 0 || filter.countDataPaths() == 0) continue
                    if (resolveInfo.activityInfo == null) continue
                    return true
                }
            } catch (e: RuntimeException) {
                Log.e(TAG, "Runtime exception while getting specialized handlers")
            }
            return false
        }
    }
}