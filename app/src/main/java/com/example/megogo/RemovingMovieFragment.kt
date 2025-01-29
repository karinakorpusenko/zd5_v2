package com.example.megogo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.megogo.databinding.FragmentRemovingMovieBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RemovingMovieFragment : Fragment() {

    private var _binding: FragmentRemovingMovieBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MovieAdapterAdm
    private val movies = mutableListOf<Movie>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRemovingMovieBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MovieAdapterAdm(movies) { movie ->
            deleteMovieFromDatabase(movie)
        }

        binding.recyclerViewMovies.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMovies.adapter = adapter

        loadMoviesFromDatabase()
    }

    private fun loadMoviesFromDatabase() {
        lifecycleScope.launch {
            val database = DatabaseClient.getInstance(requireContext())
            val movieList = withContext(Dispatchers.IO) {
                database.movieDao().getAllMovies()
            }
            movies.clear()
            movies.addAll(movieList)
            adapter.notifyDataSetChanged()
        }
    }

    private fun deleteMovieFromDatabase(movie: Movie) {
        lifecycleScope.launch {
            val database = DatabaseClient.getInstance(requireContext())
            withContext(Dispatchers.IO) {
                database.ticketDao().deleteTicketsByMovieId(movie.id) // Удаляем билеты
                database.movieDao().deleteMovie(movie) // Удаляем фильм
            }
            withContext(Dispatchers.Main) {
                movies.remove(movie)
                adapter.notifyDataSetChanged()
                Toast.makeText(requireContext(), "Фильм и билеты на него были сняты с проката", Toast.LENGTH_SHORT).show()
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
