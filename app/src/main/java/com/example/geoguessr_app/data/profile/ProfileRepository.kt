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

/**
 * Verwaltet das Nutzerprofil sowie den Login-Zustand der Anwendung
 * (siehe Doku, Kapitel 5.6 „Profil-, Statistik- und Cloud-System“).
 *
 * ARCHITEKTUR-ENTSCHEIDUNG: Gast-Modus und angemeldeter Modus sind
 * als zwei unabhängige, parallele Zustände konzipiert. Gast-
 * Statistiken (siehe StatisticsRepository, lokal über DataStore
 * persistiert) werden bei einem Login NICHT gelöscht, da sie
 * inhaltlich nichts mit einem Firebase-Account zu tun haben und beim
 * nächsten Logout unverändert wieder verfügbar sein sollen.
 */
object ProfileRepository {

    private const val DB_URL =
        "https://bsi-geoguessr-app-63b7f-default-rtdb.europe-west1.firebasedatabase.app/"
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

    /**
     * Liest die zuletzt gespeicherte UID aus SharedPreferences – dies
     * ist die alleinige Quelle der Wahrheit für den Login-Zustand
     * (siehe loadProfile).
     */
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
            Log.d(
                "PROFILE",
                "Profil gespeichert für $uid. Bild-URL vorhanden: ${profile.profileImageUrl != null}"
            )
        } catch (e: Exception) {
            Log.e("PROFILE", "Fehler beim Speichern des Profils", e)
        }
    }

    /**
     * Lädt beim App-Start den zuletzt aktiven Zustand.
     *
     * ARCHITEKTUR-ENTSCHEIDUNG: Der Login-Status wird ausschließlich
     * über den lokal gespeicherten active_uid-Wert bestimmt (siehe
     * getSavedUid()), NICHT über auth.currentUser. Der Grund: Firebase
     * Anonymous Authentication persistiert eine Sitzung geräteweit
     * über App-Neustarts hinweg, bis explizit auth.signOut() aufgerufen
     * wird. Würde man sich stattdessen auf auth.currentUser verlassen,
     * bliebe ein Nutzer nach einem expliziten Logout beim nächsten
     * App-Start unerwartet wieder angemeldet, analog zu klassischem
     * Web-Login (siehe Banking-Apps): Ein expliziter Logout beendet
     * die Sitzung dauerhaft, bis sich der Nutzer erneut aktiv anmeldet.
     *
     * Aus demselben Grund wird im Gast-Fall keine automatische anonyme
     * Anmeldung mehr durchgeführt: Gast-Daten werden ausschließlich
     * über den lokalen DataStore verwaltet und benötigen keine
     * Firebase-Verbindung.
     */
    suspend fun loadProfile() {
        try {
            val uid = getSavedUid()

            if (uid == null) {
                // Kein gespeicherter Login-Zustand: App startet
                // bewusst im Gast-Modus, unabhängig davon, ob Firebase
                // im Hintergrund noch eine alte anonyme Sitzung hält.
                _profile.value = null
                StatisticsRepository.loadLocalStatistics()
                DailyQuestRepository.resetQuests()
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

                StatisticsRepository.loadStatistics(uid)
                DailyQuestRepository.loadQuests()
            } else {
                // Gespeicherte UID verweist auf kein (mehr) gültiges
                // Profil (z. B. nach externer Löschung in Firebase) –
                // Rückfall auf den Gast-Modus statt eines Fehlzustands.
                saveUidLocally(null)
                _profile.value = null
                StatisticsRepository.loadLocalStatistics()
                DailyQuestRepository.resetQuests()
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
     * Lokale Gast-Statistiken werden hierbei bewusst NICHT gelöscht
     * (siehe Klassendokumentation). Der In-Memory-Zustand von
     * [StatisticsRepository] wird stattdessen durch loadStatistics()
     * vollständig durch die Firebase-Daten des gefundenen Accounts
     * überschrieben.
     */
    suspend fun loginWithUsername(userName: String): Boolean {
        Log.d("PROFILE", "Login Versuch mit: $userName")
        DailyQuestRepository.resetQuests()

        try {
            if (auth.currentUser == null) auth.signInAnonymously().await()

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

    /**
     * Beendet die Sitzung des angemeldeten Nutzers vollständig und
     * echt, analog zu klassischen Login-Systemen wie Online-Banking:
     * Es wird tatsächlich auth.signOut() aufgerufen, wodurch die
     * Firebase-Sitzung beendet wird und beim nächsten App-Start nicht
     * automatisch wieder aktiv ist.
     *
     * Nach dem Logout wechselt die App in den Gast-Modus und lädt die
     * lokal persistierten Gast-Statistiken über loadLocalStatistics(),
     * die durch einen zwischenzeitlichen Login unangetastet blieben.
     */
    suspend fun logout() {
        saveUidLocally(null)
        _profile.value = null
        auth.signOut()
        StatisticsRepository.loadLocalStatistics()
        DailyQuestRepository.resetQuests()
    }

    /**
     * Erstellt ein neues Profil und meldet den Nutzer damit an.
     *
     * Da nach einem echten Logout keine anonyme Firebase-Sitzung mehr
     * existiert (siehe logout()), wird hier bei Bedarf eine neue
     * anonyme Sitzung erzeugt, bevor ein Profil unter der zugehörigen
     * UID angelegt werden kann.
     *
     * WICHTIGER FIX: Nach dem Speichern des Profils wird zusätzlich
     * StatisticsRepository.loadStatistics(uid) aufgerufen. Ohne diesen
     * Aufruf würde der In-Memory-Statistikzustand weiterhin die zuvor
     * im Gast-Modus gesammelten Werte enthalten (da diese nicht mehr
     * gelöscht werden, siehe Klassendokumentation), und die erste
     * Partie des neuen Accounts würde fälschlich mit den alten
     * Gast-Statistiken vermischt in Firebase gespeichert. loadStatistics()
     * lädt für den frischen Account korrekt einen leeren
     * LifetimeStatistics()-Standardwert, ohne den lokalen DataStore
     * der Gast-Statistik zu berühren.
     */
    suspend fun createAndLogin(userName: String): Boolean {
        if (auth.currentUser == null) auth.signInAnonymously().await()
        val uid = auth.currentUser?.uid ?: return false

        DailyQuestRepository.resetQuests()

        val newProfile = PlayerProfile(
            playerId = uid,
            playerName = userName,
            createdAt = System.currentTimeMillis()
        )
        saveProfile(newProfile)
        StatisticsRepository.loadStatistics(uid)
        return true
    }

    /**
     * Lädt ein neues Profilbild hoch, komprimiert es zuvor lokal, um
     * Speicherplatz in Firebase Storage zu sparen (siehe compressImage).
     *
     * Der lokale Zustand wird zweimal aktualisiert: zunächst optimistisch
     * mit der lokalen URI für eine sofortige UI-Reaktion, anschließend
     * final mit der tatsächlichen Firebase-Download-URL.
     */
    suspend fun uploadProfilePicture(uri: Uri): String? {
        val currentProfile = _profile.value
        val uid = currentProfile?.playerId ?: auth.currentUser?.uid ?: return null

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

            // Punktuelles Update nur des Bild-URL-Feldes, statt das
            // gesamte Profil erneut zu schreiben, um versehentliches
            // Überschreiben anderer, zwischenzeitlich geänderter
            // Felder zu vermeiden.
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

    /**
     * Komprimiert ein Bild auf maximal 512px Kantenlänge und speichert
     * es als JPEG mit 70% Qualität, um Upload-Zeit und Speicherverbrauch
     * in Firebase Storage gering zu halten.
     *
     * Nutzt ein zweistufiges Ladeverfahren: Zunächst werden nur die
     * Bildabmessungen ausgelesen (inJustDecodeBounds), um eine passende
     * SampleSize für das Downsampling zu berechnen, statt das
     * Originalbild vollständig in den Speicher zu laden und erst
     * danach zu verkleinern. Das reduziert den Speicherverbrauch
     * erheblich, insbesondere bei sehr hochauflösenden Kamerabildern.
     */
    private fun compressImage(uri: Uri): ByteArray? {
        return try {
            val contentResolver = appContext?.contentResolver ?: return null

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
            val sampledBitmap = contentResolver.openInputStream(uri)?.use {
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