package com.example.jack.power_simple

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*

class MainActivity : AppCompatActivity() {

    private lateinit var batteryBroadCase: BroadcastReceiver
    private val filePath = "/sys/class/power_supply/bms/voltage_ocv"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)

        batteryBroadCase = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                    // NOTE Broadcast每秒監聽一次,無法控制
                    val level = intent.getIntExtra("level", 0)
                    val scale = intent.getIntExtra("scale", 100)

                    val percentage = (level * 100) / scale
                    textBattery.text = "$percentage %"
                }
            }
        }

        try {
            // NOTE 讀取檔案 voltage_ocv
            textFilePath.text = getStringFromFile(filePath)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        registerReceiver(batteryBroadCase, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryBroadCase)
    }

    private fun convertStreamToString(`is`: InputStream): String {
        val reader = BufferedReader(InputStreamReader(`is`))
        val sb = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            sb.append(line).append("\n")
        }
        reader.close()
        return sb.toString()
    }

    private fun getStringFromFile(text: String): String {
        val file = File(text)
        return if (file.exists()) {
            val fileInputStream = FileInputStream(file)
            val ret = convertStreamToString(fileInputStream)
            fileInputStream.close()
            ret
        } else {
            "File Not Exit"
        }
    }
}