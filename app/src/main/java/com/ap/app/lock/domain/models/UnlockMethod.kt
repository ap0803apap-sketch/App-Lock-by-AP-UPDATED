package com.ap.app.lock.domain.models

sealed class UnlockMethod {
    object BiometricOnly : UnlockMethod()
    data class PinCode(val digits: Int) : UnlockMethod()
    data class Password(val minLength: Int = 8) : UnlockMethod()
    object DeviceLock : UnlockMethod()
}
