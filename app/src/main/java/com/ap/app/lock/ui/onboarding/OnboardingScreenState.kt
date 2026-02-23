package com.ap.app.lock.ui.onboarding

sealed class OnboardingScreenState {
    data class Welcome(val onNext: () -> Unit) : OnboardingScreenState()
    object Permissions : OnboardingScreenState()
    object AppLockAuthSetup : OnboardingScreenState()
    object AppLockType : OnboardingScreenState()
    object SetupComplete : OnboardingScreenState()
}