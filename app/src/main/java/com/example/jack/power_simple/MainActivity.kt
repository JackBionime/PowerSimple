package com.example.jack.power_simple

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*

class MainActivity : AppCompatActivity() {

    private val voltageOcv = "/sys/class/power_supply/bms/voltage_ocv"
    private val voltageNow = "/sys/class/power_supply/battery/voltage_now"
    private val capacity = "/sys/class/power_supply/bms/capacity"
    private val handler = Handler(Looper.getMainLooper())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        btnExit.setOnClickListener {
            finishAndRemoveTask()
        }

        btnRefresh.setOnClickListener {
            onVoltageOcv()
            onVoltageNow()
            onCapacity()
        }
        handler.post(runnable())
    }

    private fun runnable() = object : Runnable {
        override fun run() {
            onVoltageOcv()
            onVoltageNow()
            onCapacity()
            handler.postDelayed(this,30000)
        }
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

    private fun getStringFromFile(file: File): String {
        val fileInputStream = FileInputStream(file)
        val ret = convertStreamToString(fileInputStream)
        fileInputStream.close()
        return ret
    }

    private fun onVoltageOcv() {
        try {
            val file = File(voltageOcv)
            if (file.exists()) {
                textOCV.text = "OCV : " + (getStringFromFile(file).toDouble() / 1000000)
            } else {
                textOCV.text = "$voltageOcv File Not Exit"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            textOCV.text = "Exception ${e.message}"
        }
    }

    private fun onVoltageNow() {
        try {
            val file = File(voltageNow)
            if (file.exists()) {
                textBatteryPath.text = "Battery : " + (getStringFromFile(file).toDouble() / 1000000)
            } else {
                textBatteryPath.text = "$voltageNow File Not Exit"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            textBatteryPath.text = "Exception ${e.message}"
        }
    }

    private fun onCapacity() {
        try {
            val file = File(capacity)
            if (file.exists()) {
                textBattery.text = getStringFromFile(file).trim() + " %"
            } else {
                textBattery.text = "$capacity File Not Exit"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            textBattery.text = "Exception ${e.message}"
        }
    }
}