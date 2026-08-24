package com.example.geoguessr_app.data.profile

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

    suspend fun saveProfile(profile: PlayerProfile) {
        val uid = auth.currentUser?.uid ?: return
        try {
            database.reference
                .child("users")
                .child(uid)
                .child("profile")
                .setValue(profile)
                .await()
            
            _profile.value = profile
            _isLoaded.value = true
            Log.d("PROFILE", "Profil erfolgreich gespeichert")
        } catch (e: Exception) {
            Log.e("PROFILE", "Fehler beim Speichern des Profils", e)
        }
    }

    suspend fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return
        try {
            val snapshot = database.reference
                .child("users")
                .child(uid)
                .child("profile")
                .get()
                .await()
            
            val profile = snapshot.getValue(PlayerProfile::class.java)
            _profile.value = profile
            _isLoaded.value = true
            Log.d("PROFILE", "Profil geladen: ${profile?.playerName}")
        } catch (e: Exception) {
            Log.e("PROFILE", "Fehler beim Laden des Profils", e)
            _isLoaded.value = true // Auch bei Fehler als "versucht zu laden" markieren
        }
    }

    suspend fun loginWithUsername(userName: String): Boolean {
        try {
            val snapshot = database.reference.child("users").get().await()
            
            var foundUid: String? = null
            var foundProfile: PlayerProfile? = null
            
            for (userSnapshot in snapshot.children) {
                val profile = userSnapshot.child("profile").getValue(PlayerProfile::class.java)
                if (profile?.playerName?.equals(userName, ignoreCase = true) == true) {
                    foundUid = userSnapshot.key
                    foundProfile = profile
                    break
                }
            }
            
            if (foundUid != null && foundProfile != null) {
                _profile.value = foundProfile
                _isLoaded.value = true
                StatisticsRepository.loadStatistics(foundUid)
                return true
            }
            return false
        } catch (e: Exception) {
            Log.e("PROFILE", "Login Fehler", e)
            return false
        }
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
        val uid = auth.currentUser?.uid ?: return null
        try {
            val ref = storage.reference.child("profile_pictures/$uid.jpg")
            
            // Datei hochladen
            ref.putFile(uri).await()
            
            // Download URL abrufen
            val downloadUrl = ref.downloadUrl.await().toString()
            
            // Profil aktualisieren
            val current = _profile.value
            if (current != null) {
                val updated = current.copy(profileImageUrl = downloadUrl)
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
