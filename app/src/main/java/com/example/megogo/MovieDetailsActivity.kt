package com.example.megogo

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.StaticLayout
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MovieDetailsActivity : AppCompatActivity() {

    private lateinit var pref : SharedPreferences
    private lateinit var ticketAdapter: TicketAdapter
    private lateinit var db: CinemaDatabase
    private var movieId: Long = -1
    private var selectedTicket: Ticket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_details)

        pref = getSharedPreferences("PREF", MODE_PRIVATE)
        db = DatabaseClient.getInstance(this)

        // Получаем данные фильма
        movieId = intent.getLongExtra("movieId", -1)
        if (movieId == -1L) {
            Toast.makeText(this, "Фильм не найден", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Настройка RecyclerView для билетов
        ticketAdapter = TicketAdapter(emptyList()) { ticket ->
            selectedTicket = ticket
            onBuyTicketClicked()
        }

        findViewById<RecyclerView>(R.id.ticketRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@MovieDetailsActivity)
            adapter = ticketAdapter
        }

        // Загружаем данные о фильме и билетах
        loadMovieDetails()
        loadTickets()
    }

    private fun loadMovieDetails() {
        lifecycleScope.launch(Dispatchers.IO) {
            val movie = db.movieDao().getMovieById(movieId)
            withContext(Dispatchers.Main) {
                findViewById<TextView>(R.id.movieTitle).text = movie!!.title
                findViewById<TextView>(R.id.movieGenre).text = movie.genre
                findViewById<TextView>(R.id.movieYear).text = movie.year
                findViewById<TextView>(R.id.moviePlot).text = movie.description
                Picasso.get().load(movie.poster).into(findViewById<ImageView>(R.id.moviePoster))
            }
        }
    }

    private fun loadTickets() {
        lifecycleScope.launch(Dispatchers.IO) {
            val tickets = db.ticketDao().getTicketsByMovieId(movieId).filter { it.ownerUsername == null }
            withContext(Dispatchers.Main) {
                ticketAdapter.updateTickets(tickets)
            }
        }
    }

    private fun onBuyTicketClicked() {
        selectedTicket?.let {
            if (it.ownerUsername == null) {
                // Купить билет
                lifecycleScope.launch(Dispatchers.IO) {
                    val username = pref.getString("Online", "User008")
                    db.ticketDao().updateTicket(it.id, username.toString())
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MovieDetailsActivity, "Билет успешно куплен", Toast.LENGTH_SHORT).show()
                        loadTickets()  // Обновим список билетов
                    }
                }
            } else {
                Toast.makeText(this, "Этот билет уже занят", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this, "Сначала выберите билет", Toast.LENGTH_SHORT).show()
        }
    }
}

