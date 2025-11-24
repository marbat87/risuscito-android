package it.cammino.risuscito.ui.composable

import android.util.Log
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults.navigationSuiteType
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass


//@Composable
//fun hasGridLayout(): Boolean {
//    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass
////    Log.d(TAG, "sizeClass minWidthDp: ${sizeClass.minWidthDp}")
////    Log.d(TAG, "sizeClass minHeightDp: ${sizeClass.minHeightDp}")
//    val returnValue = sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
//            && sizeClass.minHeightDp >= WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND
//    Log.d(TAG, "hasGridLayout: $returnValue")
//    return returnValue
//}

@Composable
fun hasFiveMenuElements(): Boolean {
    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass
//    Log.d(TAG, "sizeClass minWidthDp: ${sizeClass.minWidthDp}")
//    Log.d(TAG, "sizeClass minHeightDp: ${sizeClass.minHeightDp}")
    val returnValue =
        sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    Log.d(TAG, "hasFiveMenuElements: $returnValue")
    return returnValue
}

@Composable
fun hasNavigationBar(): Boolean {
    val navigationSuiteType = navigationSuiteType(currentWindowAdaptiveInfo())
    val returnValue = navigationSuiteType == NavigationSuiteType.ShortNavigationBarCompact
            || navigationSuiteType == NavigationSuiteType.ShortNavigationBarMedium
    Log.d(TAG, "hasNavigationBar: $returnValue")
    return returnValue
}

@Composable
fun layoutMargins(): Dp {
    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass
    var returnValue = 16.dp
    if (sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
        returnValue = 24.dp
    }
    Log.d(TAG, "layoutMargins: $returnValue")
    return returnValue
}

private const val TAG = "ComposableUtilities"