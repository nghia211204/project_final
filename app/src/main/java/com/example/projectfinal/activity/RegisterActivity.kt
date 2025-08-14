package com.example.projectfinal

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    private lateinit var etUsername: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnRegister: Button
    private lateinit var tvLoginNow: TextView
    private lateinit var ivBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = Firebase.auth
        bindViews()
        setupClickListeners()
    }

    private fun bindViews() {
        etUsername = findViewById(R.id.et_username)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        btnRegister = findViewById(R.id.btn_register)
        tvLoginNow = findViewById(R.id.tv_login_now)
        ivBack = findViewById(R.id.iv_back)
    }

    private fun setupClickListeners() {
        ivBack.setOnClickListener { finish() }
        tvLoginNow.setOnClickListener { finish() }
        btnRegister.setOnClickListener { handleRegistration() }
    }

    private fun handleRegistration() {
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Email không hợp lệ"
            etEmail.requestFocus()
            return
        }

        if (password.length < 6) {
            etPassword.error = "Mật khẩu phải có ít nhất 6 ký tự"
            etPassword.requestFocus()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("RegisterActivity", "Authentication account created successfully.")
                    val firebaseUser = auth.currentUser
                    firebaseUser?.let {
                        saveUserToFirestore(it.uid, username, email)
                    }
                } else {
                    Log.w("RegisterActivity", "Authentication creation failed.", task.exception)
                    Toast.makeText(baseContext, "Đăng ký thất bại: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserToFirestore(uid: String, username: String, email: String) {
        val user = hashMapOf(
            "uid" to uid,
            "username" to username,
            "email" to email
        )

        db.collection("users").document(uid)
            .set(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "User profile created in Firestore.")
                Toast.makeText(baseContext, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                Log.w("RegisterActivity", "Error creating user profile in Firestore.", e)
                Toast.makeText(baseContext, "Lỗi khi lưu thông tin người dùng.", Toast.LENGTH_SHORT).show()
            }
    }
}