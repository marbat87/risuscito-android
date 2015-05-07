/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.cammino.risuscito;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LUtils {

    protected Activity mActivity;

    private LUtils(Activity activity) {
        mActivity = activity;
    }

    public static LUtils getInstance(Activity activity) {
        return new LUtils(activity);
    }

    private static boolean hasL() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public void startActivityWithTransition(Intent intent, final View clickedView,
                                            final String transitionName) {
        ActivityOptions options = null;
        if (hasL() && clickedView != null && !TextUtils.isEmpty(transitionName)) {
            options = ActivityOptions.makeSceneTransitionAnimation(
                    mActivity, clickedView, transitionName);
            ActivityCompat.startActivity(mActivity, intent, options.toBundle());
        } else {
            mActivity.startActivity(intent);
            mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.hold_on);
        }

        //aggiorno la cronologia
        DatabaseCanti listaCanti = new DatabaseCanti(mActivity);
        SQLiteDatabase db = listaCanti.getReadableDatabase();

        String select = "SELECT COUNT(*) FROM CRONOLOGIA" +
                "        WHERE id_canto = " + intent.getExtras().getInt("idCanto");
        Cursor lista = db.rawQuery(select, null);
        lista.moveToFirst();

        switch (lista.getInt(0)) {
            case 1:
                String update = "UPDATE CRONOLOGIA" +
                        " SET ultima_visita = CURRENT_TIMESTAMP" +
                        " WHERE id_canto = " + intent.getExtras().getInt("idCanto");
                db.execSQL(update);
                break;
            case 0:
                ContentValues values = new ContentValues();
                values.put("id_canto", intent.getExtras().getInt("idCanto"));
                db.insert("CRONOLOGIA", null, values);
                break;
        }

        lista.close();
        db.close();

        }

    public void startActivityWithFadeIn(Intent intent, final View clickedView,
                                        final String transitionName) {
        ActivityOptions options = null;
        if (hasL() && clickedView != null && !TextUtils.isEmpty(transitionName)) {
            options = ActivityOptions.makeSceneTransitionAnimation(
                    mActivity, clickedView, transitionName);
            ActivityCompat.startActivity(mActivity, intent, options.toBundle());
        } else {
            mActivity.startActivity(intent);
            mActivity.overridePendingTransition(R.anim.image_fade_in, R.anim.hold_on);
        }
    }

    public void closeActivityWithTransition() {
        if (hasL())
            mActivity.finishAfterTransition();
        else {
            mActivity.finish();
            mActivity.overridePendingTransition(0, R.anim.slide_out_right);
        }
    }

    public void closeActivityWithFadeOut() {
        if (hasL())
            mActivity.finishAfterTransition();
        else {
            mActivity.finish();
            mActivity.overridePendingTransition(0, R.anim.image_fade_out);
        }
    }

    public void goFullscreen() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            mActivity.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else mActivity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

}