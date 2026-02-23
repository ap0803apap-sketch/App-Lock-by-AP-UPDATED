package com.ap.app.lock.utils

object PasswordValidator {

    data class PasswordStrength(
        val isValid: Boolean,
        val message: String,
        val strength: Int
    )

    fun validatePin(pin: String, digitCount: Int): Pair<Boolean, String> {
        return when {
            pin.length != digitCount -> Pair(false, "PIN must be $digitCount digits")
            !pin.all { it.isDigit() } -> Pair(false, "PIN must contain only digits")
            else -> Pair(true, "Valid PIN")
        }
    }

    fun validatePassword(password: String): PasswordStrength {
        val errors = mutableListOf<String>()
        var strength = 0

        if (password.length < Constants.PASSWORD_MIN_LENGTH) {
            errors.add("Minimum 8 characters required")
        } else {
            strength += 1
        }

        if (password.any { it.isUpperCase() }) {
            strength += 1
        } else {
            errors.add("At least 1 uppercase letter required")
        }

        if (password.any { it.isLowerCase() }) {
            strength += 1
        } else {
            errors.add("At least 1 lowercase letter required")
        }

        if (password.any { it.isDigit() }) {
            strength += 1
        } else {
            errors.add("At least 1 number required")
        }

        if (password.any { !it.isLetterOrDigit() }) {
            strength += 1
        } else {
            errors.add("At least 1 special character required")
        }

        return PasswordStrength(
            isValid = errors.isEmpty(),
            message = if (errors.isEmpty()) "Valid password" else errors.joinToString(", "),
            strength = strength
        )
    }

    fun validatePasswordsMatch(password1: String, password2: String): Boolean {
        return password1 == password2 && password1.isNotEmpty()
    }
}