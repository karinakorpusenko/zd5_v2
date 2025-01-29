package com.example.megogo

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Registration : AppCompatActivity() {
    private lateinit var email : EditText
    private lateinit var login : EditText
    private lateinit var pass : EditText
    private lateinit var repPass : EditText
    private lateinit var role : Spinner
    private lateinit var db: CinemaDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        email = findViewById(R.id.EmailEdit)
        login = findViewById(R.id.LoginEdit)
        pass = findViewById(R.id.PassEdit)
        repPass = findViewById(R.id.RepPassEdit)
        role = findViewById(R.id.RoleSpin)

        db = DatabaseClient.getInstance(applicationContext)
    }

    // Функция для проверки email
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
        return email.matches(Regex(emailRegex))
    }

    // Функция для проверки логина
    fun isValidLogin(login: String): Boolean {
        // Логин должен быть длиной от 3 до 20 символов и содержать только латинские буквы и цифры
        val loginRegex = "^[A-Za-z0-9]{3,20}\$"
        return login.matches(Regex(loginRegex))
    }

    // Функция для проверки пароля
    fun isValidPassword(password: String): Boolean {
        // Пароль должен содержать хотя бы одну заглавную букву, одну цифру и иметь длину от 8 до 20 символов
        val passwordRegex = "^(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,20}\$"
        return password.matches(Regex(passwordRegex))
    }

    private fun registerUser(username: String, email: String, password: String, role: String) {
        // Создаём нового пользователя
        val newUser = User(username = username, email = email, password = password, role = role)

        // Вставляем пользователя в базу данных
        GlobalScope.launch {
            db.userDao().insertUser(newUser)
            runOnUiThread {
                Toast.makeText(this@Registration, "Регистрация прошла успешно!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun Register(view: View) {
        if (email.text.isNotEmpty() && login.text.isNotEmpty() && pass.text.isNotEmpty() && repPass.text.isNotEmpty()) {
            if (isValidEmail(email.text.toString())) {
                if (isValidLogin(login.text.toString())) {
                    if (isValidPassword(pass.text.toString())) {
                        if (pass.text.toString() == repPass.text.toString()) {
                            // Используем lifecycleScope для работы в фоновом потоке
                            lifecycleScope.launch(Dispatchers.IO) {
                                // Проверка существования пользователя с таким логином или email
                                val user = db.userDao().getUserByUsernameOrEmail(login.text.toString(), email.text.toString())

                                // Проверка, если пользователь не найден (то есть можно зарегистрировать)
                                if (user == null) {
                                    // Если аккаунт не существует, регистрируем пользователя
                                    registerUser(login.text.toString(), email.text.toString(), pass.text.toString(), role.selectedItem.toString())

                                    // Очистка полей
                                    withContext(Dispatchers.Main) {
                                        email.text.clear()
                                        login.text.clear()
                                        pass.text.clear()
                                        repPass.text.clear()
                                    }
                                } else {
                                    // Если пользователь уже существует с таким логином или email
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            this@Registration,
                                            "Учетная запись с таким адресом электронной почты или таким логином уже существует",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(this@Registration, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@Registration, "Неверный пароль", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@Registration, "Неверный логин", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@Registration, "Неверная почта", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this@Registration, "Заполните все поля", Toast.LENGTH_SHORT).show()
        }
    }

}