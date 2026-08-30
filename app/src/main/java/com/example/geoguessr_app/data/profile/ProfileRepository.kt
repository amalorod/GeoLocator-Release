package com.example.geoguessr_app.data.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.example.geoguessr_app.data.firebase.FirebaseAuthRepository
import com.example.geoguessr_app.domain.model.profile.PlayerProfile
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Verwaltet das Nutzerprofil sowie den Login-Zustand der Anwendung.
 *
 * ARCHITEKTUR-HINWEIS ZUR ORCHESTRIERUNG: Diese Klasse ist bewusst NICHT
 * mehr für das Laden von Statistiken oder Daily Quests verantwortlich,
 * um eine zirkuläre Abhängigkeit zu StatisticsRepository/
 * DailyQuestRepository zu vermeiden (beide würden ihrerseits den
 * aktuellen Profilzustand benötigen). Diese Orchestrierung – z. B. nach
 * einem erfolgreichen loadProfile()/logout()/createAndLogin() zusätzlich
 * die zugehörigen Statistiken bzw. Quests nachzuladen – liegt ab jetzt
 * ausschließlich im aufrufenden ViewModel-Layer (siehe ProfileViewModel).
 * WICHTIG: Jeder Aufrufer dieser Methoden muss diese Nachbereitung
 * selbst übernehmen, siehe Methodendokumentation weiter unten.
 */
@Singleton
class ProfileRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: FirebaseAuthRepository,
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage,
) {

    private val _profile = MutableStateFlow<PlayerProfile?>(null)
    val profile: StateFlow<PlayerProfile?> = _profile.asStateFlow()

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    private fun getSavedUid(): String? {
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            ?.getString("active_uid", null)
    }

    private fun saveUidLocally(uid: String?) {
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            ?.edit()?.putString("active_uid", uid)?.apply()
    }

    suspend fun saveProfile(profile: PlayerProfile) {
        val uid = profile.playerId.ifEmpty { authRepository.currentUid() } ?: return
        try {
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
            Log.d(
                "PROFILE",
                "Profil gespeichert für $uid. Bild-URL vorhanden: ${profile.profileImageUrl != null}"
            )
        } catch (e: Exception) {
            Log.e("PROFILE", "Fehler beim Speichern des Profils", e)
        }
    }

    /**
     * Lädt beim App-Start den zuletzt aktiven Zustand rein bezogen auf
     * das Profil.
     *
     * ARCHITEKTUR-ENTSCHEIDUNG: Der Login-Status wird ausschließlich
     * über den lokal gespeicherten active_uid-Wert bestimmt (siehe
     * getSavedUid()), NICHT über authRepository.currentUid(). Der Grund:
     * Firebase Anonymous Authentication persistiert eine Sitzung
     * geräteweit über App-Neustarts hinweg, bis explizit signOut()
     * aufgerufen wird. Würde man sich stattdessen auf currentUid()
     * verlassen, bliebe ein Nutzer nach einem expliziten Logout beim
     * nächsten App-Start unerwartet wieder angemeldet, analog zu
     * klassischem Web-Login (siehe Banking-Apps).
     *
     * WICHTIG FÜR AUFRUFER (siehe Klassendokumentation): Nach dieser
     * Methode MUSS im ViewModel-Layer zusätzlich abhängig vom
     * resultierenden profile.value entweder
     * StatisticsRepository.loadLocalStatistics()/DailyQuestRepository.resetQuests()
     * (Gast-Fall, profile.value == null) oder
     * StatisticsRepository.loadStatistics(uid)/DailyQuestRepository.loadQuests()
     * (Login-Fall) aufgerufen werden, da diese Klasse selbst keine
     * Kenntnis von den anderen Repositories besitzt.
     */
    suspend fun loadProfile() {
        try {
            val uid = getSavedUid()

            if (uid == null) {
                // Kein gespeicherter Login-Zustand: App startet
                // bewusst im Gast-Modus, unabhängig davon, ob Firebase
                // im Hintergrund noch eine alte anonyme Sitzung hält.
                _profile.value = null
                return
            }

            val snapshot = database.reference
                .child("users")
                .child(uid)
                .child("profile")
                .get()
                .await()

            if (snapshot.exists()) {
                val pId = snapshot.child("playerId").getValue(String::class.java) ?: uid
                val pName = snapshot.child("playerName").getValue(String::class.java) ?: "Spieler"
                val pImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)
                    ?: snapshot.child("profile_image_url").getValue(String::class.java)
                val pCreatedAt = snapshot.child("createdAt").getValue(Long::class.java) ?: 0L

                _profile.value = PlayerProfile(pId, pName, pImageUrl, pCreatedAt)
            } else {
                // Gespeicherte UID verweist auf kein (mehr) gültiges
                // Profil – Rückfall auf den Gast-Modus statt eines
                // Fehlzustands.
                saveUidLocally(null)
                _profile.value = null
            }
        } catch (e: Exception) {
            Log.e("PROFILE", "Fehler beim Laden des Profils", e)
        } finally {
            _isLoaded.value = true
        }
    }

    /**
     * Sucht ein bestehendes Profil anhand des Spielernamens und meldet
     * den Nutzer bei Erfolg an.
     *
     * WICHTIG FÜR AUFRUFER: Nach erfolgreichem Login (return true)
     * muss das ViewModel-Layer zusätzlich
     * StatisticsRepository.loadStatistics(uid) und
     * DailyQuestRepository.loadQuests() aufrufen (siehe
     * Klassendokumentation). Lokale Gast-Statistiken werden hierbei
     * bewusst NICHT gelöscht, da sie unabhängig vom Account-Wechsel
     * für einen späteren Logout weiter bestehen sollen.
     */
    suspend fun loginWithUsername(userName: String): Boolean {
        Log.d("PROFILE", "Login Versuch mit: $userName")

        try {
            if (authRepository.currentUid() == null) authRepository.signInAnonymously()

            val snapshot = database.reference.child("users").get().await()

            for (userSnapshot in snapshot.children) {
                val profileSnapshot = userSnapshot.child("profile")
                val name = profileSnapshot.child("playerName").getValue(String::class.java)

                if (name?.trim()?.equals(userName.trim(), ignoreCase = true) == true) {
                    val foundUid = userSnapshot.key ?: continue

                    val pId =
                        profileSnapshot.child("playerId").getValue(String::class.java) ?: foundUid
                    val pName =
                        profileSnapshot.child("playerName").getValue(String::class.java) ?: name
                    val pImageUrl =
                        profileSnapshot.child("profileImageUrl").getValue(String::class.java)
                            ?: profileSnapshot.child("profile_image_url")
                                .getValue(String::class.java)
                    val pCreatedAt =
                        profileSnapshot.child("createdAt").getValue(Long::class.java) ?: 0L

                    val foundProfile = PlayerProfile(pId, pName, pImageUrl, pCreatedAt)

                    Log.d(
                        "PROFILE",
                        "User gefunden! Name: $pName, URL vorhanden: ${pImageUrl != null}"
                    )
                    _profile.value = foundProfile
                    _isLoaded.value = true
                    saveUidLocally(foundUid)
                    return true
                }
            }
            return false
        } catch (e: Exception) {
            Log.e("PROFILE", "Login Fehler", e)
            return false
        }
    }

    /**
     * Beendet die Sitzung des angemeldeten Nutzers vollständig und
     * echt, analog zu klassischen Login-Systemen wie Online-Banking:
     * Es wird tatsächlich authRepository.signOut() aufgerufen, wodurch
     * die Firebase-Sitzung beendet wird und beim nächsten App-Start
     * nicht automatisch wieder aktiv ist.
     *
     * WICHTIG FÜR AUFRUFER: Diese Methode setzt NUR den Profilzustand
     * zurück. Das ViewModel-Layer muss danach zusätzlich
     * StatisticsRepository.loadLocalStatistics() und
     * DailyQuestRepository.resetQuests() aufrufen, damit die App in
     * den Gast-Modus mit den zuvor lokal persistierten Gast-Daten
     * wechselt (siehe Klassendokumentation). Ohne diesen Folgeaufruf
     * bliebe der Statistik-State fälschlich auf dem Stand des zuvor
     * angemeldeten Accounts stehen.
     */
    fun logout() {
        saveUidLocally(null)
        _profile.value = null
        authRepository.signOut()
    }

    /**
     * Erstellt ein neues Profil und meldet den Nutzer damit an.
     *
     * Da nach einem echten Logout keine anonyme Firebase-Sitzung mehr
     * existiert (siehe logout()), wird hier bei Bedarf eine neue
     * anonyme Sitzung erzeugt, bevor ein Profil unter der zugehörigen
     * UID angelegt werden kann.
     *
     * WICHTIG FÜR AUFRUFER: Nach erfolgreichem Aufruf (return true)
     * muss das ViewModel-Layer zusätzlich
     * StatisticsRepository.loadStatistics(uid) aufrufen. Ohne diesen
     * Aufruf würde der In-Memory-Statistikzustand weiterhin die zuvor
     * im Gast-Modus gesammelten Werte enthalten, und die erste Partie
     * des neuen Accounts würde fälschlich mit alten Gast-Statistiken
     * vermischt in Firebase gespeichert (siehe Klassendokumentation).
     */
    suspend fun createAndLogin(userName: String): Boolean {
        if (authRepository.currentUid() == null) authRepository.signInAnonymously()
        val uid = authRepository.currentUid() ?: return false

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
        val uid = currentProfile?.playerId ?: authRepository.currentUid() ?: return null

        Log.d("PROFILE", "Upload (optimiert) gestartet für UID: $uid")
        try {
            val compressedBytes = compressImage(uri) ?: return null

            if (currentProfile != null) {
                _profile.value = currentProfile.copy(profileImageUrl = uri.toString())
            }

            val ref = storage.reference.child("profile_pictures/$uid.jpg")
            ref.putBytes(compressedBytes).await()

            val downloadUrl = ref.downloadUrl.await().toString()
            Log.d("PROFILE", "Upload erfolgreich, URL: $downloadUrl")

            database.reference
                .child("users")
                .child(uid)
                .child("profile")
                .child("profileImageUrl")
                .setValue(downloadUrl)
                .await()

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
            val contentResolver = context.contentResolver ?: return null

            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            contentResolver.openInputStream(uri)
                ?.use { BitmapFactory.decodeStream(it, null, options) }

            val maxSize = 512
            var inSampleSize = 1
            if (options.outHeight > maxSize || options.outWidth > maxSize) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= maxSize && halfWidth / inSampleSize >= maxSize) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            val sampledBitmap: Bitmap = contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOptions)
            } ?: return null

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