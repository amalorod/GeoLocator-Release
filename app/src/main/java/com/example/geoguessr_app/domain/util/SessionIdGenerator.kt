package com.example.geoguessr_app.domain.util

object SessionIdGenerator {

    fun generate(): String {

        return System.currentTimeMillis()
            .toString()
    }
}
