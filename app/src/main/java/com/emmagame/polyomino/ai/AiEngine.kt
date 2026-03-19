package com.emmagame.polyomino.ai

import com.emmagame.polyomino.engine.PlacementValidator
import com.emmagame.polyomino.model.*
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.random.Random

data class ScoredMove(
    val move: Move,
    val score: Float
)

class AiEngine(
    private val random: Random = Random.Default
) {
    private val aiStates = mutableMapOf<Int, AiState>()

    fun resetStates() {
        aiStates.clear()
    }

    fun computeMove(state: GameState): Move? {
        val playerIndex = state.currentPlayerIndex
        val player = state.players[playerIndex]
        if (player.passed) return null

        val difficulty = state.difficulty
        val personality = AiPersonality.forPlayerIndex(playerIndex)
        val aiState = aiStates.getOrPut(playerIndex) { AiState() }

        // 1. Context gathering
        val opponentCorners = gatherOpponentCorners(state, playerIndex)
        val leaderIndex = findLeader(state, playerIndex)
        val gamePhase = getGamePhase(player)

        // 2. Frustration (easy levels)
        val effectiveFrustration = if (aiState.frustration > 0) {
            aiStates[playerIndex] = aiState.copy(frustration = aiState.frustration - 1)
            true
        } else false

        // 3. Move generation
        var candidates = generateMoves(state, playerIndex, difficulty, aiState)
        if (candidates.isEmpty()) {
            candidates = generateMovesExhaustive(state, playerIndex)
        }
        if (candidates.isEmpty()) return null

        // 4. Move evaluation
        val scored = candidates.map { move ->
            val score = evaluateMove(
                state, move, playerIndex, personality, difficulty,
                opponentCorners, leaderIndex, gamePhase, aiState, effectiveFrustration
            )
            ScoredMove(move, score)
        }

        // 5. Anti-pattern filter
        val filtered = filterImplausible(scored, gamePhase, difficulty)
        val finalCandidates = if (filtered.isEmpty()) scored else filtered

        // 6. Softmax selection
        val selected = softmaxSelect(finalCandidates, difficulty.temperature)

        // Update AI state
        if (selected != null) {
            val centerRow = selected.cells.sumOf { it.row } / selected.cells.size
            val centerCol = selected.cells.sumOf { it.col } / selected.cells.size
            val prevRow = aiState.lastPlacedRow
            val prevCol = aiState.lastPlacedCol
            val dir = if (prevRow >= 0) {
                (centerRow - prevRow) to (centerCol - prevCol)
            } else 0 to 0
            aiStates[playerIndex] = AiState(
                lastDirection = dir,
                frustration = 0,
                lastPlacedRow = centerRow,
                lastPlacedCol = centerCol
            )
        }

        return selected
    }

    // --- Move Generation ---

    private fun generateMoves(
        state: GameState,
        playerIndex: Int,
        difficulty: Difficulty,
        aiState: AiState
    ): List<Move> {
        val player = state.players[playerIndex]
        val cornerPositions = PlacementValidator.getCornerPositions(state, playerIndex)
        if (cornerPositions.isEmpty()) return emptyList()

        val moves = mutableListOf<Move>()
        val maxCandidates = difficulty.maxCandidates
        val localRadius = difficulty.localFocusRadius

        // Filter corners by local focus radius
        val filteredCorners = if (localRadius < 20 && aiState.lastPlacedRow >= 0) {
            cornerPositions.filter { corner ->
                abs(corner.row - aiState.lastPlacedRow) <= localRadius &&
                    abs(corner.col - aiState.lastPlacedCol) <= localRadius
            }.ifEmpty { cornerPositions }
        } else cornerPositions

        for (pieceIdx in Pieces.ALL.indices) {
            if (player.usedPieces[pieceIdx]) continue
            val piece = Pieces.ALL[pieceIdx]
            val orientations = PieceTransforms.getAllOrientations(piece.cells)

            for (orientation in orientations) {
                for (corner in filteredCorners) {
                    for (anchor in orientation) {
                        val placed = orientation.map { cell ->
                            Cell(
                                corner.row + cell.row - anchor.row,
                                corner.col + cell.col - anchor.col
                            )
                        }
                        if (PlacementValidator.isValidPlacement(state, playerIndex, placed)) {
                            moves.add(
                                Move(
                                    playerIndex = playerIndex,
                                    pieceIndex = pieceIdx,
                                    cells = placed
                                )
                            )
                            if (moves.size >= maxCandidates) return moves
                        }
                    }
                }
            }
        }
        return moves
    }

    private fun generateMovesExhaustive(
        state: GameState,
        playerIndex: Int
    ): List<Move> {
        val player = state.players[playerIndex]
        val cornerPositions = PlacementValidator.getCornerPositions(state, playerIndex)
        if (cornerPositions.isEmpty()) return emptyList()

        val moves = mutableListOf<Move>()
        for (pieceIdx in Pieces.ALL.indices) {
            if (player.usedPieces[pieceIdx]) continue
            val piece = Pieces.ALL[pieceIdx]
            val orientations = PieceTransforms.getAllOrientations(piece.cells)

            for (orientation in orientations) {
                for (corner in cornerPositions) {
                    for (anchor in orientation) {
                        val placed = orientation.map { cell ->
                            Cell(
                                corner.row + cell.row - anchor.row,
                                corner.col + cell.col - anchor.col
                            )
                        }
                        if (PlacementValidator.isValidPlacement(state, playerIndex, placed)) {
                            moves.add(
                                Move(
                                    playerIndex = playerIndex,
                                    pieceIndex = pieceIdx,
                                    cells = placed
                                )
                            )
                        }
                    }
                }
            }
        }
        return moves
    }

    // --- Move Evaluation ---

    private fun evaluateMove(
        state: GameState,
        move: Move,
        playerIndex: Int,
        personality: AiPersonality,
        difficulty: Difficulty,
        opponentCorners: Map<Int, Set<Cell>>,
        leaderIndex: Int,
        gamePhase: GamePhase,
        aiState: AiState,
        frustrated: Boolean
    ): Float {
        val board = state.board
        val boardCenter = board.size / 2f
        var score = 0f

        val piece = Pieces.ALL[move.pieceIndex]
        val cellCount = move.cells.size

        // Phase-dependent weights
        val phaseWeights = getPhaseWeights(gamePhase)

        // 1. Piece size
        score += cellCount * phaseWeights.sizeWeight * 2f

        // 2. New corners created
        val tempState = state.copy(
            board = board.withPlacements(move.cells, playerIndex),
            players = state.players.toMutableList().also {
                it[playerIndex] = it[playerIndex].withPiecePlaced(move.pieceIndex, cellCount)
            }
        )
        val newCorners = PlacementValidator.getCornerPositions(tempState, playerIndex)
        val oldCorners = PlacementValidator.getCornerPositions(state, playerIndex)
        val cornersCreated = newCorners.size - oldCorners.size

        // Weight corners by distance from center (closer = better for center-focused)
        val cornerCenterScore = newCorners.sumOf { corner ->
            val dist = abs(corner.row - boardCenter) + abs(corner.col - boardCenter)
            max(0.0, (boardCenter - dist).toDouble())
        }.toFloat() / board.size

        score += cornersCreated * phaseWeights.cornerWeight * personality.cornerBonus
        score += cornerCenterScore * personality.centerBonus

        // 3. Opponent corners destroyed (blocking)
        var blockingScore = 0f
        for ((oppIdx, oppCorners) in opponentCorners) {
            val destroyed = oppCorners.count { corner ->
                move.cells.any { cell ->
                    cell == corner || isOrthogonallyAdjacent(cell, corner)
                }
            }
            val multiplier = if (oppIdx == leaderIndex && personality == AiPersonality.AGGRESSIVE) {
                2.0f
            } else 1.0f
            blockingScore += destroyed * multiplier
        }
        score += blockingScore * phaseWeights.blockWeight * personality.blockingBonus

        // 4. Center distance
        val moveCenterRow = move.cells.sumOf { it.row }.toFloat() / cellCount
        val moveCenterCol = move.cells.sumOf { it.col }.toFloat() / cellCount
        val centerDist = abs(moveCenterRow - boardCenter) + abs(moveCenterCol - boardCenter)
        score -= centerDist * 0.1f * personality.centerBonus * phaseWeights.centerWeight

        // 5. Edge/quadrant affinity (for easy levels / defensive)
        if (personality.edgeBonus > 0.5f || difficulty.level <= 2) {
            val startPos = state.startPositions[playerIndex]
            val ownQuadrantDist = abs(moveCenterRow - startPos.row) + abs(moveCenterCol - startPos.col)
            score -= ownQuadrantDist * 0.05f * personality.edgeBonus
        }

        // 6. Territory estimation (medium+ difficulty)
        if (difficulty.level >= 3) {
            val territoryBefore = Territory.estimateTerritory(state, playerIndex)
            val territoryAfter = Territory.estimateTerritory(tempState, playerIndex)
            val territoryGain = territoryAfter - territoryBefore
            score += territoryGain * 0.15f * phaseWeights.territoryWeight
        }

        // 7. Spread/compactness preference
        if (aiState.lastPlacedRow >= 0) {
            val spreadDist = abs(moveCenterRow - aiState.lastPlacedRow) +
                abs(moveCenterCol - aiState.lastPlacedCol)
            score += spreadDist * 0.05f * personality.spreadBonus
        }

        // 8. Decision momentum
        if (aiState.lastDirection != 0 to 0) {
            val (dr, dc) = aiState.lastDirection
            val len = sqrt((dr * dr + dc * dc).toFloat())
            if (len > 0) {
                val moveDr = moveCenterRow - aiState.lastPlacedRow
                val moveDc = moveCenterCol - aiState.lastPlacedCol
                val alignment = (moveDr * dr + moveDc * dc) / len
                score += alignment * 0.08f
            }
        }

        // Frustration penalty (easy levels)
        if (frustrated && difficulty.level <= 2) {
            score -= random.nextFloat() * 3f
        }

        return score
    }

    // --- Filter & Selection ---

    private fun filterImplausible(
        candidates: List<ScoredMove>,
        gamePhase: GamePhase,
        difficulty: Difficulty
    ): List<ScoredMove> {
        // Skip filter with probability
        if (random.nextFloat() < difficulty.skipFilterChance) return candidates

        if (gamePhase == GamePhase.ENDGAME) return candidates

        val maxAvailableSize = candidates.maxOfOrNull { it.move.cells.size } ?: return candidates
        var filtered = candidates

        // Don't play small pieces when large ones are available
        if (maxAvailableSize >= 4) {
            val minAcceptable = when (gamePhase) {
                GamePhase.OPENING -> maxAvailableSize - 1
                GamePhase.MIDGAME -> 3
                else -> 1
            }
            val sizeFiltered = filtered.filter { it.move.cells.size >= minAcceptable }
            if (sizeFiltered.isNotEmpty()) filtered = sizeFiltered
        }

        // Prefer moves that create corners
        val withCorners = filtered.filter { it.score > 0 }
        if (withCorners.size >= 3) filtered = withCorners

        return filtered
    }

    private fun softmaxSelect(candidates: List<ScoredMove>, temperature: Float): Move? {
        if (candidates.isEmpty()) return null
        if (candidates.size == 1) return candidates[0].move

        val maxScore = candidates.maxOf { it.score }
        val weights = candidates.map { scored ->
            exp(((scored.score - maxScore) / max(temperature, 0.01f)).toDouble())
        }
        val totalWeight = weights.sum()
        var roll = random.nextDouble() * totalWeight

        for ((idx, weight) in weights.withIndex()) {
            roll -= weight
            if (roll <= 0) return candidates[idx].move
        }
        return candidates.last().move
    }

    // --- Helpers ---

    private fun gatherOpponentCorners(
        state: GameState,
        playerIndex: Int
    ): Map<Int, Set<Cell>> {
        val result = mutableMapOf<Int, Set<Cell>>()
        for (p in state.players) {
            if (p.index == playerIndex || p.passed) continue
            result[p.index] = PlacementValidator.getCornerPositions(state, p.index)
        }
        return result
    }

    private fun findLeader(state: GameState, excludeIndex: Int): Int {
        return state.players
            .filter { it.index != excludeIndex && !it.passed }
            .minByOrNull { it.score }?.index ?: 0
    }

    private fun isOrthogonallyAdjacent(a: Cell, b: Cell): Boolean {
        return (abs(a.row - b.row) + abs(a.col - b.col)) == 1
    }

    enum class GamePhase { OPENING, MIDGAME, ENDGAME }

    private fun getGamePhase(player: Player): GamePhase = when {
        player.piecesPlaced <= 4 -> GamePhase.OPENING
        player.piecesPlaced <= 13 -> GamePhase.MIDGAME
        else -> GamePhase.ENDGAME
    }

    data class PhaseWeights(
        val sizeWeight: Float,
        val cornerWeight: Float,
        val blockWeight: Float,
        val centerWeight: Float,
        val territoryWeight: Float
    )

    private fun getPhaseWeights(phase: GamePhase) = when (phase) {
        GamePhase.OPENING -> PhaseWeights(1.5f, 1.2f, 0.3f, 1.0f, 0.5f)
        GamePhase.MIDGAME -> PhaseWeights(1.0f, 1.0f, 1.0f, 0.8f, 1.0f)
        GamePhase.ENDGAME -> PhaseWeights(2.0f, 0.5f, 0.5f, 0.3f, 0.3f)
    }
}
