package it.cammino.risuscito.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

/**
 * This class include workaround for fix issues in Android behaviour:
 * 1. correct set user visible hint after resumed or created view
 * 2. correct set child menu visibility
 */

public class HFFragment extends Fragment {

    private boolean mViewCreated = false;
    private boolean mIsVisibleToUser = false;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewCreated = true;
        if (savedInstanceState != null && savedInstanceState.containsKey("android:user_visible_hint")) {
            super.setUserVisibleHint(mIsVisibleToUser);
        }
    }

    @Override
    public void setUserVisibleHint(final boolean isVisibleToUser) {
        mIsVisibleToUser = isVisibleToUser;
        super.setUserVisibleHint(isVisibleToUser);
        if (mViewCreated)
            setChildMenuVisibility(mIsVisibleToUser);
    }

    @Override
    public boolean getUserVisibleHint() {
        return mIsVisibleToUser;
    }

    private void setChildMenuVisibility(boolean visible) {
        final FragmentManager child_fm = getChildFragmentManager();
        if (child_fm == null || child_fm.getFragments() == null)
            return;
        for (Fragment f: child_fm.getFragments())
            if (f != null) f.setMenuVisibility(visible);
    }
}