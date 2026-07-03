package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val username: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey val teamId: String, // format: "slotId_teamName" or "slotId_AI_1"
    val slotId: String,
    val name: String,
    val city: String,
    val logoText: String,
    val logoBgColor: String, // hex string
    val logoTextColor: String, // hex string
    val logoShape: String, // "CIRCLE", "SQUARE", "SHIELD", "STAR"
    val jerseyColorPrimary: String, // hex string
    val jerseyColorSecondary: String, // hex string
    val jerseyNumber: String,
    val jerseyPlayerName: String,
    val jerseyTemplate: Int, // 1, 2, 3
    val slogan: String,
    val stadiumName: String,
    val stadiumCapacity: Int, // 20000, 40000, 60000, 100000
    val stadiumVipStands: Int, // level 0-5
    val stadiumParking: Int, // level 0-5
    val stadiumFoodCourts: Int, // level 0-5
    val stadiumLighting: Int, // level 0-5
    val stadiumTrainingGrounds: Int, // level 0-5
    val stadiumMuseum: Int, // level 0-5
    val stadiumMerchShop: Int, // level 0-5
    val balance: Long, // in Rupees (INR)
    val reputation: Int, // 1 - 100
    val followers: Long,
    val fanHappiness: Int, // 1 - 100
    val isAi: Boolean,
    val aiShortName: String = ""
)

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey val playerId: String, // format: "slotId_playerName"
    val slotId: String,
    val teamId: String?, // can be null if free agent/unsold
    val name: String,
    val category: String, // "BATSMAN", "BOWLER", "ALL_ROUNDER", "WICKET_KEEPER"
    val batting: Int, // 1-100
    val bowling: Int, // 1-100
    val fielding: Int, // 1-100
    val fitness: Int, // 1-100
    val experience: Int, // 1-100
    val form: Int, // 1-100
    val leadership: Int, // 1-100
    val morale: Int, // 1-100
    val salary: Long,
    val contractDuration: Int, // remaining seasons
    val age: Int,
    val nationality: String,
    val isInjured: Boolean = false,
    val injuryDuration: Int = 0, // matches remaining
    // Lifetime statistics
    val matchesPlayed: Int = 0,
    val runs: Int = 0,
    val wickets: Int = 0,
    val strikeRate: Double = 0.0,
    val economy: Double = 0.0,
    val centuries: Int = 0,
    val fiveWickets: Int = 0,
    val isScouted: Boolean = false
)

val PlayerEntity.isStatsKnown: Boolean
    get() = this.isScouted || this.teamId != null

@Entity(tableName = "coaches")
data class CoachEntity(
    @PrimaryKey val coachId: String, // "slotId_role"
    val slotId: String,
    val teamId: String?,
    val name: String,
    val role: String, // "HEAD", "BATTING", "BOWLING", "FIELDING", "FITNESS", "PHYSIO", "SCOUT"
    val rating: Int, // 1-100
    val salary: Long,
    val isHired: Boolean = false
)

@Entity(tableName = "sponsors")
data class SponsorEntity(
    @PrimaryKey val sponsorId: String, // "slotId_type"
    val slotId: String,
    val teamId: String?,
    val name: String,
    val type: String, // "MAIN", "JERSEY", "STADIUM", "EQUIPMENT"
    val payoutPerSeason: Long,
    val isSigned: Boolean = false
)

@Entity(tableName = "finances")
data class FinanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val slotId: String,
    val type: String, // "INCOME", "EXPENSE"
    val category: String, // "TICKETS", "SPONSORS", "MERCHANDISE", "PRIZE_MONEY", "TV_RIGHTS", "TRANSFERS", "SALARIES", "MAINTENANCE", "TRAINING", "MEDICAL"
    val amount: Long,
    val season: Int,
    val matchDay: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val matchId: String, // "slotId_season_matchDay_teamA_teamB"
    val slotId: String,
    val season: Int,
    val matchDay: Int,
    val teamAId: String,
    val teamBId: String,
    val teamAScore: String = "", // e.g. "182/4 (20)"
    val teamBScore: String = "", // e.g. "178/6 (20)"
    val isPlayed: Boolean = false,
    val winnerId: String? = null,
    val commentary: String = "", // Semi-colon separated ball-by-ball events or match summary
    val tossWinnerId: String? = null,
    val tossDecision: String? = null, // "BAT" or "BOWL"
    val weather: String = "SUNNY", // "SUNNY", "OVERCAST", "RAIN"
    val isPlayoff: Boolean = false,
    val playoffType: String = "" // "Q1", "EL", "Q2", "FI"
)

@Entity(tableName = "trophies")
data class TrophyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val slotId: String,
    val teamId: String,
    val title: String, // e.g. "IPL Champion", "Orange Cap", "Purple Cap"
    val playerName: String? = null, // if player award
    val season: Int,
    val description: String
)

@Entity(tableName = "transfers")
data class TransferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val slotId: String,
    val playerName: String,
    val fromTeamName: String,
    val toTeamName: String,
    val transferFee: Long,
    val season: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "news")
data class NewsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val slotId: String,
    val title: String,
    val content: String,
    val category: String, // "TRANSFER", "PERFORMANCE", "INJURY", "COACH", "GENERAL"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String, // e.g. "first_win", "auction_master", "champion"
    val username: String,
    val title: String,
    val description: String,
    val unlockedAt: Long? = null // null means locked
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val slotId: String,
    val message: String,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "save_slots")
data class SaveSlotEntity(
    @PrimaryKey val slotId: String, // "username_1", "username_2", "username_3"
    val username: String,
    val slotNumber: Int, // 1, 2, 3
    val isCreated: Boolean = false,
    val currentSeason: Int = 1,
    val currentMatchDay: Int = 1, // 1 to 14, then 15 (Playoffs Q1/Eliminator), 16 (Q2), 17 (Final)
    val lastPlayedTime: Long = System.currentTimeMillis(),
    val isGameOver: Boolean = false
)
