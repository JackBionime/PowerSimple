package com.example.jack.power_simple

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private val voltageOcv = "/sys/class/power_supply/bms/voltage_ocv"
    private val voltageNow = "/sys/class/power_supply/battery/voltage_now"
    private val capacity = "/sys/class/power_supply/bms/capacity"
    private val handler = Handler(Looper.getMainLooper())
    private val powerTxt = "power_simple.csv"
    private val format = "MM-dd HH:mm:ss"
    private lateinit var simpleDateFormat: SimpleDateFormat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        simpleDateFormat = SimpleDateFormat(format, Locale.getDefault())
        btnExit.setOnClickListener {
            finishAndRemoveTask()
        }

        btnRefresh.setOnClickListener {
            onVoltageOcv()
            onVoltageNow()
            onCapacity()
        }
        val title = "Date,SOC,OCV,Battery"
        writeFile(title)
        handler.post(runnable())

        // TODO Read CSV
//        btnReadFile.setOnClickListener {
//            try {
//                val file = File(applicationContext.getExternalFilesDir(null), powerTxt)
//                val reader = CSVReader(FileReader(file))
//                var nextLine: Array<String>? = null
//                while (reader.readNext().also { nextLine = it } != null) {
//                    // nextLine[] is an array of values from the line
//                    Log.d(
//                        "TestCSV",
//                        (nextLine?.get(0) ?: "No Line") + (nextLine?.get(1) ?: "No Line") + "etc..."
//                    )
//                }
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }
    }

    private fun runnable() = object : Runnable {
        override fun run() {
            val time = simpleDateFormat.format(Date())
            val capacity = onCapacity()
            val ocv = onVoltageOcv()
            val voltageNow = onVoltageNow()
            val text = "$time,$capacity,$ocv,$voltageNow"
            writeFile(text)
            handler.postDelayed(this, 30000)
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

    private fun onVoltageOcv(): String {
        val message = try {
            val file = File(voltageOcv)
            if (file.exists()) {
                (getStringFromFile(file).toDouble() / 1000000).toString()
            } else {
                "$voltageOcv File Not Exit"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Exception ${e.message}"
        }
        textOCV.text = "OCV : $message"
        return message
    }

    private fun onVoltageNow(): String {
        val message = try {
            val file = File(voltageNow)
            if (file.exists()) {
                (getStringFromFile(file).toDouble() / 1000000).toString()
            } else {
                "$voltageNow File Not Exit"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Exception ${e.message}"
        }
        textBatteryPath.text = "Battery : $message"
        return message
    }

    private fun onCapacity(): String {
        val message = try {
            val file = File(capacity)
            if (file.exists()) {
                getStringFromFile(file).trim()
            } else {
                "$capacity File Not Exit"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Exception ${e.message}"
        }
        textBattery.text = "$message %"
        return message
    }

    private fun writeFile(text: String) {
        val file = File(applicationContext.getExternalFilesDir(null), powerTxt)
        try {
            val buf = BufferedWriter(FileWriter(file, true))
            buf.append(text)
            buf.newLine()
            buf.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}