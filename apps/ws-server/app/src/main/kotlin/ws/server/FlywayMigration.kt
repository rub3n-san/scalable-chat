package ws.server

import org.flywaydb.core.Flyway
import java.util.*

class FlywayMigration(properties: Properties) {
    val url = properties.getProperty("database.postgres.url", "jdbc:postgresql://localhost:5432/postgres")
    val user = properties.getProperty("database.postgres.user", "postgres")
    val password = properties.getProperty("database.postgres.password", "XXXXX")
    fun migrate() {
        println("Starting flyway postgres migration...")
        val flyway = Flyway.configure()
            .locations("classpath:db/migration")
            .dataSource(
                url,
                user,
                password
            )
            .load()
        flyway.migrate()
        println("Migration completed.")
    }
}