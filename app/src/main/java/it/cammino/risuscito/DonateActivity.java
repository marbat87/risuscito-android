package it.cammino.risuscito;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.cammino.risuscito.ui.ThemeableActivity;
import it.cammino.risuscito.utils.ThemeUtils;

public class DonateActivity extends ThemeableActivity implements PurchasesUpdatedListener {

  private final String TAG = getClass().getCanonicalName();
  private final String SKU_1_EURO = "sku_consumable_1_euro";
  private final String SKU_5_EURO = "sku_consumable_5_euro";
  private final String SKU_10_EURO = "sku_consumable_10_euro";

  @BindView(R.id.risuscito_toolbar)
  Toolbar mToolbar;

  @BindView(R.id.donate_text)
  WebView mDonateView;

  @BindView(R.id.bottom_bar)
  View mBottomBar;

  @BindView(R.id.loadingBar)
  View mLoadingBar;

  @BindView(R.id.main_content)
  View mMainContent;

  @BindView(R.id.donate_1)
  Button mDonateSmallButton;

  @BindView(R.id.donate_5)
  Button mDonateMediumButton;

  @BindView(R.id.donate_10)
  Button mDonateLargeButton;

  //    public static final int BILLING_MANAGER_NOT_INITIALIZED  = -1;

  private BillingClient mBillingClient;
  //    private int mBillingClientResponseCode = BILLING_MANAGER_NOT_INITIALIZED;
  private Set<String> mTokensToBeConsumed;

  /** True if billing service is connected now. */
  private boolean mIsServiceConnected;

  @OnClick(R.id.donate_1)
  public void donate1Euro() {
    //    initiatePurchaseFlow(SKU_1_EURO, null, BillingClient.SkuType.INAPP);
    initiatePurchaseFlow(SKU_1_EURO, BillingClient.SkuType.INAPP);
  }

  @OnClick(R.id.donate_5)
  public void donate5Euro() {
    //    initiatePurchaseFlow(SKU_5_EURO, null, BillingClient.SkuType.INAPP);
    initiatePurchaseFlow(SKU_5_EURO, BillingClient.SkuType.INAPP);
  }

  @OnClick(R.id.donate_10)
  public void donate10Euro() {
    //    initiatePurchaseFlow(SKU_10_EURO, null, BillingClient.SkuType.INAPP);
    initiatePurchaseFlow(SKU_10_EURO, BillingClient.SkuType.INAPP);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.layout_donate);
    ButterKnife.bind(this);

    mBillingClient = BillingClient.newBuilder(this).setListener(this).build();

    ((TextView) findViewById(R.id.main_toolbarTitle)).setText(R.string.title_activity_donate);
    mToolbar.setBackgroundColor(getThemeUtils().primaryColor());
    setSupportActionBar(mToolbar);
    // noinspection ConstantConditions
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    mDonateView.setBackgroundColor(0);
    mBottomBar.setBackgroundColor(getThemeUtils().primaryColor());

    String textColor = "#000000";
    if (ThemeUtils.isDarkMode(this)) textColor = "#ffffff";

    String text =
        "<html><head>"
            + "<style type=\"text/css\">body{color: "
            + textColor
            + "; opacity: 0.87;}"
            + "</style></head>"
            + "<body>"
            + getString(R.string.donate_long_text)
            + "</body></html>";

    mDonateView.loadData(text, "text/html; charset=utf-8", "UTF-8");
    mDonateView.getSettings().setTextZoom(90);
  }

  @Override
  protected void onResume() {
    super.onResume();
    mLoadingBar.setVisibility(View.VISIBLE);
    List<String> skuList = new ArrayList<>();
    skuList.add(SKU_1_EURO);
    skuList.add(SKU_5_EURO);
    skuList.add(SKU_10_EURO);
    mDonateSmallButton.setEnabled(false);
    mDonateMediumButton.setEnabled(false);
    mDonateLargeButton.setEnabled(false);
    querySkuDetailsAsync(BillingClient.SkuType.INAPP, skuList);
  }

  @Override
  public void onDestroy() {
    //        if (bp != null)
    //            bp.release();
    super.onDestroy();
    if (mBillingClient != null && mBillingClient.isReady()) {
      mBillingClient.endConnection();
      mBillingClient = null;
    }
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
  public void onBackPressed() {
    Log.d(TAG, "onBackPressed: ");
    finish();
    overridePendingTransition(0, R.anim.slide_out_bottom);
  }

  public void startServiceConnection(final Runnable executeOnSuccess) {
    mBillingClient.startConnection(
        new BillingClientStateListener() {
          @Override
          public void onBillingSetupFinished(
              @BillingClient.BillingResponse int billingResponseCode) {
            Log.d(TAG, "Setup finished. Response code: " + billingResponseCode);

            if (billingResponseCode == BillingClient.BillingResponse.OK) {
              mIsServiceConnected = true;
              if (executeOnSuccess != null) {
                executeOnSuccess.run();
              }
            }
            //                        mBillingClientResponseCode = billingResponseCode;
          }

          @Override
          public void onBillingServiceDisconnected() {
            Log.d(TAG, "onBillingServiceDisconnected");
            mIsServiceConnected = false;
          }
        });
  }

  public void querySkuDetailsAsync(
      @BillingClient.SkuType final String itemType, final List<String> skuList) {
    // Creating a runnable from the request to use it inside our connection retry policy below
    Runnable queryRequest =
        new Runnable() {
          @Override
          public void run() {
            // Query the purchase async
            SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
            params.setSkusList(skuList).setType(itemType);
            mBillingClient.querySkuDetailsAsync(
                params.build(),
                new SkuDetailsResponseListener() {
                  @Override
                  public void onSkuDetailsResponse(
                      int responseCode,
                      List<com.android.billingclient.api.SkuDetails> skuDetailsList) {
                    Log.d(TAG, "onSkuDetailsResponse");
                    if (responseCode != BillingClient.BillingResponse.OK) {
                      Log.e(TAG, "Unsuccessful query. Error code: " + responseCode);
                      Snackbar.make(
                              findViewById(R.id.main_content),
                              "Unsuccessful query. Error code: " + responseCode,
                              Snackbar.LENGTH_SHORT)
                          .show();
                      mBottomBar.setVisibility(View.VISIBLE);
                    } else if (skuDetailsList != null && skuDetailsList.size() > 0) {
                      // Then fill all the other rows
                      for (SkuDetails details : skuDetailsList) {
                        Log.i(TAG, "Adding sku: " + details);
                        switch (details.getSku()) {
                          case SKU_1_EURO:
                            mDonateSmallButton.setText(details.getPrice());
                            mDonateSmallButton.setEnabled(true);
                            break;
                          case SKU_5_EURO:
                            mDonateMediumButton.setText(details.getPrice());
                            mDonateMediumButton.setEnabled(true);
                            break;
                          case SKU_10_EURO:
                            mDonateLargeButton.setText(details.getPrice());
                            mDonateLargeButton.setEnabled(true);
                            break;
                          default:
                            break;
                        }
                      }
                      mBottomBar.setVisibility(View.VISIBLE);
                    }

                    mLoadingBar.setVisibility(View.INVISIBLE);
                  }
                });
          }
        };

    executeServiceRequest(queryRequest);
  }

  /** Start a purchase or subscription replace flow */
  public void initiatePurchaseFlow(
      final String skuId,
          //      final ArrayList<String> oldSkus,
          final @BillingClient.SkuType String billingType) {
    Log.d(TAG, "initiatePurchaseFlow: ");
    mLoadingBar.setVisibility(View.VISIBLE);
    Runnable purchaseFlowRequest =
        new Runnable() {
          @Override
          public void run() {
            Log.d(TAG, "Launching in-app purchase flow.");
            BillingFlowParams purchaseParams =
                BillingFlowParams.newBuilder()
                    .setSku(skuId)
                    .setType(billingType)
                    //                    .setOldSkus(oldSkus)
                    .build();
            mBillingClient.launchBillingFlow(DonateActivity.this, purchaseParams);
          }
        };

    executeServiceRequest(purchaseFlowRequest);
  }

  public void consumeAsync(final String purchaseToken) {
    // If we've already scheduled to consume this token - no action is needed (this could happen
    // if you received the token when querying purchases inside onReceive() and later from
    // onActivityResult()
    if (mTokensToBeConsumed == null) {
      mTokensToBeConsumed = new HashSet<>();
    } else if (mTokensToBeConsumed.contains(purchaseToken)) {
      Log.i(TAG, "Token was already scheduled to be consumed - skipping...");
      return;
    }
    mTokensToBeConsumed.add(purchaseToken);

    // Generating Consume Response listener
    final ConsumeResponseListener onConsumeListener =
        new ConsumeResponseListener() {
          @Override
          public void onConsumeResponse(
              @BillingClient.BillingResponse int responseCode, String purchaseToken) {
            // If billing service was disconnected, we try to reconnect 1 time
            // (feel free to introduce your retry policy here).
            mLoadingBar.setVisibility(View.GONE);
            if (responseCode == BillingClient.BillingResponse.OK)
              Snackbar.make(mMainContent, R.string.purchase_success, Snackbar.LENGTH_LONG).show();
            else {
              Log.e(TAG, "onConsumeResponse ERROR: " + responseCode);
            }
          }
        };

    // Creating a runnable from the request to use it inside our connection retry policy below
    Runnable consumeRequest =
        new Runnable() {
          @Override
          public void run() {
            // Consume the purchase async
            mBillingClient.consumeAsync(purchaseToken, onConsumeListener);
          }
        };

    executeServiceRequest(consumeRequest);
  }

  private void executeServiceRequest(Runnable runnable) {
    if (mIsServiceConnected) {
      runnable.run();
    } else {
      // If billing service was disconnected, we try to reconnect 1 time.
      // (feel free to introduce your retry policy here).
      startServiceConnection(runnable);
    }
  }

  @Override
  public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
    Log.d(TAG, "onPurchasesUpdated: ");
    Log.d(TAG, "onBillingError: " + responseCode);
    switch (responseCode) {
      case BillingClient.BillingResponse.OK:
        if (purchases != null)
          for (Purchase purchase : purchases) consumeAsync(purchase.getPurchaseToken());
        else mLoadingBar.setVisibility(View.GONE);
        break;
      case BillingClient.BillingResponse.USER_CANCELED:
        mLoadingBar.setVisibility(View.GONE);
        Snackbar.make(mMainContent, getString(R.string.user_canceled), Snackbar.LENGTH_LONG).show();
        break;
      case BillingClient.BillingResponse.BILLING_UNAVAILABLE:
      case BillingClient.BillingResponse.SERVICE_UNAVAILABLE:
        mLoadingBar.setVisibility(View.GONE);
        Snackbar.make(mMainContent, getString(R.string.service_unavailable), Snackbar.LENGTH_LONG)
            .show();
        break;
      case BillingClient.BillingResponse.ITEM_UNAVAILABLE:
        mLoadingBar.setVisibility(View.GONE);
        Snackbar.make(mMainContent, getString(R.string.object_invalid), Snackbar.LENGTH_LONG)
            .show();
        break;
      case BillingClient.BillingResponse.DEVELOPER_ERROR:
        mLoadingBar.setVisibility(View.GONE);
        Snackbar.make(
                mMainContent,
                "ERROR: " + responseCode + " - BILLING_RESPONSE_RESULT_DEVELOPER_ERROR",
                Snackbar.LENGTH_LONG)
            .show();
        break;
      case BillingClient.BillingResponse.ERROR:
        mLoadingBar.setVisibility(View.GONE);
        Snackbar.make(
                mMainContent,
                "ERROR: " + responseCode + " - BILLING_RESPONSE_RESULT_ERROR",
                Snackbar.LENGTH_LONG)
            .show();
        break;
      case BillingClient.BillingResponse.ITEM_ALREADY_OWNED:
        mLoadingBar.setVisibility(View.GONE);
        Snackbar.make(mMainContent, getString(R.string.object_owned), Snackbar.LENGTH_LONG).show();
        break;
      default:
        mLoadingBar.setVisibility(View.GONE);
        Snackbar.make(mMainContent, "ERROR: " + responseCode, Snackbar.LENGTH_LONG).show();
        break;
    }
  }
}
