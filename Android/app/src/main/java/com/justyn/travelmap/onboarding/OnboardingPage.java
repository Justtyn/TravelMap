package com.justyn.travelmap.onboarding;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

public class OnboardingPage {

    @DrawableRes
    private final int illustrationRes;
    @StringRes
    private final int titleRes;
    @StringRes
    private final int descriptionRes;

    public OnboardingPage(@DrawableRes int illustrationRes,
                          @StringRes int titleRes,
                          @StringRes int descriptionRes) {
        this.illustrationRes = illustrationRes;
        this.titleRes = titleRes;
        this.descriptionRes = descriptionRes;
    }

    public int getIllustrationRes() {
        return illustrationRes;
    }

    public int getTitleRes() {
        return titleRes;
    }

    public int getDescriptionRes() {
        return descriptionRes;
    }
}
