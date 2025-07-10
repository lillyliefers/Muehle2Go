package bluetooth

import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


/**
 * Verantwortung: Aufbau der Bluetooth verbindung (client-server) und datenübertragung
 * test ideen: verbindungstest auf zwei geräten, epfang von synchronen zügen (integrationstest), fehlerbehandlung bei verbindungsabbruch
 */
class BluetoothService(private val context: Context, private val demoMode: Boolean, private val callback: BluetoothCallback) {

    interface BluetoothCallback {
        fun onMoveReceived(move: String)
        fun onConnectionEstablished()
        fun onConnectionLost()
    }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val appUuid: UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")

    var isHost: Boolean = false

    private var serverThread: AcceptThread? = null
    private var clientThread: ConnectThread? = null
    private var connectionThread: ConnectedThread? = null

    fun startServer(onConnected: () -> Unit) {
        if (demoMode) {
            Handler(Looper.getMainLooper()).post {
                callback.onConnectionEstablished()
            }
            return
        }
        isHost = true
        serverThread = AcceptThread(onConnected)
        serverThread?.start()
    }

    fun connectToDevice(device: BluetoothDevice) {
        if (demoMode) {
            Handler(Looper.getMainLooper()).post {
                callback.onConnectionEstablished()
            }
            return
        }
        isHost = false
        clientThread = ConnectThread(device)
        clientThread?.start()
    }

    fun sendMove(move: String) {
        connectionThread?.write(move.toByteArray())
    }

    fun receiveMove(data: String) {
        BluetoothLog.log("Empfangen: $data")
        callback.onMoveReceived(data)
    }

    fun sendMove(position: Int) {
        sendMove(position.toString())
    }

    fun closeConnection() {
        connectionThread?.cancel()
        serverThread?.cancel()
        clientThread?.cancel()
    }

    // Accept incoming connections
    private inner class AcceptThread(private val onConnected: () -> Unit) : Thread() {
        private var serverSocket: BluetoothServerSocket? = null

        override fun run() {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Handler(Looper.getMainLooper()).post {
                    callback.onConnectionLost()
                }
                return
            }

            runCatching {
                serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord("MuehleGame", appUuid)
                val socket = serverSocket?.accept()
                socket?.let {
                    manageConnection(it)
                    Handler(Looper.getMainLooper()).post {
                        onConnected()
                    }
                    serverSocket?.close()
                }
            }.onFailure {
                serverSocket?.close()
                Handler(Looper.getMainLooper()).post {
                    callback.onConnectionLost()
                }
            }
        }

        fun cancel() {
            serverSocket?.close()
        }
    }

    // Connect to host
    private inner class ConnectThread(private val device: BluetoothDevice) : Thread() {
        private var socket: BluetoothSocket? = null

        override fun run() {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED){
                return
            }

            socket = device.createRfcommSocketToServiceRecord(appUuid)

            bluetoothAdapter?.cancelDiscovery()
            try {
                socket?.connect()
                socket?.let { manageConnection(it) }
            } catch (e: IOException) {
                socket?.close()
            }
        }

        fun cancel() {
            try {
                socket?.close()
            } catch (_: IOException) {}
        }
    }

    // Exchange data
    private inner class ConnectedThread(private val socket: BluetoothSocket) : Thread() {
        private val inStream: InputStream = socket.inputStream
        private val outStream: OutputStream = socket.outputStream
        private val handler = Handler(Looper.getMainLooper())

        override fun run() {
            val buffer = ByteArray(1024)
            while (true) {
                try {
                    val bytes = inStream.read(buffer)
                    val message = String(buffer, 0, bytes)
                    handler.post {
                        receiveMove(message)
                    }
                } catch (e: IOException) {
                    handler.post {
                        callback.onConnectionLost()
                    }
                    break
                }
            }
        }

        fun write(bytes: ByteArray) {
            try {
                outStream.write(bytes)
            } catch (_: IOException) {
            }
        }

        fun cancel() {
            socket.close()
        }
    }

    private fun manageConnection(socket: BluetoothSocket) {
        connectionThread = ConnectedThread(socket)
        connectionThread?.start()
        Handler(Looper.getMainLooper()).post {
            callback.onConnectionEstablished()
        }
    }

    object BluetoothLog {
        val moveLog: MutableList<String> = mutableListOf()

        fun log(message: String) {
            moveLog.add(message)
            Log.d("BluetoothLog", message)
        }

        fun clear() {
            moveLog.clear()
        }
    }
}