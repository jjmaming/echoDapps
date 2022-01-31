package com.echo.myDapp

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

open class BaseActivity : AppCompatActivity() {

    private lateinit var file: File

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        val etherWalletPath = "/echo"
        file = File(filesDir.toString() + etherWalletPath)
        //create the directory if it does not exist
        if (!file.mkdirs()) {
            file.mkdirs()
        }
    }

    fun getFile() = file

    fun toastAsync(message: String?) {
        runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_LONG).show() }
    }
}