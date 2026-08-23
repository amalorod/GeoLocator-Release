package com.example.geoguessr_app.domain.util

import kotlin.random.Random

object LobbyCodeGenerator {

    private const val CHARSET =
        "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    fun generate(): String {

        return buildString {

            repeat(6) {

                append(
                    CHARSET[
                        Random.nextInt(
                            CHARSET.length
                        )
                    ]
                )
            }
        }
    }
}