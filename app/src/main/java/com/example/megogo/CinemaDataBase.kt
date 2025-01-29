package com.example.megogo

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object DatabaseClient {
    private var instance: CinemaDatabase? = null

    fun getInstance(context: Context): CinemaDatabase {
        if (instance == null) {
            synchronized(CinemaDatabase::class.java) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        CinemaDatabase::class.java,
                        "cinema_database"
                    )
                        .build()
                }
            }
        }
        return instance!!
    }
}



class MovieViewModel(application: Application) : AndroidViewModel(application) {

    private val movieDao: MovieDao = DatabaseClient.getInstance(application).movieDao()
    private val ticketDao: TicketDao = DatabaseClient.getInstance(application).ticketDao()
    private val repository: CinemaRepository = CinemaRepository(movieDao, ticketDao)

    private val _movieAdded = MutableLiveData<Boolean>()
    val movieAdded: LiveData<Boolean> get() = _movieAdded

    // Функция для добавления фильма с билетами
    fun addMovieWithTickets(movie: Movie) {
        viewModelScope.launch {
            try {
                repository.addMovieWithTickets(movie)  // Добавление фильма с билетами
                _movieAdded.postValue(true)
            } catch (e: Exception) {
                _movieAdded.postValue(false)
            }
        }
    }
}



class CinemaRepository(
    private val movieDao: MovieDao,
    private val ticketDao: TicketDao
) {
    suspend fun addMovieWithTickets(movie: Movie) {
        withContext(Dispatchers.IO) {
            val movieId = movieDao.insertMovie(movie)
            val tickets = mutableListOf<Ticket>()
            for (row in 1..5) { // 5 рядов
                for (seat in 1..10) { // 10 мест в ряду
                    tickets.add(
                        Ticket(
                            row = row,
                            seat = seat,
                            movieId = movieId,
                            ownerUsername = null
                        )
                    )
                }
            }
            ticketDao.insertTickets(tickets)
        }
    }
}



@Database(entities = [Movie::class, User::class, Ticket::class], version = 2)
abstract class CinemaDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun userDao(): UserDao
    abstract fun ticketDao(): TicketDao
}




@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMovie(movie: Movie): Long

    @Delete
    fun deleteMovie(movie: Movie)

    @Query("SELECT * FROM movies")
    fun getAllMovies(): List<Movie>

    @Query("SELECT * FROM movies WHERE title LIKE :query")
    fun searchMovies(query: String): List<Movie>

    @Query("SELECT * FROM movies WHERE title = :title LIMIT 1")
    fun getMovieByTitle(title: String): Movie?

    @Query("SELECT * FROM movies WHERE id = :movieId LIMIT 1")
    fun getMovieById(movieId: Long): Movie?
}

@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val genre: String,
    val description: String,
    val poster: String,
    val year: String
)



@Dao
interface UserDao {

    @Insert
    fun insertUser(user: User)

    @Update
    fun updateUser(user: User)

    @Delete
    fun deleteUser(user: User)

    @Query("SELECT * FROM users WHERE (username = :name OR email = :name) AND password = :password LIMIT 1")
    fun getUserByUsernameOrEmailAndPassword(name: String, password: String): User?

    @Query("SELECT * FROM users WHERE role = :role")
    fun getUsersByRole(role: String): List<User?>

    @Query("SELECT * FROM users WHERE username = :username OR email = :email LIMIT 1")
    fun getUserByUsernameOrEmail(username: String, email: String): User?
}



@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val email: String,
    val password: String,
    val role: String // Роль: "User" или "Admin"
)



@Entity(
    tableName = "tickets",
    foreignKeys = [
        ForeignKey(
            entity = Movie::class,
            parentColumns = ["id"],
            childColumns = ["movieId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["movieId"])]
)

data class Ticket(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val row: Int,
    val seat: Int,
    val movieId: Long,
    var ownerUsername: String? = null // Логин владельца или null, если билет не куплен
)



@Dao
interface TicketDao {
    @Insert
    fun insertTickets(tickets: List<Ticket>)

    @Delete
    fun deleteTicket(ticket: Ticket)

    @Query("UPDATE tickets SET ownerUsername = :username WHERE id = :ticketId")
    fun updateTicket(ticketId: Long, username: String)

    @Query("DELETE FROM tickets WHERE movieId = :movieId")
    fun deleteTicketsByMovieId(movieId: Long)

    @Query("SELECT * FROM tickets WHERE movieId = :movieId")
    fun getTicketsByMovieId(movieId: Long): List<Ticket>

    @Query("UPDATE tickets SET ownerUsername = :username WHERE id = :ticketId")
    fun updateTicketOwner(ticketId: Long, username: String)

    @Query("SELECT * FROM tickets WHERE ownerUsername = :username")
    fun getTicketsByOwner(username: String): List<Ticket>

    @Query("SELECT * FROM tickets WHERE movieId = :movieId")
    fun getTicketsByMovie(movieId: Long): List<Ticket>

    @Query("SELECT * FROM tickets WHERE ownerUsername = :username")
    fun getBoughtTicketsByUser(username: String): List<Ticket>

    @Query("UPDATE tickets SET ownerUsername = NULL WHERE id = :ticketId")
    fun refundTicket(ticketId: Long)
}
