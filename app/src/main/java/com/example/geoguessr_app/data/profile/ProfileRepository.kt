package com.example.geoguessr_app.data.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.example.geoguessr_app.data.dailyquest.DailyQuestRepository
import com.example.geoguessr_app.data.statistics.StatisticsRepository
import com.example.geoguessr_app.domain.model.profile.PlayerProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

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
            // Explizites Map-Mapping um Naming-Fehler in Firebase zu vermeiden
            val profileMap = mapOf(
                "playerId" to profile.playerId,
                "playerName" to profile.playerName,
                "profileImageUrl" to profile.profileImageUrl,
                "createdAt" to profile.createdAt
            )
            
            database.reference
                .child("users")
                .child(uid)
                .child("profile")
                .setValue(profileMap)
                .await()
            
            _profile.value = profile
            _isLoaded.value = true
            saveUidLocally(uid)
            Log.d("PROFILE", "Profil gespeichert für $uid. Bild-URL vorhanden: ${profile.profileImageUrl != null}")
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
            
            val profileSnapshot = snapshot
            if (profileSnapshot.exists()) {
                val pId = profileSnapshot.child("playerId").getValue(String::class.java) ?: uid
                val pName = profileSnapshot.child("playerName").getValue(String::class.java) ?: "Spieler"
                
                // Auch hier: Fallback-Check
                val pImageUrl = profileSnapshot.child("profileImageUrl").getValue(String::class.java)
                    ?: profileSnapshot.child("profile_image_url").getValue(String::class.java)
                    
                val pCreatedAt = profileSnapshot.child("createdAt").getValue(Long::class.java) ?: 0L
                
                val profile = PlayerProfile(pId, pName, pImageUrl, pCreatedAt)
                _profile.value = profile
                
                // Registrierter User: Cloud-Statistiken laden
                StatisticsRepository.loadStatistics(uid)
                DailyQuestRepository.loadQuests()
                Log.d("PROFILE", "Profil geladen: $pName, Bild vorhanden: ${pImageUrl != null}")
            } else {
                // Gast ohne Profil: Statistiken leeren/lokal halten
                StatisticsRepository.clearLocalStatistics()
                DailyQuestRepository.resetQuests()
                Log.d("PROFILE", "Gast-Modus: Starte mit leeren Statistiken.")
            }
            _isLoaded.value = true
        } catch (e: Exception) {
            Log.e("PROFILE", "Fehler beim Laden des Profils", e)
            _isLoaded.value = true 
        }
    }

    suspend fun loginWithUsername(userName: String): Boolean {
        Log.d("PROFILE", "Login Versuch mit: $userName")
        StatisticsRepository.clearLocalStatistics()
        DailyQuestRepository.resetQuests()
        
        try {
            if (auth.currentUser == null) auth.signInAnonymously().await()

            val snapshot = database.reference.child("users").get().await()
            
            for (userSnapshot in snapshot.children) {
                val profileSnapshot = userSnapshot.child("profile")
                val name = profileSnapshot.child("playerName").getValue(String::class.java)
                
                if (name?.trim()?.equals(userName.trim(), ignoreCase = true) == true) {
                    val foundUid = userSnapshot.key ?: continue
                    
                    // Manuelles Auslesen mit Fallback für verschiedene Benennungen
                    val pId = profileSnapshot.child("playerId").getValue(String::class.java) ?: foundUid
                    val pName = profileSnapshot.child("playerName").getValue(String::class.java) ?: name
                    
                    // Prüfe beide gängigen Varianten für den Feldnamen
                    val pImageUrl = profileSnapshot.child("profileImageUrl").getValue(String::class.java) 
                        ?: profileSnapshot.child("profile_image_url").getValue(String::class.java)
                    
                    val pCreatedAt = profileSnapshot.child("createdAt").getValue(Long::class.java) ?: 0L
                    
                    val foundProfile = PlayerProfile(pId, pName, pImageUrl, pCreatedAt)
                    
                    Log.d("PROFILE", "User gefunden! Name: $pName, URL vorhanden: ${pImageUrl != null}")
                    _profile.value = foundProfile
                    _isLoaded.value = true
                    saveUidLocally(foundUid)
                    StatisticsRepository.loadStatistics(foundUid)
                    DailyQuestRepository.loadQuests()
                    return true
                }
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
        StatisticsRepository.clearLocalStatistics()
        DailyQuestRepository.resetQuests()
        // Wir bleiben in Firebase anonym angemeldet, aber löschen die Profil-Verknüpfung
    }

    suspend fun createAndLogin(userName: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        // Bei Neuerstellung Statistiken und Quests leeren
        StatisticsRepository.clearLocalStatistics()
        DailyQuestRepository.resetQuests()
        
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
        
        Log.d("PROFILE", "Upload (optimiert) gestartet für UID: $uid")
        try {
            // 1. Bild laden und komprimieren (Speicherplatz sparen!)
            val compressedBytes = compressImage(uri) ?: return null

            // 2. Lokales Profil sofort mit URI aktualisieren (für sofortige Anzeige)
            if (currentProfile != null) {
                _profile.value = currentProfile.copy(profileImageUrl = uri.toString())
            }

            // 3. Upload der komprimierten Bytes zu Firebase
            val ref = storage.reference.child("profile_pictures/$uid.jpg")
            ref.putBytes(compressedBytes).await()
            
            val downloadUrl = ref.downloadUrl.await().toString()
            Log.d("PROFILE", "Upload erfolgreich, URL: $downloadUrl")
            
            // 4. NUR die Bild-URL in DB aktualisieren (punktuelles Update ist sicherer)
            database.reference
                .child("users")
                .child(uid)
                .child("profile")
                .child("profileImageUrl")
                .setValue(downloadUrl)
                .await()

            // Lokalen State finalisieren
            if (currentProfile != null) {
                _profile.value = currentProfile.copy(profileImageUrl = downloadUrl)
            }
            return downloadUrl
        } catch (e: Exception) {
            Log.e("PROFILE", "Upload Fehler", e)
            return null
        }
    }

    private fun compressImage(uri: Uri): ByteArray? {
        return try {
            val contentResolver = appContext?.contentResolver ?: return null
            
            // 1. Nur Metadaten lesen, um Größe zu prüfen (Memory sparen)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            
            // 2. Berechne SampleSize (Downsampling beim Laden)
            val maxSize = 512
            var inSampleSize = 1
            if (options.outHeight > maxSize || options.outWidth > maxSize) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= maxSize && halfWidth / inSampleSize >= maxSize) {
                    inSampleSize *= 2
                }
            }

            // 3. Bild mit Downsampling laden
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            val sampledBitmap = contentResolver.openInputStream(uri)?.use { 
                BitmapFactory.decodeStream(it, null, decodeOptions) 
            } ?: return null
            
            // 4. Exakte Skalierung auf max 512px
            val width = sampledBitmap.width
            val height = sampledBitmap.height
            val finalBitmap = if (width > maxSize || height > maxSize) {
                val ratio = width.toFloat() / height.toFloat()
                var finalWidth = maxSize
                var finalHeight = maxSize
                if (width > height) {
                    finalHeight = (maxSize / ratio).toInt()
                } else {
                    finalWidth = (maxSize * ratio).toInt()
                }
                Bitmap.createScaledBitmap(sampledBitmap, finalWidth, finalHeight, true)
            } else {
                sampledBitmap
            }

            val outputStream = ByteArrayOutputStream()
            // JPEG mit 70% Qualität: Extrem klein, sieht aber gut aus
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val result = outputStream.toByteArray()
            
            Log.d("PROFILE", "Bild optimiert: ${result.size / 1024} KB (Sample: $inSampleSize)")
            result
        } catch (e: Exception) {
            Log.e("PROFILE", "Fehler bei Bild-Optimierung", e)
            null
        }
    }

    fun updateLocalProfile(profile: PlayerProfile) {
        _profile.value = profile
    }
}
