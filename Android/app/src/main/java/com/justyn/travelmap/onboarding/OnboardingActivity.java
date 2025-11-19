package com.justyn.travelmap.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.justyn.travelmap.LoginActivity;
import com.justyn.travelmap.R;
import com.justyn.travelmap.data.local.IntroPreferences;

import java.util.Arrays;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 onboardingPager;
    private LinearLayout indicatorLayout;
    private MaterialButton btnNext;
    private MaterialButton btnSkip;
    private IntroPreferences introPreferences;
    private List<OnboardingPage> pages;

    private final ViewPager2.OnPageChangeCallback pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            updateIndicators(position);
            updateButtonState(position);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        introPreferences = new IntroPreferences(this);
        if (introPreferences.isOnboardingCompleted()) {
            navigateToLogin();
            return;
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.onboardingRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        setupPager();
    }

    private void initViews() {
        onboardingPager = findViewById(R.id.onboardingPager);
        indicatorLayout = findViewById(R.id.indicatorLayout);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);
        btnNext.setOnClickListener(v -> handleNext());
        btnSkip.setOnClickListener(v -> completeOnboarding());
    }

    private void setupPager() {
        pages = Arrays.asList(
                new OnboardingPage(R.drawable.ic_onboarding_discover,
                        R.string.onboarding_title_discover,
                        R.string.onboarding_desc_discover),
                new OnboardingPage(R.drawable.ic_onboarding_navigation,
                        R.string.onboarding_title_navigation,
                        R.string.onboarding_desc_navigation),
                new OnboardingPage(R.drawable.ic_onboarding_booking,
                        R.string.onboarding_title_booking,
                        R.string.onboarding_desc_booking),
                new OnboardingPage(R.drawable.ic_onboarding_memory,
                        R.string.onboarding_title_memory,
                        R.string.onboarding_desc_memory)
        );
        onboardingPager.setAdapter(new OnboardingAdapter(pages));
        onboardingPager.registerOnPageChangeCallback(pageChangeCallback);
        setupIndicators();
        updateIndicators(0);
        updateButtonState(0);
    }

    private void setupIndicators() {
        indicatorLayout.removeAllViews();
        int size = pages != null ? pages.size() : 0;
        for (int i = 0; i < size; i++) {
            View indicator = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    getResources().getDimensionPixelSize(R.dimen.onboarding_indicator_inactive_width),
                    getResources().getDimensionPixelSize(R.dimen.onboarding_indicator_height)
            );
            int margin = getResources().getDimensionPixelSize(R.dimen.onboarding_indicator_spacing);
            params.setMarginEnd(margin);
            indicator.setLayoutParams(params);
            indicator.setBackgroundResource(R.drawable.bg_indicator_inactive);
            indicatorLayout.addView(indicator);
        }
    }

    private void updateIndicators(int position) {
        int childCount = indicatorLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = indicatorLayout.getChildAt(i);
            boolean isActive = i == position;
            int widthRes = isActive ? R.dimen.onboarding_indicator_active_width : R.dimen.onboarding_indicator_inactive_width;
            int height = getResources().getDimensionPixelSize(R.dimen.onboarding_indicator_height);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) child.getLayoutParams();
            params.width = getResources().getDimensionPixelSize(widthRes);
            params.height = height;
            child.setLayoutParams(params);
            child.setBackgroundResource(isActive ? R.drawable.bg_indicator_active : R.drawable.bg_indicator_inactive);
        }
    }

    private void updateButtonState(int position) {
        boolean isLastPage = pages != null && position == pages.size() - 1;
        btnNext.setText(isLastPage ? R.string.onboarding_start : R.string.onboarding_next);
        btnSkip.setVisibility(isLastPage ? View.INVISIBLE : View.VISIBLE);
    }

    private void handleNext() {
        int currentItem = onboardingPager.getCurrentItem();
        if (pages != null && currentItem < pages.size() - 1) {
            onboardingPager.setCurrentItem(currentItem + 1, true);
        } else {
            completeOnboarding();
        }
    }

    private void completeOnboarding() {
        introPreferences.setOnboardingCompleted(true);
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (onboardingPager != null) {
            onboardingPager.unregisterOnPageChangeCallback(pageChangeCallback);
        }
        super.onDestroy();
    }
}
