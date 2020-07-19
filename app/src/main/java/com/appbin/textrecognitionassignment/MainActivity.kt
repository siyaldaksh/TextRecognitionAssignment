package com.appbin.textrecognitionassignment

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var permissionLayout : ConstraintLayout
    private lateinit var allow : Button
    private lateinit var deny : Button

    companion object{
        private const val PERMISSION_REQUEST_CODE : Int = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        permissionLayout = findViewById(R.id.permission)
        allow = findViewById(R.id.allowPermission)
        deny = findViewById(R.id.denyPermission)

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                permissionLayout.visibility = View.GONE
            }

            else -> {
                permissionLayout.visibility = View.VISIBLE
            }
        }

        allow.setOnClickListener {
            requestPermissionFun()
        }

        deny.setOnClickListener {
            Toast.makeText(this,"Please allow to continue", Toast.LENGTH_SHORT).show()
        }


    }

    private fun requestPermissionFun() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {

                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED) {

                    permissionLayout.visibility = View.GONE
                }
                return
            }
        }
    }

}
