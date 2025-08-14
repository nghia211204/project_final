package com.example.projectfinal

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var btnSendResetLink: Button
    private lateinit var ivBack: ImageView

    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        bindViews()
        setupClickListeners()
    }

    private fun bindViews() {
        etEmail = findViewById(R.id.et_email_forgot)
        btnSendResetLink = findViewById(R.id.btn_send_reset_link)
        ivBack = findViewById(R.id.iv_back)
    }

    private fun setupClickListeners() {
        ivBack.setOnClickListener {
            finish()
        }
        btnSendResetLink.setOnClickListener {
            handlePasswordReset()
        }
    }

    private fun handlePasswordReset() {
        val email = etEmail.text.toString().trim()

        if (email.isEmpty()) {
            etEmail.error = "Vui lòng nhập email"
            etEmail.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Email không hợp lệ"
            etEmail.requestFocus()
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Liên kết khôi phục đã được gửi đến email của bạn!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, "Lỗi: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}