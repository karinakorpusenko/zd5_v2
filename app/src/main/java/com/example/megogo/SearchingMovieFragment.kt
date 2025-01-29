package com.example.megogo

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.megogo.databinding.FragmentSearchingMovieBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchingMovieFragment : Fragment() {

    private var _binding: FragmentSearchingMovieBinding? = null
    private val binding get() = _binding!!

    private lateinit var movieAdapter: MovieAdapter
    private lateinit var genres: List<String>
    private var allMovies: List<MovieItem> = listOf()
    private var filteredMovies: List<MovieItem> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchingMovieBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Обработчик клика по фильму, передача данных фильма
        movieAdapter = MovieAdapter(listOf()) { movie ->
            val intent = Intent(requireContext(), MovieDetailsActivity::class.java).apply {
                putExtra("movieId", movie.id)  // Передаем только movieId для запроса билетов
            }
            startActivity(intent)
        }

        binding.recyclerViewMovies.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = movieAdapter
        }

        // Получение фильмов из базы данных
        loadMovies()

        // Настройка Spinner для фильтрации
        genres = listOf(
            "All genres", "Action", "Horror", "Drama", "Comedy",
            "Thriller", "Mystery", "Romance", "Crime", "Sci-Fi",
            "Animation", "Adventure"
        )
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genres)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGenres.adapter = spinnerAdapter

        binding.spinnerGenres.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterMoviesByGenre(genres[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Обработка нажатия кнопки поиска
        binding.searchBut.setOnClickListener {
            val searchQuery = binding.searchEdit.text.toString()
            if (searchQuery.isNotEmpty()) {
                searchMoviesByTitle(searchQuery)
            }
            else
                Snackbar.make(view, "Заполните пустые поля", Snackbar.LENGTH_SHORT).show()
        }

        // Обработка нажатия кнопки очистки
        binding.clearBut.setOnClickListener {
            binding.searchEdit.text.clear()
            loadMovies() // Загружаем все фильмы из базы данных
        }
    }

    // Подгрузка фильмов в RecyclerView из БД
    private fun loadMovies() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = DatabaseClient.getInstance(requireContext())
            val moviesFromDb = db.movieDao().getAllMovies().map {
                MovieItem(it.id, it.title, it.genre, it.poster, it.year, it.description)
            }
            withContext(Dispatchers.Main) {
                allMovies = moviesFromDb
                filteredMovies = allMovies // Изначально показываем все фильмы
                movieAdapter.updateMovies(filteredMovies)
            }
        }
    }

    // Фильтрация фильмов по жанру
    private fun filterMoviesByGenre(genre: String) {
        filteredMovies = if (genre == "All genres") {
            allMovies
        } else {
            allMovies.filter { it.genre.contains(genre, ignoreCase = true) }
        }
        movieAdapter.updateMovies(filteredMovies)
    }

    // Поиск фильмов по названию
    private fun searchMoviesByTitle(title: String) {
        filteredMovies = allMovies.filter { it.title.contains(title, ignoreCase = true) }
        movieAdapter.updateMovies(filteredMovies)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

