package com.ionclaw.app.network

import java.net.Inet4Address
import java.net.NetworkInterface

// enumerates the device IPv4 addresses so the user can reach the server from the lan
object NetworkInterfaces {
    fun localIPv4Addresses(): List<String> {
        val addresses = mutableListOf<String>()

        for (networkInterface in NetworkInterface.getNetworkInterfaces()) {
            if (!networkInterface.isUp || networkInterface.isLoopback) {
                continue
            }

            for (address in networkInterface.inetAddresses) {
                if (address !is Inet4Address || address.isLoopbackAddress) {
                    continue
                }

                val host = address.hostAddress ?: continue

                if (host !in addresses) {
                    addresses.add(host)
                }
            }
        }

        return addresses.ifEmpty { listOf("127.0.0.1") }
    }
}
