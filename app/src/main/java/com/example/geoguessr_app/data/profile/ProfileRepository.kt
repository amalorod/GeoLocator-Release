package com.example.geoguessr_app.data.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.geoguessr_app.data.statistics.StatisticsRepository
import com.example.geoguessr_app.domain.model.profile.PlayerProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

object ProfileRepository {

    private const val DB_URL = "https://bsi-geoguessr-app-63b7f-default-rtdb.europe-west1.firebasedatabase.app/"
    private val database = FirebaseDatabase.getInstance(DB_URL)
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _profile = MutableStateFlow<PlayerProfile?>(null)
    val profile: StateFlow<PlayerProfile?> = _profile.asStateFlow()

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    private fun getSavedUid(): String? {
        return appContext?.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            ?.getString("active_uid", null)
    }

    private fun saveUidLocally(uid: String?) {
        appContext?.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            ?.edit()?.putString("active_uid", uid)?.apply()
    }

    suspend fun saveProfile(profile: PlayerProfile) {
        val uid = profile.playerId.ifEmpty { auth.currentUser?.uid } ?: return
        try {
            database.reference
                .child("users")
                .child(uid)
                .child("profile")
                .setValue(profile)
                .await()
            
            _profile.value = profile
            _isLoaded.value = true
            saveUidLocally(uid)
            Log.d("PROFILE", "Profil erfolgreich gespeichert für UID: $uid")
        } catch (e: Exception) {
            Log.e("PROFILE", "Fehler beim Speichern des Profils", e)
        }
    }

    suspend fun loadProfile() {
        try {
            val uid = getSavedUid() ?: auth.currentUser?.uid ?: run {
                val result = auth.signInAnonymously().await()
                result.user?.uid
            } ?: return

            val snapshot = database.reference
                .child("users")
                .child(uid)
                .child("profile")
                .get()
                .await()
            
            val profile = snapshot.getValue(PlayerProfile::class.java)
            if (profile != null) {
                _profile.value = profile
                StatisticsRepository.loadStatistics(uid)
                Log.d("PROFILE", "Profil geladen: ${profile.playerName}")
            }
            _isLoaded.value = true
        } catch (e: Exception) {
            Log.e("PROFILE", "Fehler beim Laden des Profils", e)
            _isLoaded.value = true 
        }
    }

    suspend fun loginWithUsername(userName: String): Boolean {
        Log.d("PROFILE", "Login Versuch mit: $userName")
        try {
            // Erstmal sicherstellen, dass wir eine UID haben
            if (auth.currentUser == null) {
                auth.signInAnonymously().await()
            }

            val snapshot = database.reference.child("users").get().await()
            
            var foundUid: String? = null
            var foundProfile: PlayerProfile? = null
            
            for (userSnapshot in snapshot.children) {
                val profileData = userSnapshot.child("profile")
                val name = profileData.child("playerName").getValue(String::class.java)
                
                if (name?.trim()?.equals(userName.trim(), ignoreCase = true) == true) {
                    foundUid = userSnapshot.key
                    // Wichtig: Explizites Mapping um Felder wie profileImageUrl sicher zu laden
                    foundProfile = profileData.getValue(PlayerProfile::class.java)
                    break
                }
            }
            
            if (foundUid != null && foundProfile != null) {
                Log.d("PROFILE", "Nutzer gefunden: $foundUid, Bild: ${foundProfile.profileImageUrl}")
                _profile.value = foundProfile
                _isLoaded.value = true
                saveUidLocally(foundUid)
                StatisticsRepository.loadStatistics(foundUid)
                return true
            }
            return false
        } catch (e: Exception) {
            Log.e("PROFILE", "Login Fehler", e)
            return false
        }
    }

    suspend fun logout() {
        saveUidLocally(null)
        _profile.value = null
        // Wir bleiben in Firebase anonym angemeldet, aber löschen die Profil-Verknüpfung
    }

    suspend fun createAndLogin(userName: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val newProfile = PlayerProfile(
            playerId = uid,
            playerName = userName,
            createdAt = System.currentTimeMillis()
        )
        saveProfile(newProfile)
        return true
    }

    suspend fun uploadProfilePicture(uri: Uri): String? {
        val currentProfile = _profile.value
        val uid = currentProfile?.playerId ?: auth.currentUser?.uid ?: return null
        
        Log.d("PROFILE", "Upload gestartet für UID: $uid")
        try {
            // 1. Lokales Profil sofort mit URI aktualisieren
            if (currentProfile != null) {
                _profile.value = currentProfile.copy(profileImageUrl = uri.toString())
            }

            // 2. Upload zu Firebase
            val ref = storage.reference.child("profile_pictures/$uid.jpg")
            ref.putFile(uri).await()
            
            val downloadUrl = ref.downloadUrl.await().toString()
            Log.d("PROFILE", "Upload erfolgreich, URL: $downloadUrl")
            
            // 3. Finale URL in DB speichern (immer am richtigen Ort!)
            if (currentProfile != null) {
                val updated = currentProfile.copy(profileImageUrl = downloadUrl)
                saveProfile(updated)
            }
            return downloadUrl
        } catch (e: Exception) {
            Log.e("PROFILE", "Upload Fehler", e)
            return null
        }
    }

    fun updateLocalProfile(profile: PlayerProfile) {
        _profile.value = profile
    }
}
