package bluetooth

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import bluetooth.BluetoothSimulation
import com.example.offlinemuehle.R

class BluetoothSimulationActivity : AppCompatActivity() {

    private lateinit var simulation: BluetoothSimulation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_simulation)

        simulation = BluetoothSimulation()

        findViewById<Button>(R.id.button_start_simulation).setOnClickListener {
            simulation.simulateConnection()
        }
    }
}