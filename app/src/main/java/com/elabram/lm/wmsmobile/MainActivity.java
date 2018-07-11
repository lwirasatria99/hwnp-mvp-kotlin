package com.elabram.lm.wmsmobile;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.elabram.lm.wmsmobile.fragment.MainAboutFragment;
import com.elabram.lm.wmsmobile.fragment.MainHomeFragment;
import com.elabram.lm.wmsmobile.fragment.MainProfileFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
{
    @BindView(R.id.navigation)
    BottomNavigationView navigation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_home);
    }

    BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        Fragment fragment;
        switch (item.getItemId()) {
            case R.id.navigation_home:
                fragment = new MainHomeFragment();
                replaceFragment(fragment);
                return true;
            case R.id.navigation_dashboard:
                fragment = new MainAboutFragment();
                replaceFragment(fragment);
                return true;
            case R.id.navigation_notifications:
                fragment = new MainProfileFragment();
                replaceFragment(fragment);
                return true;
        }
        return false;
    };

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameContainer, fragment);
        transaction.commit();
    }


}
