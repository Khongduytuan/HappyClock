package com.eagletech.happyclock

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.eagletech.happyclock.databinding.ActivityMainBinding
import com.eagletech.happyclock.receiver.AlarmReceiver
import com.eagletech.happyclock.service.AlarmService
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.setAlarmButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                calendar.set(Calendar.HOUR_OF_DAY, binding.timePicker.hour)
                calendar.set(Calendar.MINUTE, binding.timePicker.minute)
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, binding.timePicker.currentHour)
                calendar.set(Calendar.MINUTE, binding.timePicker.currentMinute)
            }
            calendar.set(Calendar.SECOND, 0)

            setAlarm(calendar.timeInMillis)
        }

        binding.stopButton.setOnClickListener {
            stopAlarm()
            Toast.makeText(this, "Alarm stopped!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setAlarm(timeInMillis: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)

        // Start foreground service
        val serviceIntent = Intent(this, AlarmService::class.java)
        startService(serviceIntent)

        Toast.makeText(this, "Alarm set!", Toast.LENGTH_SHORT).show()
    }

    private fun stopAlarm() {
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            action = "com.eagletech.happyclock.ACTION_STOP_ALARM"
        }
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        sendBroadcast(intent)

        // Stop foreground service
        val serviceIntent = Intent(this, AlarmService::class.java)
        stopService(serviceIntent)
    }
}
