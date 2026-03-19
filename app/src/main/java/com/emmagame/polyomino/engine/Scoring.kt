package com.emmagame.polyomino.engine

import com.emmagame.polyomino.model.GameState

object Scoring {

    data class PlayerRanking(
        val playerIndex: Int,
        val name: String,
        val score: Int,
        val rank: Int,
        val isSharedColor: Boolean = false
    )

    fun calculateRankings(state: GameState): List<PlayerRanking> {
        // Separate ranked and shared-color players
        val ranked = state.players.filter { !it.isSharedColor }
        val shared = state.players.filter { it.isSharedColor }

        val sorted = ranked.sortedWith(compareBy({ it.score }, { it.index }))

        var currentRank = 1
        val rankings = sorted.mapIndexed { idx, player ->
            val rank = if (idx > 0 && sorted[idx - 1].score < player.score) {
                currentRank = idx + 1
                currentRank
            } else if (idx == 0) {
                1
            } else {
                currentRank
            }
            PlayerRanking(
                playerIndex = player.index,
                name = player.name,
                score = player.score,
                rank = rank
            )
        }.toMutableList()

        // Append shared color at the end (unranked)
        for (s in shared) {
            rankings.add(
                PlayerRanking(
                    playerIndex = s.index,
                    name = s.name,
                    score = s.score,
                    rank = 0,
                    isSharedColor = true
                )
            )
        }

        return rankings
    }
}
