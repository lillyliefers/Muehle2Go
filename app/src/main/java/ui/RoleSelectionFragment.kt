package ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.offlinemuehle.R
import android.content.Intent
import bluetooth.BluetoothSimulationActivity

class RoleSelectionFragment : Fragment() {


    // for testting
    var testActivity: MainActivity? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_role_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_host).setOnClickListener {
            (testActivity ?: activity as? MainActivity)?.startAsHost()
        }

        view.findViewById<Button>(R.id.button_client).setOnClickListener {
            (testActivity ?: activity as? MainActivity)?.showDeviceSelection()
        }

        //  Simulation-Button für Testzwecke (optional sichtbar machen im XML)
        view.findViewById<Button>(R.id.button_debug_simulation).setOnClickListener {
            val intent = Intent(activity, BluetoothSimulationActivity::class.java)
            startActivity(intent)
        }
    }
}
