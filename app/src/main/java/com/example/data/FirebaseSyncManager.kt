package com.example.data

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SlotSyncData(
    val slot: SaveSlotEntity?,
    val teams: List<TeamEntity>,
    val players: List<PlayerEntity>,
    val coaches: List<CoachEntity>,
    val sponsors: List<SponsorEntity>,
    val finances: List<FinanceEntity>,
    val matches: List<MatchEntity>,
    val news: List<NewsEntity>,
    val notifications: List<NotificationEntity>,
    val trophies: List<TrophyEntity>,
    val transfers: List<TransferEntity>
)

class FirebaseSyncManager private constructor(context: Context) {

    private val prefs = context.getSharedPreferences("firebase_sync_prefs", Context.MODE_PRIVATE)
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // Configuration
    var apiKey: String
        get() = prefs.getString("api_key", "AIzaSyAs_DEMO_KEY_ipl_tycoon_728f") ?: "AIzaSyAs_DEMO_KEY_ipl_tycoon_728f"
        set(value) = prefs.edit().putString("api_key", value).apply()

    var projectId: String
        get() = prefs.getString("project_id", "ipl-tycoon-sync-demo") ?: "ipl-tycoon-sync-demo"
        set(value) = prefs.edit().putString("project_id", value).apply()

    // Auth State
    var email: String?
        get() = prefs.getString("email", null)
        private set(value) = prefs.edit().putString("email", value).apply()

    var uid: String?
        get() = prefs.getString("uid", null)
        private set(value) = prefs.edit().putString("uid", value).apply()

    var idToken: String?
        get() = prefs.getString("id_token", null)
        private set(value) = prefs.edit().putString("id_token", value).apply()

    val isLoggedIn: Boolean
        get() = !uid.isNullOrBlank() && !idToken.isNullOrBlank()

    fun logout() {
        prefs.edit().remove("email").remove("uid").remove("id_token").apply()
    }

    private fun isDemoOrInvalidKey(): Boolean {
        return apiKey.contains("DEMO", ignoreCase = true) || apiKey.isBlank() || apiKey == "AIzaSyAs_DEMO_KEY_ipl_tycoon_728f"
    }

    private fun loginOfflineDemoMode(emailInput: String, message: String): Result<String> {
        val safeEmail = emailInput.ifBlank { "manager@ipltycoon.com" }
        uid = "demo_uid_" + safeEmail.hashCode().toUInt().toString(16)
        idToken = "demo_token_" + System.currentTimeMillis()
        email = safeEmail
        return Result.success(message)
    }

    suspend fun signUp(emailInput: String, passwordInput: String): Result<String> = withContext(Dispatchers.IO) {
        if (isDemoOrInvalidKey()) {
            return@withContext loginOfflineDemoMode(emailInput, "Profile created & logged in (Demo / Offline Mode)!")
        }
        try {
            val url = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$apiKey"
            val json = JSONObject().apply {
                put("email", emailInput)
                put("password", passwordInput)
                put("returnSecureToken", true)
            }

            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val respJson = JSONObject(bodyStr)
                    val returnedUid = respJson.getString("localId")
                    val returnedToken = respJson.getString("idToken")
                    val returnedEmail = respJson.getString("email")

                    uid = returnedUid
                    idToken = returnedToken
                    email = returnedEmail

                    Result.success("Profile created successfully!")
                } else {
                    val errMsg = parseAuthError(bodyStr)
                    if (errMsg.contains("API key", ignoreCase = true) || bodyStr.contains("API_KEY", ignoreCase = true)) {
                        loginOfflineDemoMode(emailInput, "Profile created & logged in (Demo / Offline Mode)!")
                    } else {
                        Result.failure(Exception(errMsg))
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("FirebaseSync", "SignUp failed, switching to Demo Mode", e)
            loginOfflineDemoMode(emailInput, "Logged in (Offline Mode)")
        }
    }

    suspend fun signIn(emailInput: String, passwordInput: String): Result<String> = withContext(Dispatchers.IO) {
        if (isDemoOrInvalidKey()) {
            return@withContext loginOfflineDemoMode(emailInput, "Logged in successfully (Demo / Offline Mode)!")
        }
        try {
            val url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$apiKey"
            val json = JSONObject().apply {
                put("email", emailInput)
                put("password", passwordInput)
                put("returnSecureToken", true)
            }

            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val respJson = JSONObject(bodyStr)
                    val returnedUid = respJson.getString("localId")
                    val returnedToken = respJson.getString("idToken")
                    val returnedEmail = respJson.getString("email")

                    uid = returnedUid
                    idToken = returnedToken
                    email = returnedEmail

                    Result.success("Logged in successfully!")
                } else {
                    val errMsg = parseAuthError(bodyStr)
                    if (errMsg.contains("API key", ignoreCase = true) || bodyStr.contains("API_KEY", ignoreCase = true)) {
                        loginOfflineDemoMode(emailInput, "Logged in successfully (Demo / Offline Mode)!")
                    } else {
                        Result.failure(Exception(errMsg))
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("FirebaseSync", "SignIn failed, switching to Demo Mode", e)
            loginOfflineDemoMode(emailInput, "Logged in (Offline Mode)")
        }
    }

    suspend fun uploadSlotData(slotId: String, data: SlotSyncData): Result<Unit> = withContext(Dispatchers.IO) {
        val currentUid = uid ?: return@withContext Result.failure(Exception("Not logged in"))
        if (isDemoOrInvalidKey() || idToken?.startsWith("demo_") == true) {
            return@withContext try {
                val adapter = moshi.adapter(SlotSyncData::class.java)
                val serializedJson = adapter.toJson(data)
                prefs.edit().putString("local_slot_backup_$slotId", serializedJson).apply()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
        try {
            val adapter = moshi.adapter(SlotSyncData::class.java)
            val serializedJson = adapter.toJson(data)

            // Firestore document endpoint
            val documentPath = "users/${currentUid}/slots/${slotId}"
            val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/$documentPath?key=$apiKey"

            // Construct Firestore document payload
            val payload = JSONObject().apply {
                val fields = JSONObject().apply {
                    put("jsonData", JSONObject().apply { put("stringValue", serializedJson) })
                    put("lastSynced", JSONObject().apply { put("integerValue", System.currentTimeMillis().toString()) })
                    put("slotId", JSONObject().apply { put("stringValue", slotId) })
                }
                put("fields", fields)
            }

            val body = payload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $idToken")
                .patch(body) // Patch will create or overwrite the document
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    if (bodyStr.contains("API key", ignoreCase = true) || bodyStr.contains("API_KEY", ignoreCase = true)) {
                        prefs.edit().putString("local_slot_backup_$slotId", serializedJson).apply()
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception("Upload failed: ${response.code} ${response.message}\n$bodyStr"))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Upload failed", e)
            Result.failure(e)
        }
    }

    suspend fun downloadSlotData(slotId: String): Result<SlotSyncData?> = withContext(Dispatchers.IO) {
        val currentUid = uid ?: return@withContext Result.failure(Exception("Not logged in"))
        if (isDemoOrInvalidKey() || idToken?.startsWith("demo_") == true) {
            return@withContext try {
                val serializedJson = prefs.getString("local_slot_backup_$slotId", null)
                if (serializedJson.isNullOrBlank()) {
                    Result.success(null)
                } else {
                    val adapter = moshi.adapter(SlotSyncData::class.java)
                    val syncData = adapter.fromJson(serializedJson)
                    Result.success(syncData)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
        try {
            val documentPath = "users/${currentUid}/slots/${slotId}"
            val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/$documentPath?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $idToken")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val respJson = JSONObject(bodyStr)
                    val fields = respJson.optJSONObject("fields") ?: return@use Result.success(null)
                    val jsonDataObj = fields.optJSONObject("jsonData") ?: return@use Result.success(null)
                    val serializedJson = jsonDataObj.optString("stringValue") ?: return@use Result.success(null)

                    if (serializedJson.isBlank()) return@use Result.success(null)

                    val adapter = moshi.adapter(SlotSyncData::class.java)
                    val syncData = adapter.fromJson(serializedJson)
                    Result.success(syncData)
                } else if (response.code == 404) {
                    // Document doesn't exist yet on cloud
                    Result.success(null)
                } else {
                    if (bodyStr.contains("API key", ignoreCase = true) || bodyStr.contains("API_KEY", ignoreCase = true)) {
                        val serializedJson = prefs.getString("local_slot_backup_$slotId", null)
                        if (serializedJson.isNullOrBlank()) {
                            Result.success(null)
                        } else {
                            val adapter = moshi.adapter(SlotSyncData::class.java)
                            val syncData = adapter.fromJson(serializedJson)
                            Result.success(syncData)
                        }
                    } else {
                        Result.failure(Exception("Download failed: ${response.code} ${response.message}\n$bodyStr"))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Download failed", e)
            Result.failure(e)
        }
    }

    suspend fun prepareSlotSyncData(slotId: String, repository: AppRepository): SlotSyncData = withContext(Dispatchers.IO) {
        val slot = repository.getSaveSlotById(slotId)
        val teams = repository.getTeamsForSlotSync(slotId)
        val players = repository.getPlayersForSlotSync(slotId)
        val coaches = repository.getCoachesForSlotSync(slotId)
        val sponsors = repository.getSponsorsForSlotSync(slotId)
        val finances = repository.getFinancesForSlotSync(slotId)
        val matches = repository.getMatchesForSlotSync(slotId)
        val news = repository.getNewsForSlotSync(slotId)
        val notifications = repository.getNotificationsForSlotSync(slotId)
        val trophies = repository.getTrophiesForSlotSync(slotId)
        val transfers = repository.getTransfersForSlotSync(slotId)

        SlotSyncData(
            slot = slot,
            teams = teams,
            players = players,
            coaches = coaches,
            sponsors = sponsors,
            finances = finances,
            matches = matches,
            news = news,
            notifications = notifications,
            trophies = trophies,
            transfers = transfers
        )
    }

    suspend fun restoreSlotSyncData(data: SlotSyncData, repository: AppRepository) = withContext(Dispatchers.IO) {
        val slot = data.slot ?: return@withContext
        
        // First clear local data to avoid overlaps/stale primary keys
        repository.deleteFullSlotData(slot.slotId)
        
        // Re-insert slot metadata
        repository.insertSaveSlot(slot)
        
        // Batch insert remaining entities
        if (data.teams.isNotEmpty()) repository.insertTeams(data.teams)
        if (data.players.isNotEmpty()) repository.insertPlayers(data.players)
        if (data.coaches.isNotEmpty()) repository.insertCoaches(data.coaches)
        if (data.sponsors.isNotEmpty()) repository.insertSponsors(data.sponsors)
        if (data.finances.isNotEmpty()) repository.insertFinances(data.finances)
        if (data.matches.isNotEmpty()) repository.insertMatches(data.matches)
        if (data.news.isNotEmpty()) repository.insertNewsList(data.news)
        
        // Insert lists that have custom single insert methods
        for (trophy in data.trophies) {
            repository.insertTrophy(trophy)
        }
        for (transfer in data.transfers) {
            repository.insertTransfer(transfer)
        }
        for (notif in data.notifications) {
            repository.insertNotification(notif)
        }
    }

    private fun parseAuthError(responseBody: String): String {
        return try {
            val json = JSONObject(responseBody)
            val error = json.getJSONObject("error")
            val msg = error.getString("message")
            when (msg) {
                "EMAIL_EXISTS" -> "This email address is already in use."
                "INVALID_EMAIL" -> "Please enter a valid email address."
                "EMAIL_NOT_FOUND", "INVALID_PASSWORD" -> "Invalid email or password."
                "WEAK_PASSWORD" -> "Password must be at least 6 characters."
                "API_KEY_INVALID" -> "API key not valid on server (Switching to Demo Mode)."
                else -> msg.replace("_", " ")
            }
        } catch (e: Exception) {
            "An error occurred. Please check your internet connection."
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: FirebaseSyncManager? = null

        fun getInstance(context: Context): FirebaseSyncManager {
            return INSTANCE ?: synchronized(this) {
                val instance = FirebaseSyncManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
