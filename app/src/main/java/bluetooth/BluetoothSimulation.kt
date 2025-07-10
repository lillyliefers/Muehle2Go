package bluetooth

import android.util.Log
import bluetooth.BluetoothService.BluetoothCallback

class BluetoothSimulation {

    private val hostCallback = object : BluetoothCallback {
        override fun onConnectionEstablished() {
            Log.d("BluetoothSim", "Host: Verbindung hergestellt")
            simulateHostSendingMove(5)
        }

        override fun onMoveReceived(data: String) {
            Log.d("BluetoothSim", "Host: Zug empfangen - $data")
        }

        override fun onConnectionLost() {
            Log.d("BluetoothSim", "Host: Verbindung verloren")
        }
    }

    private val clientCallback = object : BluetoothCallback {
        override fun onConnectionEstablished() {
            Log.d("BluetoothSim", "Client: Verbindung hergestellt")
            simulateClientSendingMove(12)
        }

        override fun onMoveReceived(data: String) {
            Log.d("BluetoothSim", "Client: Zug empfangen - $data")
        }

        override fun onConnectionLost() {
            Log.d("BluetoothSim", "Client: Verbindung verloren")
        }
    }

    private val host = BluetoothServiceTest(hostCallback)
    private val client = BluetoothServiceTest(clientCallback)

    fun simulateConnection() {
        host.startServer()
        client.connectToDevice("TestDevice")
    }

    private fun simulateHostSendingMove(position: Int) {
        Log.d("BluetoothSim", "Host sendet Zug: $position")
        client.receiveMove(position.toString())
    }

    private fun simulateClientSendingMove(position: Int) {
        Log.d("BluetoothSim", "Client sendet Zug: $position")
        host.receiveMove(position.toString())
    }
}