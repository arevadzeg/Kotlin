import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import io.ktor.serialization.jackson.*
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import io.ktor.server.plugins.cors.routing.*


fun main() {
    val dbConnection = initDatabase() // Initialize the database

    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            jackson() // Use Jackson for JSON serialization
        }


        // Install the CORS plugin
        install(CORS) {
            anyHost() // Allow all origins (use with caution in production)
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.Authorization)
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
        }

        routing {
            // POST to create a new scan
            post("/scan") {
                val jsonBody = call.receiveText()
                println("Received request to create scan with body: $jsonBody")

                // Extract domain from JSON body
                val domain = try {
                    Json.parseToJsonElement(jsonBody).jsonObject["domain"]?.jsonPrimitive?.content
                } catch (e: Exception) {
                    println("Error parsing JSON: ${e.message}")
                    return@post call.respond(HttpStatusCode.BadRequest, "Invalid JSON format")
                }

                if (domain.isNullOrEmpty()) {
                    println("Missing domain in request")
                    return@post call.respond(HttpStatusCode.BadRequest, "Missing domain")
                }

                // Insert the scan into the database and get the response
                val scan = insertScan(dbConnection, domain)

                // Respond with the scan details in JSON
                call.respond(scan) // Respond with the serialized Scan object
            }

            // GET to fetch all scans
            get("/scans") {
                println("Received request to get all scans")
                val scans = getAllScans(dbConnection)

                if (scans.isEmpty()) {
                    println("No scans found in the database")
                    call.respond(HttpStatusCode.NotFound, "No scans found")
                } else {
                    println("Responding with scans: $scans")
                    call.respond(scans) // Respond with JSON array of scans
                }
            }

            // GET to fetch a scan by ID
            get("/scans/{id}") {
                val id = call.parameters["id"]
                println("Received request to get scan with ID: $id")

                if (id.isNullOrEmpty()) {
                    println("Missing ID in request")
                    call.respond(HttpStatusCode.BadRequest, "Missing ID")
                    return@get
                }

                val scan = getScanById(dbConnection, id)
                if (scan != null) {
                    println("Responding with scan details: $scan")
                    call.respond(scan) // Respond with the scan details as JSON
                } else {
                    println("No scan found with ID: $id")
                    call.respond(HttpStatusCode.NotFound, "Scan not found")
                }
            }
            // Handle sorting of scans using midpoint
            post("/scans/sort") {
                val requestBody = call.receive<Map<String, String>>()
                val scanId = requestBody["id"]
                val newSortOrder = requestBody["newSortOrder"]?.toFloat()

                if (scanId != null && newSortOrder != null) {
                    updateScanOrder(dbConnection, scanId, newSortOrder)
                    call.respond(HttpStatusCode.OK, "Scan order updated successfully")
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Invalid request")
                }
            }

        }
    }.start(wait = true)
}
