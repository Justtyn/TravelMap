package com.justyn.travelmap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.SparseArrayCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.justyn.travelmap.data.local.UserPreferences;
import com.justyn.travelmap.fragment.BookingFragment;
import com.justyn.travelmap.fragment.HomeFragment;
import com.justyn.travelmap.fragment.MallFragment;
import com.justyn.travelmap.fragment.MyFragment;
import com.justyn.travelmap.fragment.PlanFragment;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_SELECTED_ITEM = "key_selected_nav_item";
    private final SparseArrayCompat<Fragment> fragmentCache = new SparseArrayCompat<>();
    private int currentItemId = R.id.navigation_home;
    private UserPreferences userPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userPreferences = new UserPreferences(this);
        if (!userPreferences.hasLoggedInUser()) {
            redirectToLogin();
            return;
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return WindowInsetsCompat.CONSUMED;
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        restoreFragments();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switchTo(item.getItemId());
            return true;
        });
        bottomNavigationView.setOnItemReselectedListener(item -> {
            // no-op to prevent fragment recreation
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        } else {
            currentItemId = savedInstanceState.getInt(KEY_SELECTED_ITEM, R.id.navigation_home);
            bottomNavigationView.setSelectedItemId(currentItemId);
        }
    }

    private void restoreFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int[] itemIds = {
                R.id.navigation_home,
                R.id.navigation_mall,
                R.id.navigation_booking,
                R.id.navigation_plan,
                R.id.navigation_my
        };
        for (int itemId : itemIds) {
            Fragment fragment = fragmentManager.findFragmentByTag(tagForItem(itemId));
            if (fragment != null) {
                fragmentCache.put(itemId, fragment);
            }
        }
    }

    private void switchTo(@IdRes int itemId) {
        if (currentItemId == itemId && fragmentCache.get(itemId) != null) {
            return;
        }
        Fragment target = fragmentCache.get(itemId);
        if (target == null) {
            target = createFragment(itemId);
            fragmentCache.put(itemId, target);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        androidx.fragment.app.FragmentTransaction transaction = fragmentManager.beginTransaction();

        Fragment current = fragmentCache.get(currentItemId);
        if (current != null && current.isAdded()) {
            transaction.hide(current);
        }

        if (target.isAdded()) {
            transaction.show(target);
        } else {
            transaction.add(R.id.fragmentContainer, target, tagForItem(itemId));
        }

        currentItemId = itemId;
        transaction.commit();
    }

    private Fragment createFragment(@IdRes int itemId) {
        if (itemId == R.id.navigation_mall) {
            return new MallFragment();
        } else if (itemId == R.id.navigation_booking) {
            return new BookingFragment();
        } else if (itemId == R.id.navigation_plan) {
            return new PlanFragment();
        } else if (itemId == R.id.navigation_my) {
            return new MyFragment();
        } else {
            return new HomeFragment();
        }
    }

    private String tagForItem(@IdRes int itemId) {
        return "nav_fragment_" + itemId;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userPreferences != null && !userPreferences.hasLoggedInUser()) {
            redirectToLogin();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_ITEM, currentItemId);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
