package xyz.podd.piholecontrol.service

import kotlinx.coroutines.flow.*
import xyz.podd.piholecontrol.model.Device

class Coordinator(private val devices: Collection<Device>) {
    suspend fun getSummary(): Summary {
        return devices.asFlow()
            .map { it.service.getSummary() }
            .reduce { a, b -> a + b }
    }

    suspend fun getTopItems(): TopItems {
        return devices.asFlow()
            .map { it.service.getTopItems(it.authToken) }
            .reduce { a, b -> a + b }
    }

    suspend fun getTopClients(): TopClients {
        return devices.asFlow()
            .map { it.service.getTopClients(it.authToken) }
            .reduce { a, b -> a + b }
    }

    suspend fun getTopClientsBlocked(): TopClientsBlocked {
        return devices.asFlow()
            .map { it.service.getTopClientsBlocked(it.authToken) }
            .reduce { a, b -> a + b }
    }
}