package com.example.megogo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.megogo.R
import com.example.megogo.CinemaDatabase
import com.squareup.picasso.Picasso

//MovieAdapterAdm для экрана удаления фильмов
class MovieAdapterAdm(
    private val movies: MutableList<Movie>,
    private val onDeleteClick: (Movie) -> Unit
) : RecyclerView.Adapter<MovieAdapterAdm.MovieViewHolder>() {

    class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMovieTitle: TextView = view.findViewById(R.id.tvMovieTitle)
        val tvMovieGenre: TextView = view.findViewById(R.id.tvMovieGenre)
        val tvMovieYear: TextView = view.findViewById(R.id.tvMovieYear)
        val ivPoster: ImageView = view.findViewById(R.id.ivPoster)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movie_adm, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]
        holder.tvMovieTitle.text = movie.title
        holder.tvMovieGenre.text = movie.genre
        holder.tvMovieYear.text = movie.year

        // Загружаем изображение постера с помощью Picasso
        Picasso.get().load(movie.poster).placeholder(R.drawable.holder).into(holder.ivPoster)

        holder.btnDelete.setOnClickListener { onDeleteClick(movie) }
    }

    override fun getItemCount(): Int = movies.size
}


//MovieAdapter для экрана пользователя
data class MovieItem(val id: Long, val title: String, val genre: String, val posterUrl: String, val year: String, val plot: String)

class MovieAdapter(private var movieList: List<MovieItem>, private val onMovieClick: (MovieItem) -> Unit) :
    RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    inner class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.movie_title)
        val genre: TextView = view.findViewById(R.id.movie_genre)
        val poster: ImageView = view.findViewById(R.id.movie_poster)
        val yearText: TextView = view.findViewById(R.id.movie_year)
        val descText: TextView = view.findViewById(R.id.movie_desc)

        init {
            // Обработка клика по элементу фильма
            view.setOnClickListener {
                onMovieClick(movieList[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movieList[position]
        holder.title.text = movie.title
        holder.genre.text = movie.genre
        holder.yearText.text = movie.year
        holder.descText.text = movie.plot
        Picasso.get().load(movie.posterUrl).placeholder(R.drawable.holder).into(holder.poster)
    }

    override fun getItemCount(): Int = movieList.size

    fun updateMovies(newMovies: List<MovieItem>) {
        movieList = newMovies
        notifyDataSetChanged()
    }
}



class TicketAdapter(private var tickets: List<Ticket>, private val onTicketClick: (Ticket) -> Unit) :
    RecyclerView.Adapter<TicketAdapter.TicketViewHolder>() {

    inner class TicketViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val row: TextView = view.findViewById(R.id.ticketRow)
        val seat: TextView = view.findViewById(R.id.ticketSeat)
        val button: Button = view.findViewById(R.id.ticketButton)

        init {
            button.setOnClickListener {
                onTicketClick(tickets[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ticket, parent, false)
        return TicketViewHolder(view)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val ticket = tickets[position]
        holder.row.text = "Row: ${ticket.row}"
        holder.seat.text = "Seat: ${ticket.seat}"
        holder.button.text = if (ticket.ownerUsername == null) "Buy" else "Taken"
        holder.button.isEnabled = ticket.ownerUsername == null
    }

    override fun getItemCount(): Int = tickets.size

    fun updateTickets(newTickets: List<Ticket>) {
        tickets = newTickets
        notifyDataSetChanged()
    }
}



// Класс для объединения билета и фильма в один объект
data class TicketWithMovie(
    val ticket: Ticket,    // Информация о билете
    val movie: Movie?       // Информация о фильме
)

class MyTicketAdapter(
    private var ticketList: List<TicketWithMovie>,
    private val onRefundTicketClicked: (TicketWithMovie) -> Unit
) : RecyclerView.Adapter<MyTicketAdapter.TicketViewHolder>() {

    inner class TicketViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ticketInfo: TextView = view.findViewById(R.id.ticket_info)
        val movieTitle: TextView = view.findViewById(R.id.movie_title)
        val movieYear: TextView = view.findViewById(R.id.movie_year)
        val refundButton: Button = view.findViewById(R.id.refund_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        // Теперь используем my_item_ticket.xml
        val view = LayoutInflater.from(parent.context).inflate(R.layout.my_item_ticket, parent, false)
        return TicketViewHolder(view)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val ticketWithMovie = ticketList[position]

        // Отображаем информацию о билете
        holder.ticketInfo.text = "Row: ${ticketWithMovie.ticket.row}, Seat: ${ticketWithMovie.ticket.seat}"
        holder.movieTitle.text = ticketWithMovie.movie!!.title
        holder.movieYear.text = ticketWithMovie.movie.year

        // Обработчик нажатия на кнопку "Вернуть средства"
        holder.refundButton.setOnClickListener {
            onRefundTicketClicked(ticketWithMovie)
        }
    }

    override fun getItemCount(): Int = ticketList.size

    fun updateTickets(newTickets: List<TicketWithMovie>) {
        ticketList = newTickets
        notifyDataSetChanged()
    }
}