package com.apktados.ruleta.data.remote

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.apktados.ruleta.data.remote.rest.FirebaseRestClient

class FirebaseGameRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    database: FirebaseDatabase = FirebaseDatabase.getInstance(
        "https://apktados-ruleta-default-rtdb.europe-west1.firebasedatabase.app"
    )
) {

    private val playersRef = database.reference.child("players")
    private val globalPrizeRef = database.reference.child("globalPrize")

    fun registerVictory(score: Int): Task<Void> {
        val user = auth.currentUser
            ?: return Tasks.forException(IllegalStateException("No authenticated Firebase user"))

        val playerRef = playersRef.child(user.uid)
        val now = System.currentTimeMillis()
        val result = TaskCompletionSource<Void>()

        // Uso transaccion para no perder victorias si dos escrituras llegan casi a la vez.
        playerRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val current = currentData.getValue(PlayerRemote::class.java)
                val updated = PlayerRemote(
                    uid = user.uid,
                    displayName = user.displayName,
                    email = user.email,
                    victories = (current?.victories ?: 0) + 1,
                    bestScore = maxOf(current?.bestScore ?: score, score),
                    lastScore = score,
                    updatedAt = now
                )

                currentData.value = updated
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                when {
                    error != null -> result.setException(error.toException())
                    !committed -> result.setException(
                        IllegalStateException("Firebase transaction was not committed")
                    )
                    else -> result.setResult(null)
                }
            }
        })

        return result.task
    }

    fun getTopTenPlayers(): Task<List<PlayerRemote>> {
        return playersRef.get().continueWith { task ->
            if (!task.isSuccessful) {
                throw task.exception ?: IllegalStateException("Could not load online ranking")
            }

            task.result.children
                .mapNotNull { snapshot -> snapshot.getValue(PlayerRemote::class.java) }
                .sortedWith(
                    compareByDescending<PlayerRemote> { it.bestScore }
                        .thenByDescending { it.victories }
                )
                .take(10)
        }
    }

    fun getGlobalPrize(): Task<GlobalPrizeRemote> {
        return globalPrizeRef.get().continueWith { task ->
            if (!task.isSuccessful) {
                throw task.exception ?: IllegalStateException("Could not load global prize")
            }

            task.result.getValue(GlobalPrizeRemote::class.java) ?: GlobalPrizeRemote()
        }
    }

    suspend fun getGlobalPrizeViaRest(): GlobalPrizeRemote {
        return FirebaseRestClient.api.getGlobalPrize()?.toRemote() ?: GlobalPrizeRemote()
    }

    fun increaseGlobalPrize(amountToAdd: Int = 5): Task<Void> {
        val result = TaskCompletionSource<Void>()
        val now = System.currentTimeMillis()

        // El bote es compartido, por eso se incrementa dentro de una transaccion.
        globalPrizeRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val current = currentData.getValue(GlobalPrizeRemote::class.java)
                    ?: GlobalPrizeRemote()

                currentData.value = current.copy(
                    amount = current.amount + amountToAdd,
                    updatedAt = now
                )
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                when {
                    error != null -> result.setException(error.toException())
                    !committed -> result.setException(
                        IllegalStateException("Firebase transaction was not committed")
                    )
                    else -> result.setResult(null)
                }
            }
        })

        return result.task
    }

    fun claimGlobalPrize(): Task<Int> {
        val user = auth.currentUser ?: return Tasks.forResult(0)
        val result = TaskCompletionSource<Int>()
        val now = System.currentTimeMillis()
        var claimedAmount = 0

        // Se lee y se pone a cero en la misma transaccion para que no lo cobren dos jugadores.
        globalPrizeRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val current = currentData.getValue(GlobalPrizeRemote::class.java)
                    ?: GlobalPrizeRemote()

                claimedAmount = current.amount
                currentData.value = current.copy(
                    amount = 0,
                    updatedAt = now,
                    lastWinnerUid = user.uid,
                    lastWinnerName = user.displayName ?: user.email,
                    lastClaimedAmount = claimedAmount
                )
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                when {
                    error != null -> result.setException(error.toException())
                    !committed -> result.setException(
                        IllegalStateException("Firebase transaction was not committed")
                    )
                    else -> result.setResult(claimedAmount)
                }
            }
        })

        return result.task
    }
}
