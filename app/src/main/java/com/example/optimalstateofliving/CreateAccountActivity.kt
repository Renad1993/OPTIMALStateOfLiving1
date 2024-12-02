package com.example.optimalstateofliving

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class CreateAccountActivity : AppCompatActivity() {
    var mAuth : FirebaseAuth? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        mAuth = FirebaseAuth.getInstance()

    }
}