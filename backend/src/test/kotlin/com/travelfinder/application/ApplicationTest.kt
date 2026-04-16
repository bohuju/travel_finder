package com.travelfinder.application

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {

    @Test
    fun `get pois returns seeded list`() = testApplication {
        application {
            module(AppDependencies.forTests())
        }

        val response = client.get("/api/pois")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("poi-west-lake"))
    }

    @Test
    fun `get poi detail returns not found when missing`() = testApplication {
        application {
            module(AppDependencies.forTests())
        }

        val response = client.get("/api/pois/missing")

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertTrue(response.bodyAsText().contains("POI not found"))
    }

    @Test
    fun `get trips expands poi details`() = testApplication {
        application {
            module(AppDependencies.forTests())
        }

        val response = client.get("/api/trips")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("杭州周末慢游"))
        assertTrue(response.bodyAsText().contains("西湖断桥"))
    }

    @Test
    fun `post trips creates a new trip`() = testApplication {
        application {
            module(AppDependencies.forTests())
        }

        val response = client.post("/api/trips") {
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(
                """
                {
                  "name": "上海夜景散步",
                  "days": 1,
                  "note": "先压一版最小 API",
                  "poiIds": ["poi-the-bund"]
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(response.bodyAsText().contains("上海夜景散步"))
        assertTrue(response.bodyAsText().contains("外滩观景步道"))
    }
}
