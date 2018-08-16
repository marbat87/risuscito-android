package it.cammino.risuscito

import android.content.res.ColorStateList
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.android.billingclient.api.*
import it.cammino.risuscito.ui.ThemeableActivity
import it.cammino.risuscito.utils.ThemeUtils
import kotlinx.android.synthetic.main.layout_thanks.*
import kotlinx.android.synthetic.main.risuscito_toolbar_noelevation.*
import java.util.*

class ThanksActivity : ThemeableActivity(), PurchasesUpdatedListener {

    private var mBillingClient: BillingClient? = null
    private var mTokensToBeConsumed: MutableSet<String>? = null

    /** True if billing service is connected now.  */
    private var mIsServiceConnected: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_thanks)

        mBillingClient = BillingClient.newBuilder(this).setListener(this).build()

        risuscito_toolbar!!.setBackgroundColor(themeUtils!!.primaryColor())
        setSupportActionBar(risuscito_toolbar)
        supportActionBar!!.setTitle(R.string.title_activity_thanks)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        thanks_text!!.setBackgroundColor(0)
//        bottom_bar!!.setBackgroundColor(themeUtils!!.primaryColor())
        bottom_bar!!.backgroundTint = ColorStateList(arrayOf(intArrayOf()), intArrayOf(themeUtils!!.primaryColor()))

        var textColor = "#000000"
        if (ThemeUtils.isDarkMode(this)) textColor = "#ffffff"

        val text = ("<html><head>"
                + "<style type=\"text/css\">body{color: "
                + textColor
                + "; opacity: 0.87;}"
                + "</style></head>"
                + "<body>"
                + getString(R.string.thanks_long_text)
                + "</body></html>")

        thanks_text!!.loadData(text, "text/html; charset=utf-8", "UTF-8")
        thanks_text!!.settings.textZoom = 90

        thanks_1.setOnClickListener { initiatePurchaseFlow(SKU_1_EURO, BillingClient.SkuType.INAPP) }
        thanks_5.setOnClickListener { initiatePurchaseFlow(SKU_5_EURO, BillingClient.SkuType.INAPP) }
        thanks_10.setOnClickListener { initiatePurchaseFlow(SKU_10_EURO, BillingClient.SkuType.INAPP) }

    }

    override fun onResume() {
        super.onResume()
        loadingBar!!.visibility = View.VISIBLE
        val skuList = ArrayList<String>()
        skuList.add(SKU_1_EURO)
        skuList.add(SKU_5_EURO)
        skuList.add(SKU_10_EURO)
        thanks_1!!.isEnabled = false
        thanks_5!!.isEnabled = false
        thanks_10!!.isEnabled = false
        querySkuDetailsAsync(BillingClient.SkuType.INAPP, skuList)
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (mBillingClient != null && mBillingClient!!.isReady) {
            mBillingClient!!.endConnection()
            mBillingClient = null
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                overridePendingTransition(0, R.anim.slide_out_bottom)
                return true
            }
        }
        return false
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed: ")
        finish()
        overridePendingTransition(0, R.anim.slide_out_bottom)
    }

    private fun startServiceConnection(executeOnSuccess: Runnable?) {
        mBillingClient!!.startConnection(
                object : BillingClientStateListener {
                    override fun onBillingSetupFinished(
                            @BillingClient.BillingResponse billingResponseCode: Int) {
                        Log.d(TAG, "Setup finished. Response code: $billingResponseCode")

                        if (billingResponseCode == BillingClient.BillingResponse.OK) {
                            mIsServiceConnected = true
                            executeOnSuccess?.run()
                        }
                    }

                    override fun onBillingServiceDisconnected() {
                        Log.d(TAG, "onBillingServiceDisconnected")
                        mIsServiceConnected = false
                    }
                })
    }

    private fun querySkuDetailsAsync(
            @BillingClient.SkuType itemType: String, skuList: List<String>) {
        // Creating a runnable from the request to use it inside our connection retry policy below
        val queryRequest = Runnable {
            // Query the purchase async
            val params = SkuDetailsParams.newBuilder()
            params.setSkusList(skuList).setType(itemType)
            mBillingClient!!.querySkuDetailsAsync(
                    params.build()
            ) { responseCode, skuDetailsList ->
                Log.d(TAG, "onSkuDetailsResponse")
                if (responseCode != BillingClient.BillingResponse.OK) {
                    Log.e(TAG, "Unsuccessful query. Error code: $responseCode")
                    Snackbar.make(
                            findViewById(R.id.main_content),
                            "Unsuccessful query. Error code: $responseCode",
                            Snackbar.LENGTH_SHORT)
                            .show()
                    bottom_bar!!.visibility = View.VISIBLE
                } else if (skuDetailsList != null && skuDetailsList.size > 0) {
                    // Then fill all the other rows
                    for (details in skuDetailsList) {
                        Log.i(TAG, "Adding sku: $details")
                        when (details.sku) {
                            SKU_1_EURO -> {
                                thanks_1!!.text = details.price
                                thanks_1!!.isEnabled = true
                            }
                            SKU_5_EURO -> {
                                thanks_5!!.text = details.price
                                thanks_5!!.isEnabled = true
                            }
                            SKU_10_EURO -> {
                                thanks_10!!.text = details.price
                                thanks_10!!.isEnabled = true
                            }
                            else -> {
                            }
                        }
                    }
                    bottom_bar!!.visibility = View.VISIBLE
                }

                loadingBar!!.visibility = View.INVISIBLE
            }
        }

        executeServiceRequest(queryRequest)
    }

    /** Start a purchase or subscription replace flow  */
    private fun initiatePurchaseFlow(
            skuId: String,
            @BillingClient.SkuType billingType: String) {
        Log.d(TAG, "initiatePurchaseFlow: ")
        loadingBar!!.visibility = View.VISIBLE
        val purchaseFlowRequest = Runnable {
            Log.d(TAG, "Launching in-app purchase flow.")
            val purchaseParams = BillingFlowParams.newBuilder()
                    .setSku(skuId)
                    .setType(billingType)
                    .build()
            mBillingClient!!.launchBillingFlow(this@ThanksActivity, purchaseParams)
        }

        executeServiceRequest(purchaseFlowRequest)
    }

    private fun consumeAsync(purchaseToken: String) {
        // If we've already scheduled to consume this token - no action is needed (this could happen
        // if you received the token when querying purchases inside onReceive() and later from
        // onActivityResult()
        if (mTokensToBeConsumed == null) {
            mTokensToBeConsumed = HashSet()
        } else if (mTokensToBeConsumed!!.contains(purchaseToken)) {
            Log.i(TAG, "Token was already scheduled to be consumed - skipping...")
            return
        }
        mTokensToBeConsumed!!.add(purchaseToken)

        // Generating Consume Response listener
        val onConsumeListener = ConsumeResponseListener { responseCode, _ ->
            // If billing service was disconnected, we try to reconnect 1 time
            // (feel free to introduce your retry policy here).
            loadingBar!!.visibility = View.GONE
            if (responseCode == BillingClient.BillingResponse.OK)
                Snackbar.make(main_content!!, R.string.purchase_success, Snackbar.LENGTH_LONG).show()
            else {
                Log.e(TAG, "onConsumeResponse ERROR: $responseCode")
            }
        }

        // Creating a runnable from the request to use it inside our connection retry policy below
        val consumeRequest = Runnable {
            // Consume the purchase async
            mBillingClient!!.consumeAsync(purchaseToken, onConsumeListener)
        }

        executeServiceRequest(consumeRequest)
    }

    private fun executeServiceRequest(runnable: Runnable) {
        if (mIsServiceConnected) {
            runnable.run()
        } else {
            // If billing service was disconnected, we try to reconnect 1 time.
            // (feel free to introduce your retry policy here).
            startServiceConnection(runnable)
        }
    }

    override fun onPurchasesUpdated(responseCode: Int, purchases: List<Purchase>?) {
        Log.d(TAG, "onPurchasesUpdated: ")
        Log.d(TAG, "onBillingError: $responseCode")
        when (responseCode) {
            BillingClient.BillingResponse.OK -> if (purchases != null)
                for (purchase in purchases) consumeAsync(purchase.purchaseToken)
            else
                loadingBar!!.visibility = View.GONE
            BillingClient.BillingResponse.USER_CANCELED -> {
                loadingBar!!.visibility = View.GONE
                Snackbar.make(main_content!!, getString(R.string.user_canceled), Snackbar.LENGTH_LONG).show()
            }
            BillingClient.BillingResponse.BILLING_UNAVAILABLE, BillingClient.BillingResponse.SERVICE_UNAVAILABLE -> {
                loadingBar!!.visibility = View.GONE
                Snackbar.make(main_content!!, getString(R.string.service_unavailable), Snackbar.LENGTH_LONG)
                        .show()
            }
            BillingClient.BillingResponse.ITEM_UNAVAILABLE -> {
                loadingBar!!.visibility = View.GONE
                Snackbar.make(main_content!!, getString(R.string.object_invalid), Snackbar.LENGTH_LONG)
                        .show()
            }
            BillingClient.BillingResponse.DEVELOPER_ERROR -> {
                loadingBar!!.visibility = View.GONE
                Snackbar.make(
                        main_content!!,
                        "ERROR: $responseCode - BILLING_RESPONSE_RESULT_DEVELOPER_ERROR",
                        Snackbar.LENGTH_LONG)
                        .show()
            }
            BillingClient.BillingResponse.ERROR -> {
                loadingBar!!.visibility = View.GONE
                Snackbar.make(
                        main_content!!,
                        "ERROR: $responseCode - BILLING_RESPONSE_RESULT_ERROR",
                        Snackbar.LENGTH_LONG)
                        .show()
            }
            BillingClient.BillingResponse.ITEM_ALREADY_OWNED -> {
                loadingBar!!.visibility = View.GONE
                Snackbar.make(main_content!!, getString(R.string.object_owned), Snackbar.LENGTH_LONG).show()
            }
            else -> {
                loadingBar!!.visibility = View.GONE
                Snackbar.make(main_content!!, "ERROR: $responseCode", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private val TAG = ThanksActivity::class.java.canonicalName
        private const val SKU_1_EURO = "sku_consumable_1_euro"
        private const val SKU_5_EURO = "sku_consumable_5_euro"
        private const val SKU_10_EURO = "sku_consumable_10_euro"
    }

}
