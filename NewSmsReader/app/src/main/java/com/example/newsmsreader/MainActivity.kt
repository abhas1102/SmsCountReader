package com.example.newsmsreader

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.newsmsreader.databinding.ActivityMainBinding
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSubmit.setOnClickListener {
            //Checking for permissions
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    Array(1) { android.Manifest.permission.READ_SMS },
                    111
                )

            } else {
                //For long running calculations context would be Default. Message count might go long
                GlobalScope.launch(Dispatchers.Default) {
                    //Do not allow click listener to implement count function if any of the inputs is empty.
                    if (binding.etNumber.text.toString().isNotEmpty() && binding.etDays.text.toString().isNotEmpty()){
                        binding.countText.text =
                            getSmsCountByNumber(binding.etNumber.text.toString(),binding.etDays.text.toString()).toString()
                    }
                    else{
                        //Context switching to show other results
                        withContext(Dispatchers.Main){
                            Toast.makeText(this@MainActivity,"Any of your input is empty",Toast.LENGTH_SHORT).show()  // Showing Toast on MainActivity context

                        }
                    }
                }
                onStop()
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED) GlobalScope.launch {
            getSmsCountByNumber(
                binding.etNumber.text.toString(), binding.etDays.text.toString()
            )
        }
    }



        suspend fun getSmsCountByNumber(senderNumber: String, days: String):Int {
        val countryCode = "+91"
        val address = countryCode+senderNumber
        val valueOfDaysInLong = (Integer.parseInt(days)).toLong()
        val noOfDaysInMilliSeconds = System.currentTimeMillis() - valueOfDaysInLong*24*3600*1000
        


        var projection = arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.DATE,Telephony.Sms._ID) // Sms._ID is needed to include so that it can be identified as array in contetn resolver
        var selection = Telephony.Sms.ADDRESS + "=?" + " AND " + Telephony.Sms.DATE + ">= ?"  //ADDRESS=? AND DATE >= ?
        var selectionArgs = arrayOf<String>(address, noOfDaysInMilliSeconds.toString())

        var cursor = contentResolver.query(Telephony.Sms.CONTENT_URI,projection,selection,selectionArgs,null)

        if (cursor != null) {
            cursor.moveToNext()

            Log.d("isCheck",cursor.isFirst().toString())
            Log.d("Count", cursor.count.toString())
        }
            return cursor?.count!!
        }
}
