package xyz.podd.piholestats.service

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.RequestBody
import retrofit2.http.*
import xyz.podd.piholestats.model.*

interface PiHoleService {
	@GET("api.php?status")
	suspend fun getStatus(): Status

	@GET("api.php?summaryRaw")
	suspend fun getSummary(): Summary

	@GET("api.php")
	suspend fun getTopItems(@Query("topItems") count: Int = 10): TopItems

	@GET("api.php?topClients")
	suspend fun getTopClients(): TopClients

	@GET("api.php?topClientsBlocked")
	suspend fun getTopClientsBlocked(): TopClientsBlocked

	@GET("api.php?getAllQueries")
	suspend fun getQueries(): Queries

	@GET("api.php")
	suspend fun getQueries(@Query("getAllQueries") count: Int): Queries

	@GET("api.php?getAllQueries")
	suspend fun getQueriesByTime(@Query("from") from: Long, @Query("until") until: Long): Queries

	@GET("api.php?getAllQueries")
	suspend fun getQueriesByClient(@Query("client") client: String): Queries

	@POST("scripts/pi-hole/php/tailLog.php")
	suspend fun getTailLogHeight(@Body body: RequestBody): String
}

@Serializable
data class Status(val status: String) {
	val enabled
		get() = status == "enabled"
}

@Serializable
data class Summary(
	@SerialName("domains_being_blocked") val domainsInBlocklist: Int,
	@SerialName("dns_queries_today") val queriesToday: Int,
	@SerialName("ads_blocked_today") val blockedToday: Int,
	@SerialName("ads_percentage_today") val blockedTodayPercentage: Double,
	@SerialName("unique_domains") val uniqueDomains: Int,
	val status: String? = null
) {
	val enabled
		get() = status == "enabled"

	operator fun plus(other: Summary): Summary {
		val sumQueries = queriesToday + other.queriesToday
		val sumBlocked = blockedToday + other.blockedToday
		val blockedPercentage = sumBlocked.toDouble() / sumQueries

		return Summary(
			domainsInBlocklist + other.domainsInBlocklist,
			sumQueries,
			sumBlocked,
			blockedPercentage,
			uniqueDomains + other.uniqueDomains
		)
	}
}

@Serializable
data class TopItems(
	@SerialName("top_queries") val queries: Map<String, Int>,
	@SerialName("top_ads") val ads: Map<String, Int>
) {
	operator fun plus(other: TopItems): TopItems = TopItems(
		merge(queries, other.queries),
		merge(ads, other.ads)
	)
}

@Serializable
data class TopClients(
	@SerialName("top_sources")
	@Serializable(with = ClientQueriesSerializer::class)
	val stats: Map<Client, ClientStats>
)

@Serializable
data class TopClientsBlocked(
	@SerialName("top_sources_blocked")
	@Serializable(with = ClientBlockedSerializer::class)
	val stats: Map<Client, ClientStats>
)

@Serializable
data class Queries(
	val data: List<QueryData>
)

fun <K> merge(a: Map<K, Int>, b: Map<K, Int>) = (a.asSequence() + b.asSequence())
	.groupingBy { it.key } // Group entries with the same key...
	.fold(0) { sum, entry -> sum + entry.value } // ... and calculate their sum
	.toList()
	.sortedBy { (_, value) -> value } // Sort by sum...
	.reversed() // ... descending
	.take(10)
	.toMap()