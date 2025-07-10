package ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.offlinemuehle.R

class DeviceSelectionFragment : Fragment() {

    private lateinit var deviceListView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val discoveredDevices = mutableListOf<BluetoothDevice>()
    private val deviceNames = mutableListOf<String>()
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_device_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deviceListView = view.findViewById(R.id.device_list)

        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, deviceNames)
        deviceListView.adapter = adapter

        deviceListView.setOnItemClickListener { _, _, position, _ ->

            if (requireContext().checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                bluetoothAdapter?.cancelDiscovery()
            }
            if (activity is MainActivity) {
                (activity as MainActivity).startAsClient(discoveredDevices[position])
            } else {
                Toast.makeText(requireContext(), "MainActivity not available", Toast.LENGTH_SHORT).show()
            }
        }

        if (requireContext().checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            if (bluetoothAdapter?.isDiscovering == true) {
                bluetoothAdapter?.cancelDiscovery()
            }
        }
        if (requireContext().checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            if (bluetoothAdapter?.startDiscovery() == false) {
                Toast.makeText(requireContext(), "Failed to start discovery", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Bluetooth permission not granted", Toast.LENGTH_SHORT).show()
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        requireActivity().registerReceiver(receiver, filter)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (BluetoothDevice.ACTION_FOUND == intent?.action) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    if (!discoveredDevices.contains(it)) {
                        discoveredDevices.add(it)
                        val deviceName = if (requireContext().checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                            it.name ?: it.address
                        } else {
                            it.address
                        }
                        deviceNames.add(deviceName)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(receiver)
    }
}