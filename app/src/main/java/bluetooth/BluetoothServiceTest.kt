package bluetooth

import android.os.Handler
import android.os.Looper


class BluetoothServiceTest(private val callback: BluetoothService.BluetoothCallback) {
    fun startServer() {
        Handler(Looper.getMainLooper()).postDelayed({
            callback.onConnectionEstablished()
        }, 500)
    }

    fun connectToDevice(device: String) {
        Handler(Looper.getMainLooper()).postDelayed({
            callback.onConnectionEstablished()
        }, 1000)
    }

    fun receiveMove(data: String) {
        callback.onMoveReceived(data)
    }
}