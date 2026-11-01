
package com.kalley.remote

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.content.Context

class MainActivity : Activity() {

    private lateinit var nsdManager: NsdManager
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private lateinit var status: TextView
    private lateinit var devices: LinearLayout
    private var scanning = false

    private val serviceType = "_androidtvremote2._tcp."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nsdManager = getSystemService(
            Context.NSD_SERVICE
        ) as NsdManager

        createInterface()
    }

    private fun createInterface() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 35, 24, 24)
            setBackgroundColor(Color.rgb(12, 16, 28))
        }

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val title = TextView(this).apply {
            text = "KALLEY REMOTE"
            textSize = 27f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            letterSpacing = 0.08f
        }

        val subtitle = TextView(this).apply {
            text = "CONTROL UNIVERSAL PARA SMART TV"
            textSize = 11f
            gravity = Gravity.CENTER
            setTextColor(Color.LTGRAY)
            setPadding(0, 8, 0, 25)
        }

        status = TextView(this).apply {
            text = "● Sin televisor conectado"
            textSize = 14f
            setTextColor(Color.rgb(255, 190, 70))
            gravity = Gravity.CENTER
            setPadding(12, 20, 12, 20)
        }

        val search = Button(this).apply {
            text = "⌕  BUSCAR TELEVISORES"
            textSize = 15f
            setOnClickListener {
                startDiscovery()
            }
        }

        val deviceTitle = sectionTitle("DISPOSITIVOS ENCONTRADOS")

        devices = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val remoteTitle = sectionTitle("CONTROL REMOTO")

        val direction = TextView(this).apply {
            text = """
                   ▲

              ◀    OK    ▶

                   ▼
            """.trimIndent()
            textSize = 25f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(0, 20, 0, 20)
        }

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val volumeDown = Button(this).apply {
            text = "VOL −"
            isEnabled = false
        }

        val mute = Button(this).apply {
            text = "SILENCIO"
            isEnabled = false
        }

        val volumeUp = Button(this).apply {
            text = "VOL +"
            isEnabled = false
        }

        controls.addView(volumeDown)
        controls.addView(mute)
        controls.addView(volumeUp)

        val notice = TextView(this).apply {
            text = "Los controles estarán disponibles después del emparejamiento."
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(Color.LTGRAY)
            setPadding(12, 25, 12, 15)
        }

        content.addView(title)
        content.addView(subtitle)
        content.addView(status)
        content.addView(search)
        content.addView(deviceTitle)
        content.addView(devices)
        content.addView(remoteTitle)
        content.addView(direction)
        content.addView(controls)
        content.addView(notice)

        scroll.addView(content)
        root.addView(scroll)

        setContentView(root)
    }

    private fun sectionTitle(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(Color.rgb(100, 160, 255))
            setPadding(5, 25, 5, 15)
        }
    }

    private fun startDiscovery() {
        if (scanning) return

        devices.removeAllViews()
        status.text = "Buscando televisores compatibles..."

        val listener = object : NsdManager.DiscoveryListener {

            override fun onDiscoveryStarted(type: String) {
                scanning = true
            }

            override fun onServiceFound(info: NsdServiceInfo) {
                if (info.serviceType.contains("androidtvremote2")) {
                    runOnUiThread {
                        val item = TextView(this@MainActivity).apply {
                            text = "TV encontrada: ${info.serviceName}"
                            textSize = 15f
                            setTextColor(Color.WHITE)
                            setPadding(15, 18, 15, 18)
                        }
                        devices.addView(item)
                    }
                }
            }

            override fun onServiceLost(info: NsdServiceInfo) {}

            override fun onDiscoveryStopped(type: String) {
                scanning = false
            }

            override fun onStartDiscoveryFailed(type: String, error: Int) {
                scanning = false
                status.text = "No se pudo iniciar la búsqueda."
                nsdManager.stopServiceDiscovery(this)
            }

            override fun onStopDiscoveryFailed(type: String, error: Int) {
                nsdManager.stopServiceDiscovery(this)
            }
        }

        discoveryListener = listener

        try {
            nsdManager.discoverServices(
                serviceType,
                NsdManager.PROTOCOL_DNS_SD,
                listener
            )
        } catch (e: Exception) {
            scanning = false
            status.text = "Error al buscar dispositivos."
        }
    }

    override fun onDestroy() {
        discoveryListener?.let {
            try {
                nsdManager.stopServiceDiscovery(it)
            } catch (_: Exception) {}
        }
        super.onDestroy()
    }
}
