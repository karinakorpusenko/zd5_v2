package com.example.megogo

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.megogo.databinding.FragmentMyTicketsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyTicketsFragment : Fragment() {

    private var _binding: FragmentMyTicketsBinding? = null
    private val binding get() = _binding!!

    private lateinit var ticketAdapter: MyTicketAdapter  // Используем MyTicketAdapter
    private lateinit var db: CinemaDatabase
    private lateinit var username: String
    private lateinit var pref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyTicketsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pref = requireContext().getSharedPreferences("PREF", Context.MODE_PRIVATE)
        db = DatabaseClient.getInstance(requireContext())
        username = pref.getString("Online", null).toString()

        // Используем MyTicketAdapter вместо TicketAdapter
        ticketAdapter = MyTicketAdapter(emptyList()) { ticket ->
            // Обработчик нажатия на кнопку "Вернуть средства"
            onRefundTicketClicked(ticket)
        }

        binding.recyclerViewTickets.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewTickets.adapter = ticketAdapter

        loadTickets()
    }

    private fun loadTickets() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Получаем список билетов, купленных текущим пользователем
            val tickets = db.ticketDao().getBoughtTicketsByUser(username)

            // Получаем подробную информацию о фильмах, связанных с билетами
            val ticketsWithMovies = tickets.map { ticket ->
                val movie = db.movieDao().getMovieById(ticket.movieId)
                TicketWithMovie(ticket, movie)
            }

            withContext(Dispatchers.Main) {
                // Обновляем адаптер с новыми данными
                ticketAdapter.updateTickets(ticketsWithMovies)
            }
        }
    }

    private fun onRefundTicketClicked(ticket: TicketWithMovie) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Отменяем покупку билета (обнуляем ownerUsername)
            db.ticketDao().refundTicket(ticket.ticket.id)

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Билет возвращен", Toast.LENGTH_SHORT).show()
                loadTickets()  // Перезагружаем список билетов
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
