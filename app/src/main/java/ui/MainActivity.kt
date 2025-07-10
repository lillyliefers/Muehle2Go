package ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.offlinemuehle.R
import com.example.offlinemuehle.ui.GameFragment

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Toast
import android.content.Context
import android.os.Handler
import android.os.Looper
import bluetooth.BluetoothService
import ui.DeviceSelectionFragment


class MainActivity : AppCompatActivity() {

    // for espresso tests - demo mode
    var forceDemoMode = false


    private lateinit var bluetoothService: BluetoothService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val demoMode = intent?.getBooleanExtra("DEMO_MODE", false) == true

        bluetoothService = BluetoothService(this, demoMode, object : BluetoothService.BluetoothCallback {
            override fun onMoveReceived(move: String) {
                // Beispiel: Weiterleiten oder Anzeige im UI
            }
            override fun onConnectionEstablished() {
                // Beispiel: Wechsel zum GameFragment nach erfolgreicher Verbindung
            }

            override fun onConnectionLost() {
                Toast.makeText(this@MainActivity, "Verbindung verloren", Toast.LENGTH_SHORT).show()
            }
        })

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                //.replace(R.id.main_container, GameFragment())
                .replace(R.id.main_container, RoleSelectionFragment())
                .commit()
        }
        if (!demoMode) {
            requestBluetoothPermissions()
        }
    }

    // request bluetooth conection from user
    private val REQUEST_PERMISSIONS_CODE = 1

    private fun requestBluetoothPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), REQUEST_PERMISSIONS_CODE)
        }
    }

    fun hasBluetoothPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    }

    fun startAsHost() {
        // for espresso tests - demo mode
        if (forceDemoMode) {
            launchDemoGame()
            return
        }
        if (!hasBluetoothPermissions(this)) {
            Toast.makeText(this, "Bluetooth-Verbindung fehlgeschlagen - starte Demo-Modus", Toast.LENGTH_LONG).show()
            launchDemoGame()
            return
        }

        val handler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            Toast.makeText(this, "Keine Verbindung - starte Demo", Toast.LENGTH_LONG).show()
            launchDemoGame()
        }

        bluetoothService.startServer {
            handler.removeCallbacks(timeoutRunnable)  // Erfolg: cancel timeout
            runOnUiThread {
                Toast.makeText(this, "Bluetooth-Verbindung hergestellt", Toast.LENGTH_SHORT).show()
                launchGame()
            }
        }

        // Timeout: warte 15 Sekunden
        handler.postDelayed(timeoutRunnable, 15000)
    }

    fun startAsClient(device: BluetoothDevice) {
        val handler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            Toast.makeText(this, "Keine Verbindung - starte Demo", Toast.LENGTH_LONG).show()
            launchDemoGame()
        }

        try {
            bluetoothService.connectToDevice(device)
            handler.postDelayed(timeoutRunnable, 15000)  // Timeout nach 15 Sek.

            runOnUiThread {
                Toast.makeText(this, "Bluetooth-Verbindung hergestellt", Toast.LENGTH_SHORT).show()
                handler.removeCallbacks(timeoutRunnable)  // Verbindung klappte
                launchGame()
            }
        } catch (e: Exception) {
            handler.removeCallbacks(timeoutRunnable)
            runOnUiThread {
                Toast.makeText(this, "Verbindung fehlgeschlagen - starte Demo", Toast.LENGTH_LONG).show()
                launchDemoGame()
            }
        }
    }



    fun launchGame() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, GameFragment())
            .commit()
    }

    fun launchDemoGame() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, GameFragment()) // default constructor for now
            .commit()
    }

    fun showDeviceSelection() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, DeviceSelectionFragment())
            .addToBackStack(null)
            .commit()
    }

    companion object {
        fun permissionIsGranted(checkResult: Int): Boolean {
            return checkResult == PackageManager.PERMISSION_GRANTED
        }
    }
}