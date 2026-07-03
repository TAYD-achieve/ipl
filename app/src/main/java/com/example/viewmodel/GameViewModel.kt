package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.simulation.MatchEngine
import com.example.simulation.ScheduledMatchSimulationEngine
import com.example.simulation.SeasonSimulator
import com.example.simulation.SimGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import kotlin.random.Random

enum class Screen {
    Login,
    SlotSelection,
    FranchiseCreation,
    Dashboard,
    Squad,
    Auction,
    Stadium,
    Finances,
    Training,
    Staff,
    Scouting,
    MatchDay,
    Sponsors,
    Merchandise,
    TransferMarket,
    Achievements,
    AdminPanel
}

data class DailyObjective(
    val id: String,
    val title: String,
    val description: String,
    val rewardText: String,
    val rewardType: String, // "BUDGET", "FANS", "BOTH"
    val rewardBudget: Long = 0L,
    val rewardFans: Long = 0L,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AppRepository(db.appDao())

    // Central Progress Store & State Management Provider
    val progressStore = GameProgressStore(repository)

    // Firebase Sync Manager
    val firebaseSync = FirebaseSyncManager.getInstance(application)

    // Firebase Auth & Sync Inputs
    var firebaseEmail by mutableStateOf("")
    var firebasePassword by mutableStateOf("")
    var firebaseApiKeyInput by mutableStateOf(firebaseSync.apiKey)
    var firebaseProjectIdInput by mutableStateOf(firebaseSync.projectId)
    var firebaseStatusMsg by mutableStateOf("")
    var isFirebaseSyncLoading by mutableStateOf(false)
    var showFirebaseConfigDialog by mutableStateOf(false)

    // Navigation and User state
    var currentScreen by mutableStateOf(Screen.Login)
    var currentUser by mutableStateOf<String?>(null)
    var activeSlotId by mutableStateOf<String?>(null)
    var activeTeamId by mutableStateOf<String?>(null)

    // Local authentication helpers
    var loginError by mutableStateOf("")
    var resetSuccessMsg by mutableStateOf("")

    // Save slots
    private val _saveSlots = MutableStateFlow<List<SaveSlotEntity>>(emptyList())
    val saveSlots: StateFlow<List<SaveSlotEntity>> = _saveSlots.asStateFlow()

    // Active Game Save State
    var activeTeam by mutableStateOf<TeamEntity?>(null)
    var activePlayers = mutableStateListOf<PlayerEntity>()
    var allTeams = mutableStateListOf<TeamEntity>()
    var allPlayers = mutableStateListOf<PlayerEntity>()
    var coaches = mutableStateListOf<CoachEntity>()
    var sponsors = mutableStateListOf<SponsorEntity>()
    var finances = mutableStateListOf<FinanceEntity>()
    var matches = mutableStateListOf<MatchEntity>()
    var news = mutableStateListOf<NewsEntity>()
    var notifications = mutableStateListOf<NotificationEntity>()
    var trophies = mutableStateListOf<TrophyEntity>()
    var achievements = mutableStateListOf<AchievementEntity>()
    var pointsTable = mutableStateListOf<SeasonSimulator.PointsTableEntry>()

    // Auction Room State
    var auctionPlayer by mutableStateOf<PlayerEntity?>(null)
    var currentBidPrice by mutableStateOf(0L)
    var highestBidderId by mutableStateOf("")
    var auctionLog = mutableStateListOf<String>()
    var auctionEnded by mutableStateOf(false)
    var userBiddingEnabled by mutableStateOf(true)

    // Match Simulation State
    var currentMatchPlaying by mutableStateOf<MatchEntity?>(null)
    var matchXI = mutableStateListOf<PlayerEntity>()
    var matchCommentary = mutableStateListOf<String>()
    var matchResultSummary by mutableStateOf<MatchEngine.MatchSimulationResult?>(null)
    var matchInningsProgress by mutableStateOf(0) // 0: Toss, 1: Played, 2: Scorecard shown
    var userBattingTactic by mutableStateOf("NORMAL") // "NORMAL", "AGGRESSIVE", "DEFENSIVE"
    var userBowlingTactic by mutableStateOf("STANDARD") // "STANDARD", "ALL_OUT_ATTACK", "CONTAINMENT"

    // Academy & Sponsorship & Save Toast State
    val academyAssignedPlayers = mutableStateMapOf<String, String>() // playerId -> focus ("BATTING", "BOWLING", "ALL_ROUND")
    private val _saveToastMessage = MutableStateFlow<String?>(null)
    val saveToastMessage: StateFlow<String?> = _saveToastMessage.asStateFlow()

    // Daily Objectives & Morale State
    val dailyObjectives = mutableStateListOf<DailyObjective>()
    var teamMoraleScore by mutableStateOf(80)
    var lastSavedTimestamp by mutableStateOf<String>("Automatic (Sync Active)")
    var autoSaveEnabled by mutableStateOf(true)

    fun initDailyObjectives() {
        if (dailyObjectives.isNotEmpty()) return
        dailyObjectives.clear()
        dailyObjectives.add(
            DailyObjective(
                id = "OBJ_WIN",
                title = "Matchday Triumph",
                description = "Win a simulation match (Home or Away)",
                rewardText = "+₹1.5 Cr & +15,000 Fans",
                rewardType = "BOTH",
                rewardBudget = 15000000L,
                rewardFans = 15000L
            )
        )
        dailyObjectives.add(
            DailyObjective(
                id = "OBJ_SIGN",
                title = "Squad Reinforcement",
                description = "Sign a player from auction or transfer market",
                rewardText = "+₹1.0 Cr Budget Bonus",
                rewardType = "BUDGET",
                rewardBudget = 10000000L
            )
        )
        dailyObjectives.add(
            DailyObjective(
                id = "OBJ_FACILITY",
                title = "Franchise Expansion",
                description = "Upgrade stadium capacity or any facility",
                rewardText = "+25,000 Fan Base Growth",
                rewardType = "FANS",
                rewardFans = 25000L
            )
        )
    }

    fun markObjectiveCompleted(objId: String) {
        val idx = dailyObjectives.indexOfFirst { it.id == objId }
        if (idx != -1) {
            val current = dailyObjectives[idx]
            if (!current.isCompleted) {
                dailyObjectives[idx] = current.copy(isCompleted = true)
                triggerSaveToast("🎯 Daily Objective Completed: ${current.title}! Claim reward on Dashboard.")
            }
        }
    }

    fun claimObjectiveReward(objId: String) {
        val idx = dailyObjectives.indexOfFirst { it.id == objId }
        if (idx != -1) {
            val obj = dailyObjectives[idx]
            if (obj.isCompleted && !obj.isClaimed) {
                dailyObjectives[idx] = obj.copy(isClaimed = true)
                val team = activeTeam ?: return
                viewModelScope.launch {
                    val upTeam = team.copy(
                        balance = team.balance + obj.rewardBudget,
                        followers = team.followers + obj.rewardFans
                    )
                    repository.insertTeam(upTeam)
                    if (obj.rewardBudget > 0) {
                        repository.insertFinance(
                            FinanceEntity(
                                slotId = team.slotId,
                                type = "INCOME",
                                category = "SPONSORS",
                                amount = obj.rewardBudget,
                                season = progressStore.currentSeason.value,
                                matchDay = progressStore.currentMatchDay.value
                            )
                        )
                    }
                    loadGameData(team.slotId)
                    triggerSaveToast("🎁 Reward Claimed! ${obj.rewardText} added to your franchise!")
                }
            }
        }
    }

    fun calculateAndSyncTeamMorale() {
        val teamPlayers = activePlayers
        if (teamPlayers.isNotEmpty()) {
            teamMoraleScore = teamPlayers.map { it.morale }.average().toInt()
        }
    }

    fun triggerMoraleEvent(reason: String, delta: Int) {
        val team = activeTeam ?: return
        viewModelScope.launch {
            val updatedPlayers = activePlayers.map { p ->
                p.copy(morale = (p.morale + delta).coerceIn(20, 100))
            }
            repository.insertPlayers(updatedPlayers)
            calculateAndSyncTeamMorale()
            val signStr = if (delta >= 0) "+$delta" else "$delta"
            repository.insertNotification(
                NotificationEntity(
                    slotId = team.slotId,
                    message = "🔥 Morale Event ($reason): Squad morale adjusted by $signStr%! (Current Average: $teamMoraleScore%)"
                )
            )
        }
    }

    fun conductSquadRetreat(cost: Long = 10000000L) {
        val team = activeTeam ?: return
        if (team.balance < cost) {
            triggerSaveToast("⚠️ Insufficient funds for Squad Retreat (Needs ₹1.0 Cr).")
            return
        }
        viewModelScope.launch {
            val updatedPlayers = activePlayers.map { p ->
                p.copy(
                    morale = (p.morale + 15).coerceAtMost(100),
                    fitness = (p.fitness + 10).coerceAtMost(100)
                )
            }
            repository.insertPlayers(updatedPlayers)
            repository.insertTeam(team.copy(balance = team.balance - cost))
            repository.insertFinance(
                FinanceEntity(
                    slotId = team.slotId,
                    type = "EXPENSE",
                    category = "TRAINING",
                    amount = cost,
                    season = progressStore.currentSeason.value,
                    matchDay = progressStore.currentMatchDay.value
                )
            )
            calculateAndSyncTeamMorale()
            loadGameData(team.slotId)
            markObjectiveCompleted("OBJ_SIGN")
            triggerSaveToast("🧘 Whole Squad Retreat completed! All players gained +15 Morale and +10 Fitness!")
        }
    }

    fun saveGameProgressManually() {
        viewModelScope.launch {
            progressStore.saveProgressSnapshot()
            val timeStr = java.text.SimpleDateFormat("hh:mm:ss a", java.util.Locale.getDefault()).format(java.util.Date())
            lastSavedTimestamp = "Saved at $timeStr"
            triggerSaveToast("💾 Game state manually saved to Room SQLite database ($timeStr)!")
        }
    }

    fun resetCurrentFranchiseProgress(onComplete: () -> Unit) {
        val slotId = activeSlotId ?: return
        val team = activeTeam ?: return
        viewModelScope.launch {
            val tName = team.name
            val tCity = team.city
            val tLogoText = team.logoText
            val tLogoBg = team.logoBgColor
            val tLogoTextCol = team.logoTextColor
            val tLogoShape = team.logoShape
            val tJerPri = team.jerseyColorPrimary
            val tJerSec = team.jerseyColorSecondary
            val tJerNum = team.jerseyNumber
            val tJerPlName = team.jerseyPlayerName
            val tJerTemp = team.jerseyTemplate
            val tSlogan = team.slogan
            val tStad = team.stadiumName
            val slotNum = saveSlots.value.firstOrNull { it.slotId == slotId }?.slotNumber ?: 1

            repository.deleteFullSlotData(slotId)

            val newSlot = SaveSlotEntity(
                slotId = slotId,
                username = currentUser ?: "Player",
                slotNumber = slotNum,
                isCreated = true,
                currentSeason = 1,
                currentMatchDay = 1,
                lastPlayedTime = System.currentTimeMillis()
            )
            repository.insertSaveSlot(newSlot)

            val resetTeam = TeamEntity(
                teamId = "${slotId}_${tName.replace(" ", "_")}",
                slotId = slotId,
                name = tName,
                city = tCity,
                logoText = tLogoText,
                logoBgColor = tLogoBg,
                logoTextColor = tLogoTextCol,
                logoShape = tLogoShape,
                jerseyColorPrimary = tJerPri,
                jerseyColorSecondary = tJerSec,
                jerseyNumber = tJerNum,
                jerseyPlayerName = tJerPlName,
                jerseyTemplate = tJerTemp,
                slogan = tSlogan,
                stadiumName = tStad,
                stadiumCapacity = 20000,
                stadiumVipStands = 1,
                stadiumParking = 1,
                stadiumFoodCourts = 1,
                stadiumLighting = 1,
                stadiumTrainingGrounds = 1,
                stadiumMuseum = 0,
                stadiumMerchShop = 0,
                balance = 1200000000L,
                reputation = 50,
                followers = 500000L,
                fanHappiness = 80,
                isAi = false
            )
            repository.insertTeam(resetTeam)

            val aiTeams = SimGenerator.generateAiTeams(slotId)
            repository.insertTeams(aiTeams)

            val initialPlayers = SimGenerator.generateInitialPlayers(slotId)
            repository.insertPlayers(initialPlayers)

            val initialCoaches = SimGenerator.generateCoaches(slotId)
            repository.insertCoaches(initialCoaches)

            val initialSponsors = SimGenerator.generateSponsors(slotId)
            repository.insertSponsors(initialSponsors)

            val allTeamsForSlot = listOf(resetTeam) + aiTeams
            val schedule = SeasonSimulator.generateSchedule(slotId, allTeamsForSlot)
            repository.insertMatches(schedule)

            initDailyObjectives()
            loadGameData(slotId)
            triggerSaveToast("🔄 Career Progress Reset to Season 1 MatchDay 1! Team branding preserved.")
            onComplete()
        }
    }

    fun wipeSlotAndExit(onExit: () -> Unit) {
        val slotId = activeSlotId ?: return
        viewModelScope.launch {
            repository.deleteFullSlotData(slotId)
            activeTeam = null
            activeSlotId = null
            loadSaveSlots(currentUser ?: "Player")
            triggerSaveToast("🗑️ Save Slot Wiped. Returning to slot selection.")
            onExit()
        }
    }

    fun triggerSaveToast(customMsg: String = "💾 Game state successfully saved to localStorage (Room SQLite)") {
        if (!autoSaveEnabled && customMsg.contains("saved to localStorage", ignoreCase = true)) {
            val disabledMsg = "⚠️ Auto-Save OFF: Action applied in session memory. Use Manual Save in Admin to persist to Room SQLite!"
            viewModelScope.launch {
                _saveToastMessage.value = disabledMsg
                delay(3500)
                if (_saveToastMessage.value == disabledMsg) {
                    _saveToastMessage.value = null
                }
            }
            return
        }
        viewModelScope.launch {
            _saveToastMessage.value = customMsg
            delay(3500)
            if (_saveToastMessage.value == customMsg) {
                _saveToastMessage.value = null
            }
        }
    }

    // Registration, Login, and Auth
    fun registerUser(user: String, pass: String): Boolean {
        if (user.isBlank() || pass.isBlank()) {
            loginError = "Username and password cannot be empty."
            return false
        }
        viewModelScope.launch {
            val exists = repository.getUserByUsername(user)
            if (exists != null) {
                loginError = "Username already exists."
            } else {
                val hashed = hashPassword(pass)
                repository.insertUser(UserEntity(user, hashed))
                loginError = ""
                currentUser = user
                loadSaveSlots(user)
                currentScreen = Screen.SlotSelection
            }
        }
        return true
    }

    fun loginUser(user: String, pass: String) {
        if (user.isBlank() || pass.isBlank()) {
            loginError = "Please fill in all fields."
            return
        }
        viewModelScope.launch {
            val userEntity = repository.getUserByUsername(user)
            if (userEntity == null) {
                loginError = "Invalid username or password."
            } else {
                val hashed = hashPassword(pass)
                if (userEntity.passwordHash == hashed) {
                    loginError = ""
                    currentUser = user
                    loadSaveSlots(user)
                    currentScreen = Screen.SlotSelection
                } else {
                    loginError = "Invalid username or password."
                }
            }
        }
    }

    fun resetPassword(user: String, newPass: String) {
        if (user.isBlank() || newPass.isBlank()) {
            loginError = "Please fill in all fields."
            return
        }
        viewModelScope.launch {
            val userEntity = repository.getUserByUsername(user)
            if (userEntity == null) {
                loginError = "Username not found."
            } else {
                val hashed = hashPassword(newPass)
                repository.insertUser(userEntity.copy(passwordHash = hashed))
                resetSuccessMsg = "Password reset successful! Please log in."
                loginError = ""
            }
        }
    }

    // --- Firebase Auth & Cloud Sync Methods ---
    fun instantOfflinePlay() {
        val localUser = "guest_manager"
        currentUser = localUser
        loadSaveSlots(localUser)
        currentScreen = Screen.SlotSelection
    }

    fun firebaseSignUp() {
        if (firebaseEmail.isBlank() || firebasePassword.isBlank()) {
            firebaseStatusMsg = "Please enter email and password."
            return
        }
        isFirebaseSyncLoading = true
        firebaseStatusMsg = "Creating cloud account..."
        viewModelScope.launch {
            firebaseSync.apiKey = firebaseApiKeyInput
            firebaseSync.projectId = firebaseProjectIdInput
            val result = firebaseSync.signUp(firebaseEmail, firebasePassword)
            isFirebaseSyncLoading = false
            firebaseStatusMsg = result.fold(
                onSuccess = { "Cloud account created & logged in!" },
                onFailure = { it.message ?: "Creation failed." }
            )
            if (result.isSuccess) {
                val cloudEmail = firebaseSync.email ?: firebaseEmail.ifBlank { "manager@ipltycoon.com" }
                val localUser = cloudEmail.substringBefore("@")
                currentUser = localUser
                loadSaveSlots(localUser)
                currentScreen = Screen.SlotSelection
            }
        }
    }

    fun firebaseSignIn() {
        if (firebaseEmail.isBlank() || firebasePassword.isBlank()) {
            firebaseStatusMsg = "Please enter email and password."
            return
        }
        isFirebaseSyncLoading = true
        firebaseStatusMsg = "Logging into cloud..."
        viewModelScope.launch {
            firebaseSync.apiKey = firebaseApiKeyInput
            firebaseSync.projectId = firebaseProjectIdInput
            val result = firebaseSync.signIn(firebaseEmail, firebasePassword)
            isFirebaseSyncLoading = false
            firebaseStatusMsg = result.fold(
                onSuccess = { "Logged in successfully to Cloud!" },
                onFailure = { it.message ?: "Login failed." }
            )
            if (result.isSuccess) {
                val cloudEmail = firebaseSync.email ?: firebaseEmail.ifBlank { "manager@ipltycoon.com" }
                val localUser = cloudEmail.substringBefore("@")
                currentUser = localUser
                loadSaveSlots(localUser)
                currentScreen = Screen.SlotSelection
            }
        }
    }

    fun firebaseUploadSlot(slotId: String) {
        if (!firebaseSync.isLoggedIn) {
            firebaseStatusMsg = "Please login first to upload."
            return
        }
        isFirebaseSyncLoading = true
        firebaseStatusMsg = "Uploading slot to cloud..."
        viewModelScope.launch {
            val syncData = firebaseSync.prepareSlotSyncData(slotId, repository)
            val result = firebaseSync.uploadSlotData(slotId, syncData)
            isFirebaseSyncLoading = false
            firebaseStatusMsg = result.fold(
                onSuccess = { "Slot backup completed successfully!" },
                onFailure = { "Upload failed: ${it.message}" }
            )
        }
    }

    fun firebaseDownloadSlot(slotId: String) {
        if (!firebaseSync.isLoggedIn) {
            firebaseStatusMsg = "Please login first to download."
            return
        }
        isFirebaseSyncLoading = true
        firebaseStatusMsg = "Fetching slot from cloud..."
        viewModelScope.launch {
            val result = firebaseSync.downloadSlotData(slotId)
            isFirebaseSyncLoading = false
            if (result.isSuccess) {
                val data = result.getOrNull()
                if (data != null) {
                    firebaseSync.restoreSlotSyncData(data, repository)
                    firebaseStatusMsg = "Slot restored successfully from cloud!"
                    if (activeSlotId == slotId) {
                        loadGameData(slotId)
                    } else if (currentUser != null) {
                        loadSaveSlots(currentUser!!)
                    }
                } else {
                    firebaseStatusMsg = "No cloud save found for this slot."
                }
            } else {
                firebaseStatusMsg = "Download failed: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    private fun loadSaveSlots(username: String) {
        viewModelScope.launch {
            repository.getSaveSlotsForUser(username).collect { slots ->
                _saveSlots.value = slots
                if (slots.isEmpty()) {
                    // Prepopulate 3 slots
                    for (i in 1..3) {
                        repository.insertSaveSlot(
                            SaveSlotEntity(
                                slotId = "${username}_slot_$i",
                                username = username,
                                slotNumber = i,
                                isCreated = false
                            )
                        )
                    }
                }
            }
        }
    }

    fun selectSaveSlot(slot: SaveSlotEntity) {
        activeSlotId = slot.slotId
        if (slot.isCreated) {
            // Load game data
            loadGameData(slot.slotId)
            currentScreen = Screen.Dashboard
        } else {
            // New Franchise creation
            currentScreen = Screen.FranchiseCreation
        }
    }

    fun deleteSaveSlot(slot: SaveSlotEntity) {
        viewModelScope.launch {
            repository.deleteFullSlotData(slot.slotId)
            // Re-create the empty slot
            repository.insertSaveSlot(
                SaveSlotEntity(
                    slotId = slot.slotId,
                    username = slot.username,
                    slotNumber = slot.slotNumber,
                    isCreated = false
                )
            )
            currentUser?.let { loadSaveSlots(it) }
        }
    }

    private fun hashPassword(password: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            password.hashCode().toString()
        }
    }

    // Franchise Creation
    fun createFranchise(
        name: String,
        city: String,
        slogan: String,
        stadium: String,
        logoText: String,
        logoBg: String,
        logoTextCol: String,
        logoShape: String,
        jerseyPrimary: String,
        jerseySecondary: String,
        jerseyNum: String,
        jerseyPlName: String,
        jerseyTemplate: Int
    ) {
        val slotId = activeSlotId ?: return
        val username = currentUser ?: return

        viewModelScope.launch {
            // Create user team
            val userTeamId = "${slotId}_USER"
            val newTeam = TeamEntity(
                teamId = userTeamId,
                slotId = slotId,
                name = name,
                city = city,
                logoText = logoText.ifBlank { name.take(2).uppercase() },
                logoBgColor = logoBg,
                logoTextColor = logoTextCol,
                logoShape = logoShape,
                jerseyColorPrimary = jerseyPrimary,
                jerseyColorSecondary = jerseySecondary,
                jerseyNumber = jerseyNum,
                jerseyPlayerName = jerseyPlName,
                jerseyTemplate = jerseyTemplate,
                slogan = slogan,
                stadiumName = stadium.ifBlank { "$city Arena" },
                stadiumCapacity = 20000, // starting Small capacity
                stadiumVipStands = 1,
                stadiumParking = 1,
                stadiumFoodCourts = 1,
                stadiumLighting = 1,
                stadiumTrainingGrounds = 1,
                stadiumMuseum = 0,
                stadiumMerchShop = 0,
                balance = 1200000000L, // 120 Crores starting cash
                reputation = 50,
                followers = 500000L,
                fanHappiness = 80,
                isAi = false
            )

            repository.insertTeam(newTeam)

            // Generate AI teams
            val aiTeams = SimGenerator.generateAiTeams(slotId)
            repository.insertTeams(aiTeams)

            // Generate initial players, coaches, sponsors
            val initialPlayers = SimGenerator.generateInitialPlayers(slotId)
            repository.insertPlayers(initialPlayers)

            val initialCoaches = SimGenerator.generateCoaches(slotId)
            repository.insertCoaches(initialCoaches)

            val initialSponsors = SimGenerator.generateSponsors(slotId)
            repository.insertSponsors(initialSponsors)

            // Generate season schedule
            val allTeamsForSlot = listOf(newTeam) + aiTeams
            val schedule = SeasonSimulator.generateSchedule(slotId, allTeamsForSlot)
            repository.insertMatches(schedule)

            // Update slot status
            val currentSlot = repository.getSaveSlotById(slotId)
            if (currentSlot != null) {
                repository.insertSaveSlot(currentSlot.copy(isCreated = true, lastPlayedTime = System.currentTimeMillis()))
            }

            // Create dynamic news
            repository.insertNews(
                NewsEntity(
                    slotId = slotId,
                    title = "$name Franchise Formed!",
                    content = "A new era of cricket dawns as the $name from $city joins the league! Fans are ecstatic about the $stadium stadium construction.",
                    category = "GENERAL"
                )
            )

            // Pre-seed locked achievements for profile
            seedAchievements(username)

            // Load and switch
            activeTeamId = userTeamId
            loadGameData(slotId)
            currentScreen = Screen.Dashboard
        }
    }

    private fun seedAchievements(username: String) {
        viewModelScope.launch {
            val achievementsList = listOf(
                AchievementEntity("first_win", username, "First Victory", "Win your first cricket match in the league."),
                AchievementEntity("auction_master", username, "Auction Master", "Sign an Elite tier player with batting/bowling rating over 90."),
                AchievementEntity("stadium_mega", username, "Mega Fortress", "Upgrade stadium capacity to Mega (100,000 capacity)."),
                AchievementEntity("richest_franchise", username, "Trillionaire", "Amass a franchise balance of over 1.2 Billion INR (120 Crores)."),
                AchievementEntity("championship_title", username, "League Champion", "Win the grand finale and lift the championship trophy!")
            )
            repository.insertAchievements(achievementsList)
        }
    }

    // Load active Slot State
    fun loadGameData(slotId: String) {
        initDailyObjectives()
        progressStore.bindToSlot(slotId, currentUser ?: "", viewModelScope)
        viewModelScope.launch {
            repository.getTeamsForSlot(slotId).collect { teams ->
                allTeams.clear()
                allTeams.addAll(teams)
                val userT = teams.find { !it.isAi }
                activeTeam = userT
                activeTeamId = userT?.teamId
            }
        }
        viewModelScope.launch {
            repository.getPlayersForSlot(slotId).collect { players ->
                allPlayers.clear()
                allPlayers.addAll(players)
                activePlayers.clear()
                activePlayers.addAll(players.filter { it.teamId == activeTeamId })
                calculateAndSyncTeamMorale()
            }
        }
        viewModelScope.launch {
            repository.getCoachesForSlot(slotId).collect { items ->
                coaches.clear()
                coaches.addAll(items)
            }
        }
        viewModelScope.launch {
            repository.getSponsorsForSlot(slotId).collect { items ->
                sponsors.clear()
                sponsors.addAll(items)
            }
        }
        viewModelScope.launch {
            repository.getFinancesForSlot(slotId).collect { items ->
                finances.clear()
                finances.addAll(items)
            }
        }
        viewModelScope.launch {
            repository.getMatchesForSlot(slotId).collect { items ->
                matches.clear()
                matches.addAll(items)
                
                // Re-calculate points table
                val table = SeasonSimulator.calculatePointsTable(allTeams, items)
                pointsTable.clear()
                pointsTable.addAll(table)

                if (activeTeamId != null && items.any { it.isPlayed && it.winnerId == activeTeamId }) {
                    markObjectiveCompleted("OBJ_WIN")
                }
            }
        }
        viewModelScope.launch {
            repository.getNewsForSlot(slotId).collect { items ->
                news.clear()
                news.addAll(items)
            }
        }
        viewModelScope.launch {
            repository.getNotificationsForSlot(slotId).collect { items ->
                notifications.clear()
                notifications.addAll(items)
            }
        }
        viewModelScope.launch {
            currentUser?.let { user ->
                repository.getAchievementsForUser(user).collect { items ->
                    achievements.clear()
                    achievements.addAll(items)
                }
            }
        }
    }

    // Auction Room Logic
    fun calculatePlayerPerformanceValuation(player: PlayerEntity): Long {
        var baseVal = player.salary
        
        // Performance stats boosts
        if (player.matchesPlayed > 0) {
            val avgRuns = player.runs.toDouble() / player.matchesPlayed
            if (avgRuns > 30.0) baseVal += 20000000L
            if (avgRuns > 45.0) baseVal += 35000000L
            if (player.strikeRate > 135.0) baseVal += 15000000L
            if (player.strikeRate > 150.0) baseVal += 25000000L
            baseVal += (player.centuries * 15000000L)
            
            val avgWickets = player.wickets.toDouble() / player.matchesPlayed
            if (avgWickets > 1.2) baseVal += 20000000L
            if (avgWickets > 1.8) baseVal += 35000000L
            if (player.economy > 0.0 && player.economy < 7.5) baseVal += 15000000L
            if (player.economy > 0.0 && player.economy < 6.5) baseVal += 25000000L
            baseVal += (player.fiveWickets * 20000000L)
        }
        
        // Form and Experience boosts
        if (player.form >= 80) baseVal += 15000000L
        if (player.experience >= 80) baseVal += 10000000L
        
        // Overall rating boost
        val maxSkill = maxOf(player.batting, player.bowling)
        if (maxSkill >= 85) baseVal += 30000000L
        if (maxSkill >= 90) baseVal += 50000000L
        
        return baseVal.coerceAtLeast(player.salary)
    }

    fun simulateRemainingAuctions() {
        val unassigned = allPlayers.filter { it.teamId == null }
        if (unassigned.isEmpty()) return
        
        viewModelScope.launch {
            val logSummary = mutableListOf<String>()
            logSummary.add("⚡ STATS-DRIVEN AUCTION SIMULATION RESULTS:")
            
            for (player in unassigned) {
                val valuation = calculatePlayerPerformanceValuation(player)
                val eligibleAiTeams = allTeams.filter { it.isAi && it.balance >= player.salary }
                if (eligibleAiTeams.isNotEmpty()) {
                    val interestedTeams = eligibleAiTeams.filter { it.balance >= valuation * 0.75 }
                    val winnerTeam = if (interestedTeams.isNotEmpty()) interestedTeams.random() else eligibleAiTeams.random()
                    
                    val finalPrice = if (interestedTeams.size > 1) {
                        (valuation * Random.nextDouble(0.85, 1.15)).toLong().coerceIn(player.salary, winnerTeam.balance)
                    } else {
                        player.salary
                    }
                    
                    val updatedTeam = winnerTeam.copy(balance = winnerTeam.balance - finalPrice)
                    repository.insertTeam(updatedTeam)
                    
                    val updatedPlayer = player.copy(teamId = winnerTeam.teamId, salary = finalPrice, isScouted = true)
                    repository.insertPlayer(updatedPlayer)
                    
                    repository.insertFinance(
                        FinanceEntity(
                            slotId = player.slotId,
                            type = "EXPENSE",
                            category = "TRANSFERS",
                            amount = finalPrice,
                            season = 1,
                            matchDay = 1
                        )
                    )
                    logSummary.add("✅ ${player.name} -> ${winnerTeam.name} for ₹${formatCurrency(finalPrice)} (Val: ₹${formatCurrency(valuation)})")
                } else {
                    logSummary.add("❌ ${player.name} went UNSOLD (No AI budget).")
                }
            }
            
            auctionLog.clear()
            auctionLog.addAll(logSummary)
            auctionPlayer = null
            
            activeSlotId?.let { loadGameData(it) }
        }
    }

    fun selectAuctionPlayer(player: PlayerEntity) {
        auctionPlayer = player
        currentBidPrice = player.salary
        highestBidderId = ""
        auctionEnded = false
        userBiddingEnabled = true
        auctionLog.clear()
        val valuation = calculatePlayerPerformanceValuation(player)
        auctionLog.add("Starting bid for ${player.name} (${player.category}) at Base: ₹${formatCurrency(player.salary)}")
        if (player.isStatsKnown) {
            auctionLog.add("📊 Stats Valuation: ₹${formatCurrency(valuation)} (${player.matchesPlayed} M, ${player.runs} R, ${player.wickets} W)")
        } else {
            auctionLog.add("🕵️ Hidden Stats: Exact ratings & valuation unknown until scouted! (${player.matchesPlayed} M)")
        }
    }

    fun placeUserBid() {
        val player = auctionPlayer ?: return
        val userTeam = activeTeam ?: return
        if (auctionEnded) return

        val nextBid = getNextBidAmount(currentBidPrice)
        if (userTeam.balance < nextBid) {
            auctionLog.add("Insufficient funds to bid ₹${formatCurrency(nextBid)}")
            return
        }

        currentBidPrice = nextBid
        highestBidderId = userTeam.teamId
        auctionLog.add("You bid ₹${formatCurrency(currentBidPrice)}")

        // Trigger AI response bid
        triggerAiBid()
    }

    private fun triggerAiBid() {
        val player = auctionPlayer ?: return
        if (auctionEnded) return

        viewModelScope.launch {
            kotlinx.coroutines.delay(1000) // 1s delay for bidding speed
            val nextBid = getNextBidAmount(currentBidPrice)

            val eligibleAiTeams = allTeams.filter { it.isAi && it.balance >= nextBid }
            val valuation = calculatePlayerPerformanceValuation(player)
            val isUnderpriced = nextBid <= valuation * 1.15
            val bidProbability = if (nextBid < valuation * 0.8) 0.90 else if (isUnderpriced) 0.70 else 0.25

            if (eligibleAiTeams.isNotEmpty() && Random.nextDouble() < bidProbability && nextBid <= valuation * 1.45 && highestBidderId != "AI_SQUAD") {
                val bidder = eligibleAiTeams.random()
                currentBidPrice = nextBid
                highestBidderId = bidder.teamId
                auctionLog.add("${bidder.name} bid ₹${formatCurrency(currentBidPrice)} (Stats Val: ₹${formatCurrency(valuation)})")
            } else {
                // No higher bids -> sold!
                concludeAuction()
            }
        }
    }

    fun skipOrPassAuction() {
        auctionLog.add("You passed. Bidding continues among AI teams...")
        userBiddingEnabled = false
        triggerAiBidContinues()
    }

    private fun triggerAiBidContinues() {
        val player = auctionPlayer ?: return
        val nextBid = getNextBidAmount(currentBidPrice)
        val eligibleAiTeams = allTeams.filter { it.isAi && it.balance >= nextBid }
        val valuation = calculatePlayerPerformanceValuation(player)
        val isUnderpriced = nextBid <= valuation * 1.15
        val bidProbability = if (nextBid < valuation * 0.85) 0.85 else if (isUnderpriced) 0.60 else 0.20

        viewModelScope.launch {
            kotlinx.coroutines.delay(800)
            if (eligibleAiTeams.isNotEmpty() && Random.nextDouble() < bidProbability && nextBid <= valuation * 1.45) {
                val bidder = eligibleAiTeams.random()
                currentBidPrice = nextBid
                highestBidderId = bidder.teamId
                auctionLog.add("${bidder.name} bid ₹${formatCurrency(currentBidPrice)}")
                triggerAiBidContinues()
            } else {
                concludeAuction()
            }
        }
    }

    private fun concludeAuction() {
        val player = auctionPlayer ?: return
        auctionEnded = true
        if (highestBidderId.isBlank()) {
            auctionLog.add("${player.name} went UNSOLD.")
        } else {
            val winnerTeam = allTeams.find { it.teamId == highestBidderId }
            if (winnerTeam != null) {
                auctionLog.add("${player.name} SOLD to ${winnerTeam.name} for ₹${formatCurrency(currentBidPrice)}!")
                
                // Perform transaction in DB
                viewModelScope.launch {
                    val updatedTeam = winnerTeam.copy(balance = winnerTeam.balance - currentBidPrice)
                    repository.insertTeam(updatedTeam)

                    val updatedPlayer = player.copy(teamId = winnerTeam.teamId, salary = currentBidPrice, isScouted = true)
                    repository.insertPlayer(updatedPlayer)

                    // Write financial transactions
                    repository.insertFinance(
                        FinanceEntity(
                            slotId = player.slotId,
                            type = "EXPENSE",
                            category = "TRANSFERS",
                            amount = currentBidPrice,
                            season = 1,
                            matchDay = 1
                        )
                    )

                    // Achievement check
                    if (winnerTeam.teamId == activeTeamId) {
                        markObjectiveCompleted("OBJ_SIGN")
                        triggerMoraleEvent("New Star Auction Signing (${player.name})", +5)
                        val maxRating = maxOf(player.batting, player.bowling)
                        if (maxRating >= 90) {
                            unlockAchievement("auction_master")
                        }
                    }

                    // Reload
                    loadGameData(player.slotId)
                }
            }
        }
    }

    private fun getNextBidAmount(current: Long): Long {
        return when {
            current < 20000000L -> current + 2000000L  // +20 L
            current < 50000000L -> current + 5000000L  // +50 L
            current < 100000000L -> current + 10000000L // +1 Cr
            else -> current + 20000000L                 // +2 Cr
        }
    }

    // Facilities & Stadium Upgrades
    fun upgradeStadium(type: String, cost: Long) {
        val team = activeTeam ?: return
        if (team.balance < cost) return

        viewModelScope.launch {
            val updatedTeam = when (type) {
                "CAPACITY" -> {
                    val nextCap = when (team.stadiumCapacity) {
                        20000 -> 40000
                        40000 -> 60000
                        60000 -> 100000
                        else -> 100000
                    }
                    if (nextCap == 100000) {
                        unlockAchievement("stadium_mega")
                    }
                    team.copy(stadiumCapacity = nextCap)
                }
                "VIP" -> team.copy(stadiumVipStands = (team.stadiumVipStands + 1).coerceAtMost(5))
                "PARKING" -> team.copy(stadiumParking = (team.stadiumParking + 1).coerceAtMost(5))
                "FOOD" -> team.copy(stadiumFoodCourts = (team.stadiumFoodCourts + 1).coerceAtMost(5))
                "LIGHTING" -> team.copy(stadiumLighting = (team.stadiumLighting + 1).coerceAtMost(5))
                "TRAINING" -> team.copy(stadiumTrainingGrounds = (team.stadiumTrainingGrounds + 1).coerceAtMost(5))
                "MUSEUM" -> team.copy(stadiumMuseum = (team.stadiumMuseum + 1).coerceAtMost(5))
                "MERCH" -> team.copy(stadiumMerchShop = (team.stadiumMerchShop + 1).coerceAtMost(5))
                else -> team
            }

            // Deduct funds and boost followers on capacity upgrade
            val capDiff = if (type == "CAPACITY") (updatedTeam.stadiumCapacity - team.stadiumCapacity) else 0
            val followerSurge = (capDiff * 3L).coerceAtLeast(0L)
            val finalTeam = updatedTeam.copy(
                balance = team.balance - cost,
                followers = team.followers + followerSurge
            )
            repository.insertTeam(finalTeam)

            repository.insertFinance(
                FinanceEntity(
                    slotId = team.slotId,
                    type = "EXPENSE",
                    category = "MAINTENANCE",
                    amount = cost,
                    season = 1,
                    matchDay = 1
                )
            )

            repository.insertNews(
                NewsEntity(
                    slotId = team.slotId,
                    title = "Stadium Infrastructure Upgraded!",
                    content = "${team.stadiumName} gets an upgrade in $type category! Boosts spectator satisfaction and ticket sales.",
                    category = "GENERAL"
                )
            )

            markObjectiveCompleted("OBJ_FACILITY")
            triggerMoraleEvent("Facility Upgrade ($type)", +4)
            loadGameData(team.slotId)
            triggerSaveToast("🏟️ Facility upgraded! Fan growth rates boosted & game state saved to localStorage (Room SQLite)")
        }
    }

    // Sponsorship module: accept short-term contract based on fan count
    fun acceptShortTermSponsor(contractName: String, ratePerFan: Double) {
        val team = activeTeam ?: return
        val payout = (team.followers * ratePerFan).toLong().coerceAtLeast(5000000L)
        viewModelScope.launch {
            val updatedTeam = team.copy(balance = team.balance + payout)
            repository.insertTeam(updatedTeam)
            repository.insertFinance(
                FinanceEntity(
                    slotId = team.slotId,
                    type = "INCOME",
                    category = "SPONSORS",
                    amount = payout,
                    season = progressStore.currentSeason.value,
                    matchDay = progressStore.currentMatchDay.value
                )
            )
            repository.insertNews(
                NewsEntity(
                    slotId = team.slotId,
                    title = "🤝 Signed Short-Term Sponsorship with $contractName!",
                    content = "The franchise secured a short-term contract valued at ₹${formatCurrency(payout)}, scaling directly with our ${String.format("%.1f", team.followers / 1000000.0)}M fan base!",
                    category = "GENERAL"
                )
            )
            markObjectiveCompleted("OBJ_FACILITY")
            triggerMoraleEvent("Brand Partnership Signed", +2)
            loadGameData(team.slotId)
            triggerSaveToast("🤝 Sponsor Contract accepted! ₹${formatCurrency(payout)} added & saved to localStorage (Room SQLite)")
        }
    }

    // Long-Term Academy Training Mechanic
    fun assignToAcademy(player: PlayerEntity, focus: String) {
        if (academyAssignedPlayers.size >= 3 && !academyAssignedPlayers.containsKey(player.playerId)) {
            triggerSaveToast("⚠️ Academy full! Max 3 players can be assigned for steady automated training.")
            return
        }
        academyAssignedPlayers[player.playerId] = focus
        triggerSaveToast("🎓 ${player.name} assigned to $focus Training Track! Stats will improve steadily over time.")
    }

    fun removeFromAcademy(player: PlayerEntity) {
        if (academyAssignedPlayers.remove(player.playerId) != null) {
            triggerSaveToast("🎓 ${player.name} graduated/removed from Academy Training.")
        }
    }

    suspend fun processLongTermAcademyTraining() {
        if (academyAssignedPlayers.isEmpty()) return
        val updates = mutableListOf<String>()
        for ((playerId, focus) in academyAssignedPlayers) {
            val pl = repository.getPlayerById(playerId)
            if (pl != null) {
                val upPlayer = when (focus) {
                    "BATTING" -> pl.copy(batting = (pl.batting + Random.nextInt(1, 3)).coerceAtMost(100), fitness = (pl.fitness + 1).coerceAtMost(100))
                    "BOWLING" -> pl.copy(bowling = (pl.bowling + Random.nextInt(1, 3)).coerceAtMost(100), fitness = (pl.fitness + 1).coerceAtMost(100))
                    "ALL_ROUND" -> pl.copy(batting = (pl.batting + 1).coerceAtMost(100), bowling = (pl.bowling + 1).coerceAtMost(100), fielding = (pl.fielding + 1).coerceAtMost(100))
                    else -> pl
                }
                repository.insertPlayer(upPlayer)
                updates.add("${pl.name} (${focus})")
            }
        }
        if (updates.isNotEmpty()) {
            repository.insertNotification(
                NotificationEntity(
                    slotId = activeSlotId ?: "",
                    message = "📈 Long-Term Training Report: ${updates.joinToString(", ")} gained stat boosts after matchday!"
                )
            )
        }
    }

    // Player Training Academy
    fun trainPlayer(player: PlayerEntity, statType: String, cost: Long) {
        val team = activeTeam ?: return
        if (team.balance < cost) return

        viewModelScope.launch {
            val updatedPlayer = when (statType) {
                "BATTING" -> player.copy(batting = (player.batting + Random.nextInt(2, 5)).coerceAtMost(100), morale = (player.morale - Random.nextInt(2, 5)).coerceAtLeast(25))
                "BOWLING" -> player.copy(bowling = (player.bowling + Random.nextInt(2, 5)).coerceAtMost(100), morale = (player.morale - Random.nextInt(2, 5)).coerceAtLeast(25))
                "FIELDING" -> player.copy(fielding = (player.fielding + Random.nextInt(2, 5)).coerceAtMost(100), morale = (player.morale - Random.nextInt(2, 5)).coerceAtLeast(25))
                "FITNESS" -> player.copy(fitness = (player.fitness + Random.nextInt(3, 6)).coerceAtMost(100), morale = (player.morale - Random.nextInt(3, 6)).coerceAtLeast(25))
                "CAPTAINCY" -> player.copy(leadership = (player.leadership + Random.nextInt(2, 5)).coerceAtMost(100), morale = (player.morale + Random.nextInt(3, 6)).coerceAtMost(100))
                "MENTAL" -> player.copy(morale = (player.morale + Random.nextInt(8, 15)).coerceAtMost(100))
                else -> player
            }

            repository.insertPlayer(updatedPlayer)

            // Deduct cash
            repository.insertTeam(team.copy(balance = team.balance - cost))

            repository.insertFinance(
                FinanceEntity(
                    slotId = team.slotId,
                    type = "EXPENSE",
                    category = "TRAINING",
                    amount = cost,
                    season = 1,
                    matchDay = 1
                )
            )

            markObjectiveCompleted("OBJ_SIGN")
            calculateAndSyncTeamMorale()
            loadGameData(team.slotId)
            if (statType == "MENTAL" || statType == "CAPTAINCY") {
                triggerSaveToast("🏋️ Mental/Leadership Drill completed! Morale boosted & saved to localStorage")
            } else {
                triggerSaveToast("🏋️ Physical drill completed! (+Stats, -Morale from intensity) & saved to localStorage")
            }
        }
    }

    // Staff Management (Hire Coaches)
    fun hireCoach(coach: CoachEntity) {
        val team = activeTeam ?: return
        if (team.balance < coach.salary) return

        viewModelScope.launch {
            // Dismiss old coach of same role if hired
            coaches.filter { it.isHired && it.role == coach.role }.forEach { old ->
                repository.insertCoach(old.copy(isHired = false, teamId = null))
            }

            // Hire new coach
            repository.insertCoach(coach.copy(isHired = true, teamId = team.teamId))

            // Deduct salary (representing signing fee + season cost)
            repository.insertTeam(team.copy(balance = team.balance - coach.salary))

            repository.insertFinance(
                FinanceEntity(
                    slotId = team.slotId,
                    type = "EXPENSE",
                    category = "SALARIES",
                    amount = coach.salary,
                    season = 1,
                    matchDay = 1
                )
            )

            repository.insertNews(
                NewsEntity(
                    slotId = team.slotId,
                    title = "New Coach Hired!",
                    content = "${team.name} signed premium ${coach.role} Coach ${coach.name} with rating ${coach.rating}!",
                    category = "COACH"
                )
            )

            loadGameData(team.slotId)
        }
    }

    // Scouting System
    fun scoutPlayers(cost: Long) {
        val team = activeTeam ?: return
        if (team.balance < cost) return

        viewModelScope.launch {
            // Deduct cash
            repository.insertTeam(team.copy(balance = team.balance - cost))

            // Create a custom scouted player to inject into the market!
            val suffix = Random.nextInt(100, 999)
            val youngStar = PlayerEntity(
                playerId = "${team.slotId}_SCOUTED_$suffix",
                slotId = team.slotId,
                teamId = null, // free agent
                name = listOf("A. Sharma", "R. Jadeja", "P. Patel", "M. Yadav", "K. Singh", "S. Iyer").random() + " (Scouted)",
                category = listOf("BATSMAN", "BOWLER", "ALL_ROUNDER", "WICKET_KEEPER").random(),
                batting = Random.nextInt(75, 92),
                bowling = Random.nextInt(75, 92),
                fielding = Random.nextInt(80, 95),
                fitness = Random.nextInt(85, 98),
                experience = Random.nextInt(20, 50),
                form = 80,
                leadership = Random.nextInt(30, 70),
                morale = 90,
                salary = Random.nextLong(20000000L, 60000000L),
                contractDuration = 3,
                age = Random.nextInt(18, 23),
                nationality = "India",
                isScouted = true
            )

            repository.insertPlayer(youngStar)

            repository.insertFinance(
                FinanceEntity(
                    slotId = team.slotId,
                    type = "EXPENSE",
                    category = "TRAINING",
                    amount = cost,
                    season = 1,
                    matchDay = 1
                )
            )

            repository.insertNotification(
                NotificationEntity(
                    slotId = team.slotId,
                    message = "Scouts discovered a Hidden Gem! ${youngStar.name} (${youngStar.category}) is now available in Free Agent market."
                )
            )

            loadGameData(team.slotId)
        }
    }

    /**
     * Scouts a specific player before the auction, spending part of the budget to reveal their hidden stats and valuation.
     */
    fun scoutAuctionPlayer(player: PlayerEntity, cost: Long = 2500000L) {
        val team = activeTeam ?: return
        if (team.balance < cost) {
            viewModelScope.launch {
                notifySimResult("⚠️ Insufficient budget to scout ${player.name}! Requires ₹${formatCurrency(cost)}.", team.slotId)
            }
            return
        }
        viewModelScope.launch {
            val updatedTeam = team.copy(balance = team.balance - cost)
            repository.insertTeam(updatedTeam)

            val scoutedPlayer = player.copy(isScouted = true)
            repository.insertPlayer(scoutedPlayer)

            repository.insertFinance(
                FinanceEntity(
                    slotId = team.slotId,
                    type = "EXPENSE",
                    category = "TRAINING",
                    amount = cost,
                    season = progressStore.currentSeason.value,
                    matchDay = 1
                )
            )

            val valPrice = calculatePlayerPerformanceValuation(scoutedPlayer)
            notifySimResult("🔍 Scout Report Ready! ${scoutedPlayer.name}: OVR ${(scoutedPlayer.batting + scoutedPlayer.bowling)/2}, Est. Valuation: ₹${formatCurrency(valPrice)}.", team.slotId)

            if (auctionPlayer?.playerId == player.playerId) {
                auctionPlayer = scoutedPlayer
            }

            loadGameData(team.slotId)
        }
    }

    /**
     * Spends budget to batch scout all unscouted players in the provided list before auction.
     */
    fun scoutBatchPlayers(players: List<PlayerEntity>, cost: Long = 10000000L) {
        val team = activeTeam ?: return
        val unscouted = players.filter { !it.isStatsKnown }
        if (unscouted.isEmpty()) {
            viewModelScope.launch {
                notifySimResult("✅ All players in this filter are already scouted!", team.slotId)
            }
            return
        }
        if (team.balance < cost) {
            viewModelScope.launch {
                notifySimResult("⚠️ Insufficient budget to batch scout! Requires ₹${formatCurrency(cost)}.", team.slotId)
            }
            return
        }
        viewModelScope.launch {
            val updatedTeam = team.copy(balance = team.balance - cost)
            repository.insertTeam(updatedTeam)

            for (pl in unscouted) {
                repository.insertPlayer(pl.copy(isScouted = true))
            }

            repository.insertFinance(
                FinanceEntity(
                    slotId = team.slotId,
                    type = "EXPENSE",
                    category = "TRAINING",
                    amount = cost,
                    season = progressStore.currentSeason.value,
                    matchDay = 1
                )
            )

            notifySimResult("⚡ Batch Scout Report! Revealed exact ratings and valuations for ${unscouted.size} draft prospects.", team.slotId)

            if (auctionPlayer != null && unscouted.any { it.playerId == auctionPlayer?.playerId }) {
                auctionPlayer = auctionPlayer?.copy(isScouted = true)
            }

            loadGameData(team.slotId)
        }
    }

    // Match Simulation flow
    fun prepareMatchDay(match: MatchEntity) {
        currentMatchPlaying = match
        matchInningsProgress = 0
        matchCommentary.clear()
        matchResultSummary = null

        // Auto populate playing XI (all user's roster players)
        matchXI.clear()
        matchXI.addAll(activePlayers.sortedByDescending { it.batting + it.bowling }.take(11))

        currentScreen = Screen.MatchDay
    }

    fun playSelectedMatch() {
        val match = currentMatchPlaying ?: return
        val opponentId = if (match.teamAId == activeTeamId) match.teamBId else match.teamAId
        val userPlayers = matchXI.toList()

        viewModelScope.launch {
            val opponentTeam = repository.getTeamById(opponentId) ?: return@launch
            val oppPlayers = repository.getPlayersForTeamSync(opponentId)

            val uTeam = activeTeam ?: return@launch
            
            val simRes = MatchEngine.simulateMatch(
                teamA = uTeam,
                teamAPlayers = userPlayers,
                teamB = opponentTeam,
                teamBPlayers = oppPlayers,
                weather = match.weather,
                userTeamId = uTeam.teamId,
                userBattingTactic = userBattingTactic,
                userBowlingTactic = userBowlingTactic
            )

            // Save match score details
            val updatedMatch = match.copy(
                teamAScore = simRes.teamAScoreText,
                teamBScore = simRes.teamBScoreText,
                isPlayed = true,
                winnerId = simRes.winnerId,
                commentary = simRes.commentary,
                tossWinnerId = simRes.tossWinnerId,
                tossDecision = simRes.tossDecision
            )

            repository.insertMatch(updatedMatch)

            // Perform earnings calculations from Ticket Sales & Stadium Capacity Boosted Fan Growth!
            val ticketIncome = (uTeam.stadiumCapacity * 800L).coerceIn(10000000L, 80000000L) // up to 8 Crores per home match
            val isWin = updatedMatch.winnerId == uTeam.teamId
            val baseFans = if (isWin) 45000L else 12000L
            val capacityBoost = (uTeam.stadiumCapacity / 1000L) * (if (isWin) 1500L else 500L)
            val newFollowers = uTeam.followers + baseFans + capacityBoost
            val finalBal = if (isWin) uTeam.balance + ticketIncome else uTeam.balance + (ticketIncome / 2)
            
            repository.insertTeam(uTeam.copy(balance = finalBal, followers = newFollowers))

            repository.insertFinance(
                FinanceEntity(
                    slotId = uTeam.slotId,
                    type = "INCOME",
                    category = "TICKETS",
                    amount = ticketIncome,
                    season = 1,
                    matchDay = match.matchDay
                )
            )

            // Dynamic news updates
            val winnerName = if (simRes.winnerId == uTeam.teamId) uTeam.name else opponentTeam.name
            val newsTitle = if (simRes.winnerId == uTeam.teamId) "Thrilling Win for ${uTeam.name}!" else "${opponentTeam.name} Clinches Victory!"
            repository.insertNews(
                NewsEntity(
                    slotId = uTeam.slotId,
                    title = newsTitle,
                    content = "The battle at stadium concluded with $winnerName lifting the spirits of fans. Final stats: ${simRes.teamAScoreText} vs ${simRes.teamBScoreText}.",
                    category = "PERFORMANCE"
                )
            )

            // Check dynamic achievements
            if (simRes.winnerId == uTeam.teamId) {
                unlockAchievement("first_win")
                markObjectiveCompleted("OBJ_WIN")
                triggerMoraleEvent("Matchday Victory vs ${opponentTeam.name}", +6)
            } else {
                triggerMoraleEvent("Matchday Loss vs ${opponentTeam.name}", -8)
            }

            // Save individual stats
            applyPlayerStatsUpdates(simRes.playerStatsUpdates)

            // Check user balance achievement
            if (finalBal >= 1200000000L) {
                unlockAchievement("richest_franchise")
            }

            // State updates
            matchCommentary.addAll(simRes.commentary.split(";"))
            matchResultSummary = simRes
            matchInningsProgress = 1

            // Increment currentMatchDay and handle playoffs via Central Progress Store
            progressStore.advanceMatchDay()
            processLongTermAcademyTraining()
            triggerSaveToast("💾 Match results, follower growth & academy training saved to localStorage (Room SQLite)")
            loadGameData(uTeam.slotId)
        }
    }

    private suspend fun applyPlayerStatsUpdates(updates: List<MatchEngine.PlayerStatsUpdate>) {
        updates.forEach { up ->
            val pl = repository.getPlayerById(up.playerId)
            if (pl != null) {
                val newRuns = pl.runs + up.runsAdded
                val newWickets = pl.wickets + up.wicketsAdded
                val newMatches = pl.matchesPlayed + 1
                val newCenturies = pl.centuries + up.centuriesAdded
                val newFiveWickets = pl.fiveWickets + up.fiveWicketsAdded
                
                val prevBalls = if (pl.strikeRate > 0) (pl.runs.toDouble() / pl.strikeRate) * 100.0 else 0.0
                val totBalls = prevBalls + up.ballsFaced
                val newSR = if (totBalls > 0) ((newRuns.toDouble() / totBalls) * 100.0) else pl.strikeRate

                val matchEcon = if (up.ballsBowled > 0) (up.runsConceded.toDouble() / (up.ballsBowled / 6.0)) else 0.0
                val newEcon = if (pl.economy == 0.0 && matchEcon > 0) matchEcon else if (matchEcon > 0) ((pl.economy * pl.matchesPlayed) + matchEcon) / newMatches else pl.economy

                repository.insertPlayer(
                    pl.copy(
                        matchesPlayed = newMatches,
                        runs = newRuns,
                        wickets = newWickets,
                        strikeRate = Math.round(newSR * 100.0) / 100.0,
                        economy = Math.round(newEcon * 100.0) / 100.0,
                        centuries = newCenturies,
                        fiveWickets = newFiveWickets
                    )
                )
            }
        }
    }

    // Auto-simulate other matches of MatchDay using scheduled simulation engine
    fun simulateRemainingMatchesForDay(day: Int, onComplete: () -> Unit = {}) {
        val slotId = activeSlotId ?: return
        viewModelScope.launch {
            ScheduledMatchSimulationEngine.simulateScheduledRound(day, repository, progressStore)
            loadGameData(slotId)
            onComplete()
        }
    }

    fun simulateScheduledGame(match: MatchEntity, onComplete: () -> Unit = {}) {
        val slotId = activeSlotId ?: return
        viewModelScope.launch {
            ScheduledMatchSimulationEngine.simulateScheduledGame(match, repository, progressStore)
            loadGameData(slotId)
            onComplete()
        }
    }

    private suspend fun notifySimResult(msg: String, slotId: String) {
        repository.insertNotification(
            NotificationEntity(
                slotId = slotId,
                message = msg
            )
        )
    }

    fun simulateScheduledRound(round: Int, onComplete: () -> Unit = {}) {
        val slotId = activeSlotId ?: return
        viewModelScope.launch {
            val summary = ScheduledMatchSimulationEngine.simulateScheduledRound(round, repository, progressStore)
            notifySimResult(summary.message, slotId)
            processLongTermAcademyTraining()
            triggerSaveToast("💾 Round simulated & game state saved to localStorage (Room SQLite)")
            loadGameData(slotId)
            onComplete()
        }
    }

    fun simulateUpToNextUserMatch(onComplete: () -> Unit = {}) {
        val slotId = activeSlotId ?: return
        viewModelScope.launch {
            val summary = ScheduledMatchSimulationEngine.simulateUpToNextUserMatch(repository, progressStore)
            notifySimResult(summary.message, slotId)
            processLongTermAcademyTraining()
            triggerSaveToast("💾 Matches simulated & game state saved to localStorage (Room SQLite)")
            loadGameData(slotId)
            onComplete()
        }
    }

    fun simulateRemainingSeason(onComplete: () -> Unit = {}) {
        val slotId = activeSlotId ?: return
        viewModelScope.launch {
            val summary = ScheduledMatchSimulationEngine.simulateRemainingSeason(repository, progressStore)
            notifySimResult(summary.message, slotId)
            processLongTermAcademyTraining()
            triggerSaveToast("💾 Season simulated & game state saved to localStorage (Room SQLite)")
            loadGameData(slotId)
            onComplete()
        }
    }

    // Sponsors & Merch Payouts
    fun signSponsor(sponsor: SponsorEntity) {
        val team = activeTeam ?: return
        viewModelScope.launch {
            repository.insertSponsor(sponsor.copy(isSigned = true, teamId = team.teamId))
            
            // Initial signing bonus
            repository.insertTeam(team.copy(balance = team.balance + sponsor.payoutPerSeason))

            repository.insertFinance(
                FinanceEntity(
                    slotId = team.slotId,
                    type = "INCOME",
                    category = "SPONSORS",
                    amount = sponsor.payoutPerSeason,
                    season = 1,
                    matchDay = 1
                )
            )

            loadGameData(team.slotId)
            triggerSaveToast("🤝 Sponsor signed! Bonus added & game state saved to localStorage (Room SQLite)")
        }
    }

    // Merchandise stores
    fun sellMerchandise(type: String, price: Long, count: Int) {
        val team = activeTeam ?: return
        val earnings = price * count
        viewModelScope.launch {
            repository.insertTeam(team.copy(balance = team.balance + earnings))

            repository.insertFinance(
                FinanceEntity(
                    slotId = team.slotId,
                    type = "INCOME",
                    category = "MERCHANDISE",
                    amount = earnings,
                    season = 1,
                    matchDay = 1
                )
            )

            repository.insertNotification(
                NotificationEntity(
                    slotId = team.slotId,
                    message = "Successfully sold $count $type! Gained ₹${formatCurrency(earnings)}."
                )
            )

            loadGameData(team.slotId)
            triggerSaveToast("🛍️ Merch sold! Revenue saved to localStorage (Room SQLite)")
        }
    }

    // Transfer Market (Free Agent Signing)
    fun buyFreeAgent(player: PlayerEntity) {
        val team = activeTeam ?: return
        if (team.balance < player.salary) return

        viewModelScope.launch {
            // Deduct salary from balance
            repository.insertTeam(team.copy(balance = team.balance - player.salary))

            // Update player team
            repository.insertPlayer(player.copy(teamId = team.teamId, isScouted = true))

            repository.insertFinance(
                FinanceEntity(
                    slotId = team.slotId,
                    type = "EXPENSE",
                    category = "TRANSFERS",
                    amount = player.salary,
                    season = 1,
                    matchDay = 1
                )
            )

            repository.insertTransfer(
                TransferEntity(
                    slotId = team.slotId,
                    playerName = player.name,
                    fromTeamName = "Free Agent Market",
                    toTeamName = team.name,
                    transferFee = player.salary,
                    season = 1
                )
            )

            markObjectiveCompleted("OBJ_SIGN")
            triggerMoraleEvent("New Free Agent Signing (${player.name})", +3)
            loadGameData(team.slotId)
            triggerSaveToast("✍️ Player signed! Roster & balance saved to localStorage (Room SQLite)")
        }
    }

    fun releasePlayer(player: PlayerEntity) {
        val team = activeTeam ?: return
        viewModelScope.launch {
            repository.insertPlayer(player.copy(teamId = null))

            repository.insertTransfer(
                TransferEntity(
                    slotId = team.slotId,
                    playerName = player.name,
                    fromTeamName = team.name,
                    toTeamName = "Released",
                    transferFee = 0,
                    season = 1
                )
            )

            loadGameData(team.slotId)
            triggerSaveToast("📤 Player released! Roster saved to localStorage (Room SQLite)")
        }
    }

    private fun unlockAchievement(id: String) {
        val user = currentUser ?: return
        viewModelScope.launch {
            val ach = achievements.find { it.id == id }
            if (ach != null && ach.unlockedAt == null) {
                repository.insertAchievement(ach.copy(unlockedAt = System.currentTimeMillis()))
                
                repository.insertNotification(
                    NotificationEntity(
                        slotId = activeSlotId ?: "",
                        message = "🏆 Achievement Unlocked: ${ach.title}!"
                    )
                )
            }
        }
    }

    // --- Admin panel functions ---
    fun adminModifyPlayer(player: PlayerEntity, batting: Int, bowling: Int, fitness: Int) {
        viewModelScope.launch {
            repository.insertPlayer(
                player.copy(
                    batting = batting,
                    bowling = bowling,
                    fitness = fitness
                )
            )
            loadGameData(player.slotId)
        }
    }

    fun formatCurrency(amount: Long): String {
        return when {
            amount >= 10000000L -> "${String.format("%.2f", amount / 10000000.0)} Cr"
            amount >= 100000L -> "${String.format("%.2f", amount / 100000.0)} Lakh"
            else -> amount.toString()
        }
    }

    fun advanceToNextSeason() {
        viewModelScope.launch {
            val champId = pointsTable.firstOrNull()?.teamId ?: ""
            val champName = pointsTable.firstOrNull()?.teamName ?: "League Champions"
            progressStore.advanceToNextSeason(champId, champName)
            activeSlotId?.let { loadGameData(it) }
            triggerSaveToast("🏆 Season advanced & game state saved to localStorage (Room SQLite)")
        }
    }

    fun saveFranchiseProgress() {
        viewModelScope.launch {
            progressStore.saveProgressSnapshot()
            triggerSaveToast("💾 Game state successfully saved to localStorage (Room SQLite)")
        }
    }

    fun renewPlayerContract(player: PlayerEntity, extensionSeasons: Int, newSalary: Long) {
        viewModelScope.launch {
            val updated = player.copy(
                contractDuration = player.contractDuration + extensionSeasons,
                salary = newSalary,
                morale = (player.morale + 15).coerceAtMost(100)
            )
            repository.insertPlayer(updated)
            
            // Update in-memory lists immediately
            val idx1 = activePlayers.indexOfFirst { it.playerId == player.playerId }
            if (idx1 != -1) activePlayers[idx1] = updated
            val idx2 = allPlayers.indexOfFirst { it.playerId == player.playerId }
            if (idx2 != -1) allPlayers[idx2] = updated
            
            triggerSaveToast("✍️ Contract Extended (+${extensionSeasons} Yrs) for ${player.name}! New Salary: ₹${formatCurrency(newSalary)}/season & saved to localStorage")
        }
    }

    fun randomizeCurrentMatchWeather() {
        val match = currentMatchPlaying ?: return
        val options = listOf("SUNNY", "OVERCAST", "RAIN", "DEW", "WINDY").filter { it != match.weather }
        val newWeather = if (options.isNotEmpty()) options.random() else "SUNNY"
        val updated = match.copy(weather = newWeather)
        currentMatchPlaying = updated
        viewModelScope.launch {
            repository.insertMatch(updated)
            triggerSaveToast("🌤️ Weather Forecast Randomized: $newWeather! Adjust your Playing XI & Tactics strategically!")
        }
    }
}
