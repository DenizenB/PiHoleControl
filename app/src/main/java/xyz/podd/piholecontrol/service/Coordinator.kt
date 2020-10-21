package xyz.podd.piholecontrol.service

import kotlinx.coroutines.flow.*
import xyz.podd.piholecontrol.model.Client
import xyz.podd.piholecontrol.model.ClientStats
import xyz.podd.piholecontrol.model.Device
import xyz.podd.piholecontrol.model.mergeClientStats

class Coordinator(private val devices: Collection<Device>) {
    suspend fun getSummary(): Summary {
        return devices.asFlow()
            .map { it.service.getSummary() }
            .reduce { a, b -> a + b }
    }

    suspend fun getTopItems(): TopItems {
        return devices.asFlow()
            .map { it.service.getTopItems() }
            .reduce { a, b -> a + b }
    }

    suspend fun getTopClients(): Map<Client, ClientStats> {
        val topQueries = devices.asFlow()
            .map { it.service.getTopClients().stats }
        val topBlocked = devices.asFlow()
            .map { it.service.getTopClientsBlocked().stats }

        var result: Map<Client, ClientStats> = emptyMap()
        flowOf(topQueries, topBlocked)
            .flattenConcat()
            .collect { result = mergeClientStats(result, it) }

        return result
    }
}