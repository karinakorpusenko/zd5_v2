package com.example.megogo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.megogo.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivityForUser : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_for_user)

        // Получаем BottomNavigationView из разметки
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_nav_user)

        // Устанавливаем слушатель для выбора пунктов меню
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_searching -> {
                    replaceFragment(SearchingMovieFragment())
                    true
                }
                R.id.nav_myTickets -> {
                    replaceFragment(MyTicketsFragment())
                    true
                }
                else -> false
            }
        }

        // Устанавливаем фрагмент по умолчанию
        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.nav_searching
        }
    }

    // Функция для замены фрагмента
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container_user, fragment)
            .commit()
    }
}