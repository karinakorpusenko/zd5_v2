package com.example.megogo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AdditionMovieFragment : Fragment() {

    private lateinit var searchTitle: EditText
    private lateinit var searchYear: EditText
    private lateinit var searchButton: Button
    private lateinit var addButton: Button
    private lateinit var posterIm: ImageView
    private lateinit var titleText: TextView
    private lateinit var yearText: TextView
    private lateinit var genreText: TextView
    private lateinit var plotText: TextView
    private lateinit var db: CinemaDatabase
    private var movie: Movie? = null
    private lateinit var movieViewModel: MovieViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_addition_movie, container, false)

        db = DatabaseClient.getInstance(requireContext())
        searchTitle = view.findViewById(R.id.etSearchTitle)
        searchYear = view.findViewById(R.id.etSearchYear)
        searchButton = view.findViewById(R.id.btnSearch)
        addButton = view.findViewById(R.id.btnAdd)
        posterIm = view.findViewById(R.id.tvPoster)
        titleText = view.findViewById(R.id.tvTitle)
        yearText = view.findViewById(R.id.tvYear)
        genreText = view.findViewById(R.id.tvGenre)
        plotText = view.findViewById(R.id.tvPlot)

        // Инициализация ViewModel
        movieViewModel = ViewModelProvider(this).get(MovieViewModel::class.java)

        // Наблюдаем за результатом добавления фильма
        movieViewModel.movieAdded.observe(viewLifecycleOwner, Observer { success ->
            if (success) {
                Toast.makeText(requireContext(), "Фильм успешно добавлен!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "При добавлении фильма произошла ошибка", Toast.LENGTH_SHORT).show()
            }
        })

        searchButton.setOnClickListener {
            if (searchTitle.text.isNotEmpty()) {
                val title = searchTitle.text.toString()
                val year = searchYear.text.toString()
                searchMovies(title, year)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Введите хотя бы название фильма",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        addButton.setOnClickListener {
            addMovieToDatabase(movie)
        }

        return view
    }

    private fun searchMovies(title: String, year: String) {
        val url = "https://www.omdbapi.com/?apikey=8424b5c9&t=$title&y=$year"

        val queue = Volley.newRequestQueue(requireContext())
        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                val obj = JSONObject(response)
                try {
                    movie = Movie(
                        id = 0,
                        title = obj.getString("Title"),
                        year = obj.getString("Year"),
                        poster = obj.getString("Poster"),
                        description = obj.getString("Plot"),
                        genre = obj.getString("Genre")
                    )
                    movie?.let { loadMovie(it) }
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Фильм не был найден",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            {
                Toast.makeText(requireContext(), "Здесь какая-то ошибка, мы скоро ее исправим", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(stringRequest)
    }

    private fun loadMovie(movie: Movie) {
            titleText.text = movie.title
            yearText.text = movie.year
            plotText.text = movie.description
            genreText.text = movie.genre
            Picasso.get()
                .load(movie.poster)
                .placeholder(R.drawable.holder)
                .into(posterIm)
    }

    private fun addMovieToDatabase(movie: Movie?) {
        if (movie == null) {
            Toast.makeText(requireContext(), "Пожалуйста, сначала выполните поиск и выберите фильм", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val existingMovie = withContext(Dispatchers.IO) {
                    db.movieDao().getMovieByTitle(movie.title)
                }

                if (existingMovie == null) {
                    withContext(Dispatchers.IO) {
                        movieViewModel.addMovieWithTickets(movie)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "\n" +
                                "Фильм уже вышел в прокат", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Ошибка при добавлении фильма", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
