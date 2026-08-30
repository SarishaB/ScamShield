package com.bitwiseoperators.scamshield.db

import com.bitwiseoperators.scamshield.config.DatabaseConfig
import com.bitwiseoperators.scamshield.model.CommunityPost
import com.bitwiseoperators.scamshield.model.CommunityResult
import com.bitwiseoperators.scamshield.model.IndicatorType
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.security.MessageDigest
import java.sql.Connection

class Database(config: DatabaseConfig) {
    private val dataSource: HikariDataSource = HikariDataSource(
        HikariConfig().apply {
            jdbcUrl = config.jdbcUrl
            username = config.username
            password = config.password
            maximumPoolSize = config.maximumPoolSize
            driverClassName = "org.postgresql.Driver"
        }
    )

    fun connection(): Connection = dataSource.connection

    fun migrate() {
        connection().use { c ->
            c.createStatement().use { s ->
                s.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS community_reports (
                        id BIGSERIAL PRIMARY KEY,
                        indicator_hash CHAR(64) NOT NULL,
                        indicator TEXT NOT NULL,
                        indicator_type VARCHAR(20) NOT NULL,
                        category VARCHAR(80) NOT NULL,
                        description TEXT,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                    )
                    """.trimIndent()
                )

                s.executeUpdate(
                    """
                    CREATE INDEX IF NOT EXISTS idx_reports_indicator_hash
                    ON community_reports(indicator_hash)
                    """.trimIndent()
                )

                s.executeUpdate(
                    """
                    CREATE INDEX IF NOT EXISTS idx_reports_created_at
                    ON community_reports(created_at DESC)
                    """.trimIndent()
                )
            }
        }
    }

    fun addReport(
        indicator: String,
        type: String,
        category: String,
        description: String?
    ) {
        val normalized = normalize(indicator)
        val hash = sha256(normalized)

        connection().use { c ->
            c.prepareStatement(
                """
                INSERT INTO community_reports
                (indicator_hash, indicator, indicator_type, category, description)
                VALUES (?, ?, ?, ?, ?)
                """.trimIndent()
            ).use { ps ->
                ps.setString(1, hash)
                ps.setString(2, normalized.take(4000))
                ps.setString(3, type)
                ps.setString(4, category.take(80))
                ps.setString(5, description?.take(2000))
                ps.executeUpdate()
            }
        }
    }

    fun listCommunityPosts(): List<CommunityPost> {
        connection().use { c ->
            c.prepareStatement(
                """
                SELECT id, indicator, indicator_type, category, description, created_at
                FROM community_reports
                ORDER BY created_at DESC, id DESC
                """.trimIndent()
            ).use { ps ->
                ps.executeQuery().use { rs ->
                    val posts = mutableListOf<CommunityPost>()

                    while (rs.next()) {
                        val type = runCatching {
                            IndicatorType.valueOf(rs.getString("indicator_type"))
                        }.getOrDefault(IndicatorType.MESSAGE)

                        posts += CommunityPost(
                            id = rs.getLong("id"),
                            indicator = rs.getString("indicator"),
                            type = type,
                            category = rs.getString("category"),
                            description = rs.getString("description"),
                            createdAt = rs.getTimestamp("created_at").toInstant().toString()
                        )
                    }

                    return posts
                }
            }
        }
    }

    fun communityResult(indicator: String): CommunityResult {
        val normalized = normalize(indicator)
        val hash = sha256(normalized)

        connection().use { c ->
            c.prepareStatement(
                """
                SELECT COUNT(*), COALESCE(ARRAY_AGG(DISTINCT category), ARRAY[]::TEXT[])
                FROM community_reports
                WHERE indicator_hash = ?
                """.trimIndent()
            ).use { ps ->
                ps.setString(1, hash)

                ps.executeQuery().use { rs ->
                    rs.next()

                    val count = rs.getInt(1)
                    val categories =
                        (rs.getArray(2)?.array as? Array<*>)?.map { it.toString() }
                            ?: emptyList()

                    return CommunityResult(
                        reports = count,
                        corroborated = count >= 3,
                        categories = categories
                    )
                }
            }
        }
    }

    private fun normalize(value: String): String =
        value.trim().lowercase().replace(Regex("\\s+"), " ")

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }
}