package com.joshsoftware.core

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.joshsoftware.core.model.User

class AppSharedPreferences(context: Context) {
    private val KEY_WALLY_SHARED_PREFS = "KEY_WALLY_SHARED_PREFS"
    private val KEY_USER_DATA = "KEY_USER_DATA"
    private val KEY_USER_ID = "KEY_USER_ID"

    private var sharedPreferences: SharedPreferences =
            context.getSharedPreferences(KEY_WALLY_SHARED_PREFS, Context.MODE_PRIVATE)

    var userData: User? = null
        get() {
            val storedString = sharedPreferences.getString(KEY_USER_DATA, "")
            val type = object: TypeToken<User>(){}.type
            userData = Gson().fromJson<User>(storedString, type)
            return field
        }
        private set

    var userId: String? = null
    get() {
        return sharedPreferences.getString(KEY_USER_ID, "")
    }
    private set

    fun saveUserData(user: User?) {
        val type = object: TypeToken<User>(){}.type
        val userData = Gson().toJson(user, type)
        sharedPreferences.putValues {
            it.putString(KEY_USER_DATA, userData)
        }
    }

    fun saveUserId(id: String?) {
        sharedPreferences.putValues {
            it.putString(KEY_USER_ID, id)
        }
    }

    fun deleteUserData() {
        sharedPreferences.edit().remove(KEY_USER_DATA).apply();
        sharedPreferences.edit().remove(KEY_USER_ID).apply();
    }
    /**
     * Extension function for shared preferences
     */
    private fun SharedPreferences.putValues(func:(SharedPreferences.Editor)-> Unit) {
        val editor: SharedPreferences.Editor = this.edit()
        func(editor)
        editor.apply()
    }
}