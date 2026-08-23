package com.example.geoguessr_app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geoguessr_app.data.firebase.FirebaseAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseAuthRepository:
    FirebaseAuthRepository
) : ViewModel() {

    private val _firebaseUid =
        MutableStateFlow<String?>(null)

    val firebaseUid =
        _firebaseUid.asStateFlow()

    init {

        viewModelScope.launch {

            val uid =
                firebaseAuthRepository.currentUid()
                    ?: firebaseAuthRepository
                        .signInAnonymously()

            _firebaseUid.value = uid
        }
    }
}