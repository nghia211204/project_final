package com.example.projectfinal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projectfinal.manager.BookmarkManager
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvSignUp: TextView
    private lateinit var ivBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = Firebase.auth
        bindViews()
        setupClickListeners()
    }

    private fun bindViews() {
        etUsername = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        tvForgotPassword = findViewById(R.id.tv_forgot_password)
        tvSignUp = findViewById(R.id.tv_sign_up)
        ivBack = findViewById(R.id.iv_back)
    }

    private fun setupClickListeners() {
        ivBack.setOnClickListener { finish() }
        tvSignUp.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
        tvForgotPassword.setOnClickListener { startActivity(Intent(this, ForgotPasswordActivity::class.java)) }
        btnLogin.setOnClickListener { handleLogin() }
    }

    private fun handleLogin() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Tên tài khoản và mật khẩu", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").whereEqualTo("username", username).limit(1).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(baseContext, "Tên tài khoản hoặc mật khẩu không đúng.", Toast.LENGTH_SHORT).show()
                } else {
                    val email = documents.documents[0].getString("email")
                    if (email != null) {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    Log.d("LoginActivity", "signInWithEmail:success")
                                    Toast.makeText(baseContext, "Đang tải dữ liệu...", Toast.LENGTH_SHORT).show()
                                    BookmarkManager.fetchBookmarksForCurrentUser {
                                        Toast.makeText(baseContext, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                } else {
                                    Toast.makeText(baseContext, "Tên tài khoản hoặc mật khẩu không đúng.", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(baseContext, "Lỗi kết nối.", Toast.LENGTH_SHORT).show()
            }
    }
}