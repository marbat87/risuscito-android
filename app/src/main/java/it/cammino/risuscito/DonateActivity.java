package it.cammino.risuscito;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.Constants;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.firebase.crash.FirebaseCrash;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.cammino.risuscito.ui.ThemeableActivity;

public class DonateActivity extends ThemeableActivity implements BillingProcessor.IBillingHandler {

    BillingProcessor bp;

    /* base64EncodedPublicKey should be YOUR APPLICATION'S PUBLIC KEY
         * (that you got from the Google Play developer console). This is not your
         * developer public key, it's the *app-specific* public key.
         *
         * Instead of just storing the entire literal string here embedded in the
         * program,  construct the key at runtime from pieces or
         * use bit manipulation (for example, XOR with some other string) to hide
         * the actual key.  The key itself is not secret information, but we don't
         * want to make it easy for an attacker to replace the public key with one
         * of their own and then fake messages from the server.
         */
    String base64EncodedPublicKey1 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyc5DMy0PDup7vuO0Twitg7vMHIrN996cchDCGpRDgQSB8+LvnZ";
    String base64EncodedPublicKey2 = "7ji7tOaXFxKiMffXB72dUtLV6dLkM8CosfguQikmRSsDTjLc4TR47pwLbQ4bGsyQwl2bwjilBlxVrcfI2QFTwniWNS8mY3";
    String base64EncodedPublicKey3 = "a5TB+OZEZaLbOqcId4Pl87RW9/oonw+x/u7sVLAmxB+Skqgji3/ExJW/dztPTdjAj0Vu/vqdph0sAV8C++mDKt19IHkX/U";
    String base64EncodedPublicKey4 = "9I2+xqJsQEKyruPR3d5cZkfWCVQ0lfXjQqg4YtuTP3v9rShYBJn2K3sGy2byt4ykSFNoBO6RGZLBlyu1Gyv2a5WDfmvqqPe+HY0BVbPQIDAQAB";

    private final int TEXTZOOM = 90;
    private final String TAG = getClass().getCanonicalName();
    private final String SKU_1_EURO = "sku_consumable_1_euro";
    private final String SKU_5_EURO = "sku_consumable_5_euro";
    private final String SKU_10_EURO = "sku_consumable_10_euro";

    @BindView(R.id.risuscito_toolbar) Toolbar mToolbar;
    @BindView(R.id.donate_text) WebView mDonateView;
    @BindView(R.id.bottom_bar) View mBottomBar;
    @BindView(R.id.loadingBar) View mLoadingBar;
    @BindView(R.id.main_content) View mMainContent;
    @BindView(R.id.donate_1) Button mDonateSmallButton;
    @BindView(R.id.donate_5) Button mDonateMediumButton;
    @BindView(R.id.donate_10) Button mDonateLargeButton;

    @OnClick(R.id.donate_1)
    public void donate1Euro() {
        mLoadingBar.setVisibility(View.VISIBLE);
        boolean isAvailable = BillingProcessor.isIabServiceAvailable(DonateActivity.this);
        if(isAvailable && bp != null && bp.isInitialized()) {
            boolean isOneTimePurchaseSupported = bp.isOneTimePurchaseSupported();
            if(isOneTimePurchaseSupported) {
                // launch payment flow
                bp.purchase(DonateActivity.this, SKU_1_EURO);
            }
        }
        else {
            FirebaseCrash.log(TAG + "donate1Euro - " + getString(R.string.service_unavailable));
            Snackbar.make(mMainContent, R.string.service_unavailable, Snackbar.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.donate_5)
    public void donate5Euro() {
        mLoadingBar.setVisibility(View.VISIBLE);
        boolean isAvailable = BillingProcessor.isIabServiceAvailable(DonateActivity.this);
        if(isAvailable && bp != null && bp.isInitialized()) {
            boolean isOneTimePurchaseSupported = bp.isOneTimePurchaseSupported();
            if(isOneTimePurchaseSupported) {
                // launch payment flow
                bp.purchase(DonateActivity.this, SKU_5_EURO);
            }
        }
        else {
            FirebaseCrash.log(TAG + "donate5Euro - " + getString(R.string.service_unavailable));
            Snackbar.make(mMainContent, R.string.service_unavailable, Snackbar.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.donate_10)
    public void donate10Euro() {
        mLoadingBar.setVisibility(View.VISIBLE);
        boolean isAvailable = BillingProcessor.isIabServiceAvailable(DonateActivity.this);
        if(isAvailable && bp != null && bp.isInitialized()) {
            boolean isOneTimePurchaseSupported = bp.isOneTimePurchaseSupported();
            if(isOneTimePurchaseSupported) {
                // launch payment flow
                bp.purchase(DonateActivity.this, SKU_10_EURO);
            }
        }
        else {
            FirebaseCrash.log(TAG + "donate10Euro - " + getString(R.string.service_unavailable));
            Snackbar.make(mMainContent, R.string.service_unavailable, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_donate);
        ButterKnife.bind(this);

        boolean isAvailable = BillingProcessor.isIabServiceAvailable(DonateActivity.this);
        if(isAvailable) {
            if (Utility.isOnline(DonateActivity.this)) {
                mLoadingBar.setVisibility(View.VISIBLE);
                bp = new BillingProcessor(this, base64EncodedPublicKey1 + base64EncodedPublicKey2 + base64EncodedPublicKey3 + base64EncodedPublicKey4, this);
                mBottomBar.setVisibility(View.VISIBLE);
            }
            else {
                Snackbar.make(findViewById(R.id.main_content)
                        , R.string.no_connection
                        , Snackbar.LENGTH_SHORT)
                        .show();
                mBottomBar.setVisibility(View.GONE);
            }
        }
        else {
            FirebaseCrash.log(TAG + "onCreate - " + getString(R.string.service_unavailable));
            Snackbar.make(mMainContent, R.string.service_unavailable, Snackbar.LENGTH_LONG).show();
            mBottomBar.setVisibility(View.GONE);
        }

        ((TextView)findViewById(R.id.main_toolbarTitle)).setText(R.string.title_activity_donate);
        mToolbar.setBackgroundColor(getThemeUtils().primaryColor());
        setSupportActionBar(mToolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDonateView.setBackgroundColor(0);
        mBottomBar.setBackgroundColor(getThemeUtils().primaryColor());

        String text = "<html><head>"
                + "<style type=\"text/css\">body{color: #000000; opacity: 0.87;}"
                + "</style></head>"
                + "<body>"
                + getString(R.string.donate_long_text)
                + "</body></html>";

        mDonateView.loadData(text, "text/html; charset=utf-8", "UTF-8");
        mDonateView.getSettings().setTextZoom(TEXTZOOM);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onDestroy() {
        if (bp != null)
            bp.release();

        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(0, R.anim.slide_out_bottom);
                return true;
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            overridePendingTransition(0, R.anim.slide_out_bottom);
        }
        return super.onKeyUp(keyCode, event);
    }

    // IBillingHandler implementation
    @Override
    public void onBillingInitialized() {
        /*
         * Called when BillingProcessor was initialized and it's ready to purchase
         */
        Log.d(TAG, "onBillingInitialized: OK");

        SkuDetails skuSmall = bp.getPurchaseListingDetails(SKU_1_EURO);
        if (skuSmall != null)
            mDonateSmallButton.setText(skuSmall.priceText);
        else
            mDonateSmallButton.setEnabled(false);

        SkuDetails skuMedim = bp.getPurchaseListingDetails(SKU_5_EURO);
        if (skuMedim != null)
            mDonateMediumButton.setText(skuMedim.priceText);
        else
            mDonateMediumButton.setEnabled(false);

        SkuDetails skuLarge = bp.getPurchaseListingDetails(SKU_10_EURO);
        if (skuLarge != null)
        mDonateLargeButton.setText(skuLarge.priceText);
        else
            mDonateMediumButton.setEnabled(false);

        mLoadingBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        /*
         * Called when requested PRODUCT ID was successfully purchased
         */
        Log.d(TAG, "onProductPurchased - productId: " + productId);
        switch (productId) {
            case SKU_1_EURO:
                mLoadingBar.setVisibility(View.GONE);
                Snackbar.make(mMainContent, R.string.purchase_success, BaseTransientBottomBar.LENGTH_LONG).show();
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
                int smallDonationsCount = pref.getInt(Utility.SMALL_DONATIONS_COUNT, 0);
                if (bp.consumePurchase(SKU_1_EURO)) {
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putInt(Utility.SMALL_DONATIONS_COUNT, smallDonationsCount + 1);
                    editor.apply();
                }
                break;
            case SKU_5_EURO:
                mLoadingBar.setVisibility(View.GONE);
                Snackbar.make(mMainContent, R.string.purchase_success, BaseTransientBottomBar.LENGTH_LONG).show();
                pref = PreferenceManager.getDefaultSharedPreferences(this);
                int mediumDonationsCount = pref.getInt(Utility.MEDIUM_DONATIONS_COUNT, 0);
                if (bp.consumePurchase(SKU_5_EURO)) {
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putInt(Utility.MEDIUM_DONATIONS_COUNT, mediumDonationsCount + 1);
                    editor.apply();
                }
                break;
            case SKU_10_EURO:
                mLoadingBar.setVisibility(View.GONE);
                Snackbar.make(mMainContent, R.string.purchase_success, BaseTransientBottomBar.LENGTH_LONG).show();
                pref = PreferenceManager.getDefaultSharedPreferences(this);
                int largeDonationsCount = pref.getInt(Utility.LARGE_DONATIONS_COUNT, 0);
                if (bp.consumePurchase(SKU_10_EURO)) {
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putInt(Utility.LARGE_DONATIONS_COUNT, largeDonationsCount + 1);
                    editor.apply();
                }
                break;
            default:
                break;
        }

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        mLoadingBar.setVisibility(View.GONE);
        Log.d(TAG, "onBillingError: " + errorCode);
        String errorString;
        switch (errorCode) {
            case Constants.BILLING_RESPONSE_RESULT_USER_CANCELED:
                errorString = getString(R.string.user_canceled);
                break;
            case Constants.BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE:
            case Constants.BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE:
                errorString = getString(R.string.service_unavailable);
                break;
            case Constants.BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE:
                errorString = getString(R.string.object_invalid);
                break;
            case Constants.BILLING_RESPONSE_RESULT_DEVELOPER_ERROR:
                errorString = "ERROR: " + errorCode + " - BILLING_RESPONSE_RESULT_DEVELOPER_ERROR";
                break;
            case Constants.BILLING_RESPONSE_RESULT_ERROR:
                errorString = "ERROR: " + errorCode + " - BILLING_RESPONSE_RESULT_ERROR";
                break;
            case Constants.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED:
                errorString = getString(R.string.object_owned);
                break;
            default:
                errorString = "ERROR: " + errorCode;
                break;
        }
        Snackbar.make(mMainContent, errorString, BaseTransientBottomBar.LENGTH_LONG).show();
        /*
         * Called when some error occurred. See Constants class for more details
         *
         * Note - this includes handling the case where the user canceled the buy dialog:
          errorCode = Constants.BILLING_RESPONSE_RESULT_USER_CANCELED
         */
    }

    @Override
    public void onPurchaseHistoryRestored() {
        /*
         * Called when purchase history was restored and the list of all owned PRODUCT ID's
         * was loaded from Google Play
         */
        Log.d(TAG, "onPurchaseHistoryRestored: ");
    }

}