package com.example

import com.example.Routes.RoutePayment
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    val server = embeddedServer(Netty, port = 8080) {
        routing {

            get("/") {
                call.respondText("Server On!", ContentType.Text.Plain)
            }

            RoutePayment()

        }
    }
    server.start(wait = true)
}

