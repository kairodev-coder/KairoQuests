package dev.kairo.kairoquests.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.plugin.java.JavaPlugin
import java.sql.Connection

class DatabaseManager(private val plugin: JavaPlugin) {
    private lateinit var dataSource: HikariDataSource

    fun connect() {
        plugin.dataFolder.mkdirs()
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:sqlite:${plugin.dataFolder.resolve("kairoquests.db").absolutePath}"
            driverClassName = "org.sqlite.JDBC"
            maximumPoolSize = 4
            poolName = "KairoQuests"
            addDataSourceProperty("foreign_keys", "true")
        }
        dataSource = HikariDataSource(config)
    }

    fun connection(): Connection = dataSource.connection

    fun close() {
        if (::dataSource.isInitialized) {
            dataSource.close()
        }
    }
}
