package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {

    // Users
    suspend fun getUserByUsername(username: String): UserEntity? = appDao.getUserByUsername(username)
    suspend fun insertUser(user: UserEntity) = appDao.insertUser(user)

    // Save Slots
    fun getSaveSlotsForUser(username: String): Flow<List<SaveSlotEntity>> = appDao.getSaveSlotsForUser(username)
    suspend fun getSaveSlotById(slotId: String): SaveSlotEntity? = appDao.getSaveSlotById(slotId)
    fun getSaveSlotByIdFlow(slotId: String): Flow<SaveSlotEntity?> = appDao.getSaveSlotByIdFlow(slotId)
    suspend fun insertSaveSlot(slot: SaveSlotEntity) = appDao.insertSaveSlot(slot)
    suspend fun deleteSaveSlot(slotId: String) = appDao.deleteSaveSlot(slotId)
    suspend fun deleteFullSlotData(slotId: String) = appDao.deleteFullSlotData(slotId)

    // Teams
    fun getTeamsForSlot(slotId: String): Flow<List<TeamEntity>> = appDao.getTeamsForSlot(slotId)
    suspend fun getTeamsForSlotSync(slotId: String): List<TeamEntity> = appDao.getTeamsForSlotSync(slotId)
    suspend fun getTeamById(teamId: String): TeamEntity? = appDao.getTeamById(teamId)
    fun getTeamByIdFlow(teamId: String): Flow<TeamEntity?> = appDao.getTeamByIdFlow(teamId)
    suspend fun insertTeam(team: TeamEntity) = appDao.insertTeam(team)
    suspend fun insertTeams(teams: List<TeamEntity>) = appDao.insertTeams(teams)

    // Players
    fun getPlayersForSlot(slotId: String): Flow<List<PlayerEntity>> = appDao.getPlayersForSlot(slotId)
    suspend fun getPlayersForSlotSync(slotId: String): List<PlayerEntity> = appDao.getPlayersForSlotSync(slotId)
    fun getPlayersForTeam(teamId: String): Flow<List<PlayerEntity>> = appDao.getPlayersForTeam(teamId)
    suspend fun getPlayersForTeamSync(teamId: String): List<PlayerEntity> = appDao.getPlayersForTeamSync(teamId)
    suspend fun getPlayerById(playerId: String): PlayerEntity? = appDao.getPlayerById(playerId)
    suspend fun insertPlayer(player: PlayerEntity) = appDao.insertPlayer(player)
    suspend fun insertPlayers(players: List<PlayerEntity>) = appDao.insertPlayers(players)

    // Coaches
    fun getCoachesForSlot(slotId: String): Flow<List<CoachEntity>> = appDao.getCoachesForSlot(slotId)
    suspend fun getCoachesForSlotSync(slotId: String): List<CoachEntity> = appDao.getCoachesForSlotSync(slotId)
    fun getCoachesForTeam(teamId: String): Flow<List<CoachEntity>> = appDao.getCoachesForTeam(teamId)
    suspend fun getCoachesForTeamSync(teamId: String): List<CoachEntity> = appDao.getCoachesForTeamSync(teamId)
    suspend fun insertCoach(coach: CoachEntity) = appDao.insertCoach(coach)
    suspend fun insertCoaches(coaches: List<CoachEntity>) = appDao.insertCoaches(coaches)

    // Sponsors
    fun getSponsorsForSlot(slotId: String): Flow<List<SponsorEntity>> = appDao.getSponsorsForSlot(slotId)
    suspend fun getSponsorsForSlotSync(slotId: String): List<SponsorEntity> = appDao.getSponsorsForSlotSync(slotId)
    fun getSponsorsForTeam(teamId: String): Flow<List<SponsorEntity>> = appDao.getSponsorsForTeam(teamId)
    suspend fun insertSponsor(sponsor: SponsorEntity) = appDao.insertSponsor(sponsor)
    suspend fun insertSponsors(sponsors: List<SponsorEntity>) = appDao.insertSponsors(sponsors)

    // Finances
    fun getFinancesForSlot(slotId: String): Flow<List<FinanceEntity>> = appDao.getFinancesForSlot(slotId)
    suspend fun getFinancesForSlotSync(slotId: String): List<FinanceEntity> = appDao.getFinancesForSlotSync(slotId)
    suspend fun insertFinance(finance: FinanceEntity) = appDao.insertFinance(finance)
    suspend fun insertFinances(finances: List<FinanceEntity>) = appDao.insertFinances(finances)

    // Matches
    fun getMatchesForSlot(slotId: String): Flow<List<MatchEntity>> = appDao.getMatchesForSlot(slotId)
    suspend fun getMatchesForSlotSync(slotId: String): List<MatchEntity> = appDao.getMatchesForSlotSync(slotId)
    suspend fun insertMatch(match: MatchEntity) = appDao.insertMatch(match)
    suspend fun insertMatches(matches: List<MatchEntity>) = appDao.insertMatches(matches)

    // Trophies
    fun getTrophiesForSlot(slotId: String): Flow<List<TrophyEntity>> = appDao.getTrophiesForSlot(slotId)
    suspend fun getTrophiesForSlotSync(slotId: String): List<TrophyEntity> = appDao.getTrophiesForSlotSync(slotId)
    suspend fun insertTrophy(trophy: TrophyEntity) = appDao.insertTrophy(trophy)

    // Transfers
    fun getTransfersForSlot(slotId: String): Flow<List<TransferEntity>> = appDao.getTransfersForSlot(slotId)
    suspend fun getTransfersForSlotSync(slotId: String): List<TransferEntity> = appDao.getTransfersForSlotSync(slotId)
    suspend fun insertTransfer(transfer: TransferEntity) = appDao.insertTransfer(transfer)

    // News
    fun getNewsForSlot(slotId: String): Flow<List<NewsEntity>> = appDao.getNewsForSlot(slotId)
    suspend fun getNewsForSlotSync(slotId: String): List<NewsEntity> = appDao.getNewsForSlotSync(slotId)
    suspend fun insertNews(news: NewsEntity) = appDao.insertNews(news)
    suspend fun insertNewsList(newsList: List<NewsEntity>) = appDao.insertNewsList(newsList)

    // Achievements
    fun getAchievementsForUser(username: String): Flow<List<AchievementEntity>> = appDao.getAchievementsForUser(username)
    suspend fun insertAchievement(achievement: AchievementEntity) = appDao.insertAchievement(achievement)
    suspend fun insertAchievements(achievements: List<AchievementEntity>) = appDao.insertAchievements(achievements)

    // Notifications
    fun getNotificationsForSlot(slotId: String): Flow<List<NotificationEntity>> = appDao.getNotificationsForSlot(slotId)
    suspend fun getNotificationsForSlotSync(slotId: String): List<NotificationEntity> = appDao.getNotificationsForSlotSync(slotId)
    suspend fun insertNotification(notification: NotificationEntity) = appDao.insertNotification(notification)
    suspend fun markAllNotificationsRead(slotId: String) = appDao.markAllNotificationsRead(slotId)
}
