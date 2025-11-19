package me.oscarsanchez.cacaonet

import android.content.Context

object SessionManager {

    private const val PREFS_NAME = "cacaonet_prefs"
    private const val KEY_USER_TYPE = "user_type"

    fun saveUserType(context: Context, userType: UserType) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_USER_TYPE, userType.name)
            .apply()
    }

    fun getUserType(context: Context): UserType? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val value = prefs.getString(KEY_USER_TYPE, null) ?: return null
        return runCatching { UserType.valueOf(value) }.getOrNull()
    }

    fun clearSession(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
