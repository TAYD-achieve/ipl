package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- Users ---
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // --- Save Slots ---
    @Query("SELECT * FROM save_slots WHERE username = :username ORDER BY slotNumber ASC")
    fun getSaveSlotsForUser(username: String): Flow<List<SaveSlotEntity>>

    @Query("SELECT * FROM save_slots WHERE slotId = :slotId")
    suspend fun getSaveSlotById(slotId: String): SaveSlotEntity?

    @Query("SELECT * FROM save_slots WHERE slotId = :slotId")
    fun getSaveSlotByIdFlow(slotId: String): Flow<SaveSlotEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaveSlot(slot: SaveSlotEntity)

    @Query("DELETE FROM save_slots WHERE slotId = :slotId")
    suspend fun deleteSaveSlot(slotId: String)

    // --- Teams ---
    @Query("SELECT * FROM teams WHERE slotId = :slotId")
    fun getTeamsForSlot(slotId: String): Flow<List<TeamEntity>>

    @Query("SELECT * FROM teams WHERE slotId = :slotId")
    suspend fun getTeamsForSlotSync(slotId: String): List<TeamEntity>

    @Query("SELECT * FROM teams WHERE teamId = :teamId")
    suspend fun getTeamById(teamId: String): TeamEntity?

    @Query("SELECT * FROM teams WHERE teamId = :teamId")
    fun getTeamByIdFlow(teamId: String): Flow<TeamEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: TeamEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeams(teams: List<TeamEntity>)

    // --- Players ---
    @Query("SELECT * FROM players WHERE slotId = :slotId")
    fun getPlayersForSlot(slotId: String): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE slotId = :slotId")
    suspend fun getPlayersForSlotSync(slotId: String): List<PlayerEntity>

    @Query("SELECT * FROM players WHERE teamId = :teamId")
    fun getPlayersForTeam(teamId: String): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE teamId = :teamId")
    suspend fun getPlayersForTeamSync(teamId: String): List<PlayerEntity>

    @Query("SELECT * FROM players WHERE playerId = :playerId")
    suspend fun getPlayerById(playerId: String): PlayerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayers(players: List<PlayerEntity>)

    // --- Coaches / Staff ---
    @Query("SELECT * FROM coaches WHERE slotId = :slotId")
    fun getCoachesForSlot(slotId: String): Flow<List<CoachEntity>>

    @Query("SELECT * FROM coaches WHERE slotId = :slotId")
    suspend fun getCoachesForSlotSync(slotId: String): List<CoachEntity>

    @Query("SELECT * FROM coaches WHERE teamId = :teamId")
    fun getCoachesForTeam(teamId: String): Flow<List<CoachEntity>>

    @Query("SELECT * FROM coaches WHERE teamId = :teamId")
    suspend fun getCoachesForTeamSync(teamId: String): List<CoachEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoach(coach: CoachEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoaches(coaches: List<CoachEntity>)

    // --- Sponsors ---
    @Query("SELECT * FROM sponsors WHERE slotId = :slotId")
    fun getSponsorsForSlot(slotId: String): Flow<List<SponsorEntity>>

    @Query("SELECT * FROM sponsors WHERE slotId = :slotId")
    suspend fun getSponsorsForSlotSync(slotId: String): List<SponsorEntity>

    @Query("SELECT * FROM sponsors WHERE teamId = :teamId")
    fun getSponsorsForTeam(teamId: String): Flow<List<SponsorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSponsor(sponsor: SponsorEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSponsors(sponsors: List<SponsorEntity>)

    // --- Finances ---
    @Query("SELECT * FROM finances WHERE slotId = :slotId ORDER BY timestamp DESC")
    fun getFinancesForSlot(slotId: String): Flow<List<FinanceEntity>>

    @Query("SELECT * FROM finances WHERE slotId = :slotId ORDER BY timestamp DESC")
    suspend fun getFinancesForSlotSync(slotId: String): List<FinanceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinance(finance: FinanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinances(finances: List<FinanceEntity>)

    // --- Matches ---
    @Query("SELECT * FROM matches WHERE slotId = :slotId ORDER BY season ASC, matchDay ASC, matchId ASC")
    fun getMatchesForSlot(slotId: String): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE slotId = :slotId ORDER BY season ASC, matchDay ASC, matchId ASC")
    suspend fun getMatchesForSlotSync(slotId: String): List<MatchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)

    // --- Trophies ---
    @Query("SELECT * FROM trophies WHERE slotId = :slotId ORDER BY season DESC")
    fun getTrophiesForSlot(slotId: String): Flow<List<TrophyEntity>>

    @Query("SELECT * FROM trophies WHERE slotId = :slotId ORDER BY season DESC")
    suspend fun getTrophiesForSlotSync(slotId: String): List<TrophyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrophy(trophy: TrophyEntity)

    // --- Transfers ---
    @Query("SELECT * FROM transfers WHERE slotId = :slotId ORDER BY timestamp DESC")
    fun getTransfersForSlot(slotId: String): Flow<List<TransferEntity>>

    @Query("SELECT * FROM transfers WHERE slotId = :slotId ORDER BY timestamp DESC")
    suspend fun getTransfersForSlotSync(slotId: String): List<TransferEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: TransferEntity)

    // --- News ---
    @Query("SELECT * FROM news WHERE slotId = :slotId ORDER BY timestamp DESC")
    fun getNewsForSlot(slotId: String): Flow<List<NewsEntity>>

    @Query("SELECT * FROM news WHERE slotId = :slotId ORDER BY timestamp DESC")
    suspend fun getNewsForSlotSync(slotId: String): List<NewsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(news: NewsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewsList(newsList: List<NewsEntity>)

    // --- Achievements ---
    @Query("SELECT * FROM achievements WHERE username = :username")
    fun getAchievementsForUser(username: String): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    // --- Notifications ---
    @Query("SELECT * FROM notifications WHERE slotId = :slotId ORDER BY timestamp DESC")
    fun getNotificationsForSlot(slotId: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE slotId = :slotId ORDER BY timestamp DESC")
    suspend fun getNotificationsForSlotSync(slotId: String): List<NotificationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE slotId = :slotId")
    suspend fun markAllNotificationsRead(slotId: String)

    // --- Clear Slot Data (Cascaded Deletions) ---
    @Query("DELETE FROM teams WHERE slotId = :slotId")
    suspend fun clearTeamsForSlot(slotId: String)

    @Query("DELETE FROM players WHERE slotId = :slotId")
    suspend fun clearPlayersForSlot(slotId: String)

    @Query("DELETE FROM coaches WHERE slotId = :slotId")
    suspend fun clearCoachesForSlot(slotId: String)

    @Query("DELETE FROM sponsors WHERE slotId = :slotId")
    suspend fun clearSponsorsForSlot(slotId: String)

    @Query("DELETE FROM finances WHERE slotId = :slotId")
    suspend fun clearFinancesForSlot(slotId: String)

    @Query("DELETE FROM matches WHERE slotId = :slotId")
    suspend fun clearMatchesForSlot(slotId: String)

    @Query("DELETE FROM trophies WHERE slotId = :slotId")
    suspend fun clearTrophiesForSlot(slotId: String)

    @Query("DELETE FROM transfers WHERE slotId = :slotId")
    suspend fun clearTransfersForSlot(slotId: String)

    @Query("DELETE FROM news WHERE slotId = :slotId")
    suspend fun clearNewsForSlot(slotId: String)

    @Query("DELETE FROM notifications WHERE slotId = :slotId")
    suspend fun clearNotificationsForSlot(slotId: String)

    @Transaction
    suspend fun deleteFullSlotData(slotId: String) {
        clearTeamsForSlot(slotId)
        clearPlayersForSlot(slotId)
        clearCoachesForSlot(slotId)
        clearSponsorsForSlot(slotId)
        clearFinancesForSlot(slotId)
        clearMatchesForSlot(slotId)
        clearTrophiesForSlot(slotId)
        clearTransfersForSlot(slotId)
        clearNewsForSlot(slotId)
        clearNotificationsForSlot(slotId)
        deleteSaveSlot(slotId)
    }
}
