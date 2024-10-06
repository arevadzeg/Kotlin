import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.util.UUID
import java.time.LocalDateTime

// Define the Scan data class
data class Scan(
    val id: String,
    val domain: String,
    val startTime: String,
    val endTime: String,
    val sortOrder: Float // Add sortOrder here
)

// Initialize the SQLite database
fun initDatabase(): Connection {
    val url = "jdbc:sqlite:scans.db"
    return DriverManager.getConnection(url).apply {
        createStatement().execute(
            """
            CREATE TABLE IF NOT EXISTS scans (
                id TEXT PRIMARY KEY,
                domain TEXT NOT NULL,
                startTime TEXT NOT NULL,
                endTime TEXT NOT NULL,
                sortOrder INTEGER NOT NULL DEFAULT 0
            )
            """
        )
    }
}

// Function to get a scan by ID
fun getScanById(connection: Connection, id: String): Scan? {
    println("Fetching scan with ID: $id")
    val preparedStatement = connection.prepareStatement("SELECT * FROM scans WHERE id = ?")
    preparedStatement.setString(1, id)
    val resultSet = preparedStatement.executeQuery()

    return if (resultSet.next()) {
        val scan = Scan(
            id = resultSet.getString("id"),
            domain = resultSet.getString("domain"),
            startTime = resultSet.getString("startTime"),
            endTime = resultSet.getString("endTime"),
            sortOrder = resultSet.getFloat("sortOrder") // Fetch sortOrder from the result set

        )
        println("Found scan: $scan")
        scan
    } else {
        println("No scan found with ID: $id")
        null
    }
}

// Update the order of a scan based on the provided sortOrder (midpoint value)
fun updateScanOrder(connection: Connection, scanId: String, newSortOrder: Float) {
    val statement = connection.prepareStatement("UPDATE scans SET sortOrder = ? WHERE id = ?")
    statement.setFloat(1, newSortOrder)
    statement.setString(2, scanId)
    statement.executeUpdate()
}

// Function to insert a scan into the database
fun insertScan(connection: Connection, domain: String): Scan {
    val scanId = UUID.randomUUID().toString()
    val startTime = LocalDateTime.now()
    val endTime = startTime.plusMinutes(1)

    println("Inserting scan into database: id=$scanId, domain=$domain, startTime=$startTime, endTime=$endTime")

    // Insert the scan into the database
    connection.prepareStatement("INSERT INTO scans (id, domain, startTime, endTime) VALUES (?, ?, ?, ?)").use { stmt ->
        stmt.setString(1, scanId)
        stmt.setString(2, domain)
        stmt.setString(3, startTime.toString())
        stmt.setString(4, endTime.toString())
        stmt.executeUpdate()
    }

    return Scan(scanId, domain, startTime.toString(), endTime.toString(), 0f)
}

fun getAllScans(connection: Connection): List<Scan> {
    val statement = connection.createStatement()
    val resultSet: ResultSet = statement.executeQuery("SELECT * FROM scans ORDER BY sortOrder ASC")
    val scans = mutableListOf<Scan>()
    while (resultSet.next()) {
        scans.add(
            Scan(
                id = resultSet.getString("id"),
                domain = resultSet.getString("domain"),
                startTime = resultSet.getString("startTime"),
                endTime = resultSet.getString("endTime"),
                sortOrder = resultSet.getFloat("sortOrder") // Fetch sortOrder from the result set
            )
        )
    }
    return scans
}

