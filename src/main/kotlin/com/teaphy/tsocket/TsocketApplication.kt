package com.teaphy.tsocket

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TsocketApplication

fun main(args: Array<String>) {
    runApplication<TsocketApplication>(*args)
}
