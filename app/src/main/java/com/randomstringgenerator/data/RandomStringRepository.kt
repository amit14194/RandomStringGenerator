package com.randomstringgenerator.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.randomstringgenerator.util.Constants.RANDOM_STRING_URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class RandomStringRepository(private val context: Context) {
    private val TAG = "RandomStringRepository"
    private val uri = Uri.parse(RANDOM_STRING_URI)

    suspend fun fetchRandomString(maxLength: Int): RandomStringMetadata? {
        return withContext(Dispatchers.IO) {
            val args = Bundle().apply {
                putInt(ContentResolver.QUERY_ARG_LIMIT, maxLength)
            }
            try {
                context.contentResolver.query(uri, null, args, null)?.use { cursor ->
                    val columnIndex = cursor.getColumnIndexOrThrow("data")
                    Log.i(TAG, "fetchRandomString columnIndex: $columnIndex")
                    if (columnIndex != -1 && cursor.moveToFirst()) {
                        val json = cursor.getString(columnIndex)
                        return@withContext parseJson(json)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while fetchRandomString: $e")
            }
            return@withContext null
        }
    }

    fun parseJson(json: String): RandomStringMetadata {
        val jsonObject = JSONObject(json).getJSONObject("randomText")
        Log.i(TAG, "parseJson jsonObject: $jsonObject")
        return RandomStringMetadata(
            value = jsonObject.getString("value"),
            length = jsonObject.getInt("length"),
            created = jsonObject.getString("created")
        )
    }
}
