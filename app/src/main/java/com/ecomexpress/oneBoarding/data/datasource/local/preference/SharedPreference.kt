package com.ecomexpress.oneBoarding.data.datasource.local.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.PREF_NAME
import com.ecomexpress.oneBoarding.utils.comman.AppConstants.STORE_NAME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.lang.Double.isNaN
import javax.inject.Inject

/*
Datastore runs on a separate thread which makes it thread-safe
Async API for storing and reading the data (Flow)
Safe to call from UI thread (Dispatchers.IO underneath)
-> Preferences Datastore [that stores key-value pairs.]
-> Proto Datastore  [that stores typed objects (backed by protocol buffers)]
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = STORE_NAME,
    produceMigrations = { context -> listOf(SharedPreferencesMigration(context, PREF_NAME)) })


class SharedPreference @Inject constructor(private val context: Context) {

    suspend fun putString(key: String, value: String) {
        val preferencesKey = stringPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    suspend fun putLong(key: String, value: Long) {
        val preferencesKey = longPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    suspend fun putBoolean(key: String, value: Boolean) {
        val preferencesKey = booleanPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    suspend fun putDouble(key: String, value: Double) {
        val preferencesKey = doublePreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    suspend fun putInt(key: String, value: Int) {
        val preferencesKey = intPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    suspend fun getInt(Key: String): Int? {
        return try {
            val preferencesKey = intPreferencesKey(Key)
            val preferences = context.dataStore.data.first()
            preferences[preferencesKey]
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getLong(Key: String): Long? {
        return try {
            val preferencesKey = longPreferencesKey(Key)
            val preferences = context.dataStore.data.first()
            preferences[preferencesKey]
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getString(Key: String): String? {
        return try {
            val preferencesKey = stringPreferencesKey(Key)
            val preferences = context.dataStore.data.first()
            preferences[preferencesKey] ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    suspend fun getBoolean(Key: String): Boolean? {
        return try {
            val preferencesKey = booleanPreferencesKey(Key)
            val preferences = context.dataStore.data.first()
            preferences[preferencesKey] ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    suspend fun getDouble(Key: String): Double? {
        return try {
            val preferencesKey = doublePreferencesKey(Key)
            val preferences = context.dataStore.data.first()
            //Log.d("getkeyvalue-----",preferences[preferencesKey].toString())
            if (isNaN((preferences[preferencesKey])!!))
                0.0
            else
                preferences[preferencesKey]
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
    }

    fun isKeyStored(key: Preferences.Key<String>): Flow<Boolean> =
        context.dataStore.data.map { preference ->
            preference.contains(key)
        }

    suspend fun clearDataStore() {
        context.dataStore.edit {
            it.clear()
        }
    }
}