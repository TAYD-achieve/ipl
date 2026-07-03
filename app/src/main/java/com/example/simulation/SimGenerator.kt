package com.example.simulation

import com.example.data.*
import java.util.UUID

object SimGenerator {

    // Preconfigured AI Franchises
    val AI_TEMPLATES = listOf(
        Triple("Mumbai Shield", "Mumbai", "#004B87"), // MI
        Triple("Chennai Kings", "Chennai", "#FFD200"), // CSK
        Triple("Bangalore Royals", "Bengaluru", "#D22630"), // RCB
        Triple("Kolkata Knights", "Kolkata", "#3A225D"), // KKR
        Triple("Delhi Capitalists", "Delhi", "#005CA9"), // DC
        Triple("Rajasthan Royals", "Jaipur", "#EA1A85"), // RR
        Triple("Gujarat Titans", "Ahmedabad", "#0B2240") // GT
    )

    fun generateAiTeams(slotId: String): List<TeamEntity> {
        return AI_TEMPLATES.mapIndexed { index, (name, city, color) ->
            TeamEntity(
                teamId = "${slotId}_AI_${index + 1}",
                slotId = slotId,
                name = name,
                city = city,
                logoText = name.split(" ").map { it.take(1) }.joinToString(""),
                logoBgColor = color,
                logoTextColor = "#FFFFFF",
                logoShape = "SHIELD",
                jerseyColorPrimary = color,
                jerseyColorSecondary = "#FFFFFF",
                jerseyNumber = "10",
                jerseyPlayerName = "AI",
                jerseyTemplate = (index % 3) + 1,
                slogan = "Together we conquer!",
                stadiumName = "$city Stadium",
                stadiumCapacity = 40000,
                stadiumVipStands = 1,
                stadiumParking = 1,
                stadiumFoodCourts = 1,
                stadiumLighting = 1,
                stadiumTrainingGrounds = 1,
                stadiumMuseum = 0,
                stadiumMerchShop = 0,
                balance = 1200000000L, // 120 Crores
                reputation = 60 + (index * 5),
                followers = 1000000L * (index + 2),
                fanHappiness = 80,
                isAi = true,
                aiShortName = name.split(" ").map { it.take(1) }.joinToString("")
            )
        }
    }

    val PLAYER_POOL = listOf(
        // RETAINED / ADDITIONAL PLAYERS (Base Price 2 Cr)
        PlayerTemplate("Ruturaj Gaikwad", "BATSMAN", 88, 10, 85, 92, 85, 27, "India", 20000000L),
        PlayerTemplate("Matheesha Pathirana", "BOWLER", 10, 89, 82, 85, 75, 21, "Sri Lanka", 20000000L),
        PlayerTemplate("Shivam Dube", "ALL_ROUNDER", 86, 75, 80, 82, 80, 31, "India", 20000000L),
        PlayerTemplate("Ravindra Jadeja", "ALL_ROUNDER", 82, 89, 95, 90, 92, 35, "India", 20000000L),
        PlayerTemplate("MS Dhoni", "WICKET_KEEPER", 85, 10, 92, 98, 99, 42, "India", 20000000L),
        PlayerTemplate("Axar Patel", "ALL_ROUNDER", 80, 85, 86, 85, 82, 30, "India", 20000000L),
        PlayerTemplate("Kuldeep Yadav", "BOWLER", 14, 89, 82, 85, 80, 29, "India", 20000000L),
        PlayerTemplate("Tristan Stubbs", "BATSMAN", 87, 10, 84, 88, 75, 23, "South Africa", 20000000L),
        PlayerTemplate("Abhishek Porel", "WICKET_KEEPER", 78, 10, 80, 85, 60, 21, "India", 20000000L),
        PlayerTemplate("Rashid Khan", "ALL_ROUNDER", 78, 92, 88, 88, 85, 25, "Afghanistan", 20000000L),
        PlayerTemplate("Shubman Gill", "BATSMAN", 89, 10, 84, 88, 80, 24, "India", 20000000L),
        PlayerTemplate("Sai Sudharsan", "BATSMAN", 83, 10, 82, 85, 70, 22, "India", 20000000L),
        PlayerTemplate("Rahul Tewatia", "ALL_ROUNDER", 82, 78, 80, 82, 78, 31, "India", 20000000L),
        PlayerTemplate("Shahrukh Khan", "ALL_ROUNDER", 80, 75, 82, 84, 75, 29, "India", 20000000L),
        PlayerTemplate("Rinku Singh", "BATSMAN", 85, 10, 88, 86, 78, 26, "India", 20000000L),
        PlayerTemplate("Varun Chakaravarthy", "BOWLER", 10, 88, 80, 82, 78, 32, "India", 20000000L),
        PlayerTemplate("Sunil Narine", "ALL_ROUNDER", 82, 89, 85, 84, 94, 36, "West Indies", 20000000L),
        PlayerTemplate("Andre Russell", "ALL_ROUNDER", 90, 85, 82, 80, 92, 36, "West Indies", 20000000L),
        PlayerTemplate("Harshit Rana", "BOWLER", 15, 84, 80, 85, 65, 22, "India", 20000000L),
        PlayerTemplate("Ramandeep Singh", "ALL_ROUNDER", 80, 78, 85, 86, 68, 27, "India", 20000000L),
        PlayerTemplate("Nicholas Pooran", "WICKET_KEEPER", 89, 10, 82, 86, 85, 28, "West Indies", 20000000L),
        PlayerTemplate("Ravi Bishnoi", "BOWLER", 12, 84, 85, 88, 75, 23, "India", 20000000L),
        PlayerTemplate("Mayank Yadav", "BOWLER", 10, 86, 78, 80, 55, 22, "India", 20000000L),
        PlayerTemplate("Mohsin Khan", "BOWLER", 10, 82, 80, 80, 68, 25, "India", 20000000L),
        PlayerTemplate("Ayush Badoni", "ALL_ROUNDER", 78, 70, 82, 84, 70, 24, "India", 20000000L),
        PlayerTemplate("Jasprit Bumrah", "BOWLER", 20, 97, 88, 90, 92, 30, "India", 20000000L),
        PlayerTemplate("Suryakumar Yadav", "BATSMAN", 94, 10, 86, 86, 88, 33, "India", 20000000L),
        PlayerTemplate("Hardik Pandya", "ALL_ROUNDER", 88, 84, 85, 82, 88, 30, "India", 20000000L),
        PlayerTemplate("Rohit Sharma", "BATSMAN", 94, 15, 82, 80, 96, 37, "India", 20000000L),
        PlayerTemplate("Tilak Varma", "BATSMAN", 84, 65, 84, 88, 72, 21, "India", 20000000L),
        PlayerTemplate("Shashank Singh", "BATSMAN", 82, 10, 80, 85, 75, 32, "India", 20000000L),
        PlayerTemplate("Prabhsimran Singh", "WICKET_KEEPER", 80, 10, 78, 84, 70, 23, "India", 20000000L),
        PlayerTemplate("Sanju Samson", "WICKET_KEEPER", 88, 10, 84, 88, 85, 29, "India", 20000000L),
        PlayerTemplate("Yashasvi Jaiswal", "BATSMAN", 89, 10, 85, 88, 78, 22, "India", 20000000L),
        PlayerTemplate("Riyan Parag", "BATSMAN", 82, 70, 82, 85, 72, 22, "India", 20000000L),
        PlayerTemplate("Dhruv Jurel", "WICKET_KEEPER", 80, 10, 84, 86, 70, 23, "India", 20000000L),
        PlayerTemplate("Shimron Hetmyer", "BATSMAN", 83, 10, 82, 85, 80, 27, "West Indies", 20000000L),
        PlayerTemplate("Sandeep Sharma", "BOWLER", 10, 84, 82, 82, 85, 31, "India", 20000000L),
        PlayerTemplate("Virat Kohli", "BATSMAN", 96, 10, 88, 95, 96, 35, "India", 20000000L),
        PlayerTemplate("Rajat Patidar", "BATSMAN", 83, 10, 80, 84, 75, 30, "India", 20000000L),
        PlayerTemplate("Yash Dayal", "BOWLER", 10, 83, 80, 82, 68, 26, "India", 20000000L),
        PlayerTemplate("Pat Cummins", "BOWLER", 35, 90, 84, 92, 92, 31, "Australia", 20000000L),
        PlayerTemplate("Abhishek Sharma", "ALL_ROUNDER", 86, 74, 82, 86, 74, 23, "India", 20000000L),
        PlayerTemplate("Nitish Kumar Reddy", "ALL_ROUNDER", 82, 78, 84, 88, 65, 21, "India", 20000000L),
        PlayerTemplate("Heinrich Klaasen", "WICKET_KEEPER", 92, 10, 86, 88, 85, 32, "South Africa", 20000000L),
        PlayerTemplate("Travis Head", "BATSMAN", 91, 70, 80, 88, 88, 30, "Australia", 20000000L),

        // CAPPED PLAYERS FROM PDF PAGE 1
        PlayerTemplate("Jos Buttler", "WICKET_KEEPER", 93, 10, 85, 88, 88, 34, "England", 20000000L),
        PlayerTemplate("Shreyas Iyer", "BATSMAN", 84, 10, 82, 85, 82, 30, "India", 20000000L),
        PlayerTemplate("Rishabh Pant", "WICKET_KEEPER", 89, 10, 82, 86, 84, 27, "India", 20000000L),
        PlayerTemplate("Kagiso Rabada", "BOWLER", 15, 89, 83, 85, 82, 29, "South Africa", 20000000L),
        PlayerTemplate("Arshdeep Singh", "BOWLER", 10, 86, 82, 85, 78, 26, "India", 20000000L),
        PlayerTemplate("Mitchell Starc", "BOWLER", 15, 93, 82, 84, 88, 35, "Australia", 20000000L),
        PlayerTemplate("Yuzvendra Chahal", "BOWLER", 10, 90, 80, 82, 85, 34, "India", 20000000L),
        PlayerTemplate("Liam Livingstone", "ALL_ROUNDER", 85, 76, 80, 82, 80, 31, "England", 20000000L),
        PlayerTemplate("David Miller", "BATSMAN", 86, 10, 84, 85, 86, 35, "South Africa", 15000000L),
        PlayerTemplate("KL Rahul", "WICKET_KEEPER", 88, 10, 84, 86, 86, 32, "India", 20000000L),
        PlayerTemplate("Mohammad Shami", "BOWLER", 15, 90, 83, 84, 85, 34, "India", 20000000L),
        PlayerTemplate("Mohammad Siraj", "BOWLER", 12, 87, 83, 85, 80, 31, "India", 20000000L),
        PlayerTemplate("Harry Brook", "BATSMAN", 84, 10, 82, 85, 70, 26, "England", 20000000L),
        PlayerTemplate("Devon Conway", "BATSMAN", 85, 10, 83, 84, 82, 33, "New Zealand", 20000000L),
        PlayerTemplate("Jake Fraser-Mcgurk", "BATSMAN", 83, 10, 78, 84, 60, 22, "Australia", 20000000L),
        PlayerTemplate("Aiden Markram", "BATSMAN", 84, 72, 82, 85, 80, 30, "South Africa", 20000000L),
        PlayerTemplate("Devdutt Padikkal", "BATSMAN", 78, 10, 80, 82, 68, 24, "India", 20000000L),
        PlayerTemplate("Rahul Tripathi", "BATSMAN", 80, 10, 82, 84, 75, 34, "India", 7500000L),
        PlayerTemplate("David Warner", "BATSMAN", 88, 10, 85, 80, 94, 38, "Australia", 20000000L),
        PlayerTemplate("Ravichandran Ashwin", "ALL_ROUNDER", 74, 86, 85, 80, 95, 38, "India", 20000000L),
        PlayerTemplate("Venkatesh Iyer", "ALL_ROUNDER", 82, 74, 80, 82, 78, 30, "India", 20000000L),
        PlayerTemplate("Mitchell Marsh", "ALL_ROUNDER", 84, 80, 82, 80, 82, 33, "Australia", 20000000L),
        PlayerTemplate("Glenn Maxwell", "ALL_ROUNDER", 88, 78, 85, 82, 88, 36, "Australia", 20000000L),
        PlayerTemplate("Harshal Patel", "ALL_ROUNDER", 76, 83, 80, 82, 80, 34, "India", 20000000L),
        PlayerTemplate("Rachin Ravindra", "ALL_ROUNDER", 84, 76, 82, 85, 72, 25, "New Zealand", 15000000L),
        PlayerTemplate("Marcus Stoinis", "ALL_ROUNDER", 84, 80, 82, 82, 82, 35, "Australia", 20000000L),
        PlayerTemplate("Jonny Bairstow", "WICKET_KEEPER", 86, 10, 82, 84, 84, 35, "England", 20000000L),
        PlayerTemplate("Quinton De Kock", "WICKET_KEEPER", 88, 10, 84, 85, 86, 32, "South Africa", 20000000L),
        PlayerTemplate("Rahmanullah Gurbaz", "WICKET_KEEPER", 82, 10, 80, 84, 70, 23, "Afghanistan", 20000000L),
        PlayerTemplate("Ishan Kishan", "WICKET_KEEPER", 84, 10, 80, 85, 78, 26, "India", 20000000L),
        PlayerTemplate("Phil Salt", "WICKET_KEEPER", 85, 10, 82, 86, 75, 28, "England", 20000000L),
        PlayerTemplate("Jitesh Sharma", "WICKET_KEEPER", 78, 10, 82, 84, 70, 31, "India", 10000000L),
        PlayerTemplate("Khaleel Ahmed", "BOWLER", 10, 83, 80, 82, 75, 27, "India", 20000000L),
        PlayerTemplate("Trent Boult", "BOWLER", 15, 91, 85, 82, 88, 35, "New Zealand", 20000000L),
        PlayerTemplate("Josh Hazlewood", "BOWLER", 12, 88, 82, 84, 86, 34, "Australia", 20000000L),
        PlayerTemplate("Avesh Khan", "BOWLER", 10, 83, 80, 82, 75, 28, "India", 20000000L),
        PlayerTemplate("Prasidh Krishna", "BOWLER", 10, 81, 78, 80, 70, 29, "India", 20000000L),
        PlayerTemplate("T. Natarajan", "BOWLER", 10, 84, 80, 82, 76, 33, "India", 20000000L),
        PlayerTemplate("Anrich Nortje", "BOWLER", 10, 86, 80, 82, 78, 31, "South Africa", 20000000L),
        PlayerTemplate("Noor Ahmad", "BOWLER", 10, 85, 80, 84, 68, 20, "Afghanistan", 20000000L),
        PlayerTemplate("Rahul Chahar", "BOWLER", 10, 80, 82, 82, 75, 25, "India", 10000000L),
        PlayerTemplate("Wanindu Hasaranga", "BOWLER", 20, 88, 85, 84, 80, 27, "Sri Lanka", 20000000L),
        PlayerTemplate("Waqar Salamkheil", "BOWLER", 10, 81, 78, 80, 60, 23, "Afghanistan", 7500000L),
        PlayerTemplate("Maheesh Theekshana", "BOWLER", 10, 84, 82, 82, 75, 24, "Sri Lanka", 20000000L),
        PlayerTemplate("Adam Zampa", "BOWLER", 10, 88, 82, 84, 85, 32, "Australia", 20000000L),

        // CAPPED PLAYERS FROM PDF PAGE 2
        PlayerTemplate("Mayank Agarwal", "BATSMAN", 80, 10, 80, 82, 82, 34, "India", 10000000L),
        PlayerTemplate("Faf Du Plessis", "BATSMAN", 86, 10, 88, 88, 92, 40, "South Africa", 20000000L),
        PlayerTemplate("Glenn Phillips", "BATSMAN", 82, 74, 88, 92, 78, 28, "New Zealand", 20000000L),
        PlayerTemplate("Rovman Powell", "BATSMAN", 82, 70, 80, 84, 76, 31, "West Indies", 15000000L),
        PlayerTemplate("Ajinkya Rahane", "BATSMAN", 80, 10, 84, 82, 88, 36, "India", 15000000L),
        PlayerTemplate("Prithvi Shaw", "BATSMAN", 78, 10, 75, 78, 70, 25, "India", 7500000L),
        PlayerTemplate("Kane Williamson", "BATSMAN", 88, 10, 84, 82, 92, 34, "New Zealand", 20000000L),
        PlayerTemplate("Sam Curran", "ALL_ROUNDER", 80, 82, 82, 85, 78, 26, "England", 20000000L),
        PlayerTemplate("Marco Jansen", "ALL_ROUNDER", 76, 84, 80, 84, 72, 24, "South Africa", 12500000L),
        PlayerTemplate("Daryl Mitchell", "ALL_ROUNDER", 83, 76, 82, 84, 78, 33, "New Zealand", 20000000L),
        PlayerTemplate("Krunal Pandya", "ALL_ROUNDER", 78, 79, 82, 82, 80, 34, "India", 20000000L),
        PlayerTemplate("Nitish Rana", "ALL_ROUNDER", 80, 70, 80, 82, 78, 31, "India", 15000000L),
        PlayerTemplate("Washington Sundar", "ALL_ROUNDER", 76, 82, 84, 85, 76, 25, "India", 20000000L),
        PlayerTemplate("Shardul Thakur", "ALL_ROUNDER", 75, 81, 80, 82, 80, 33, "India", 20000000L),
        PlayerTemplate("KS Bharat", "WICKET_KEEPER", 75, 10, 82, 82, 72, 31, "India", 7500000L),
        PlayerTemplate("Alex Carey", "WICKET_KEEPER", 80, 10, 84, 84, 80, 33, "Australia", 10000000L),
        PlayerTemplate("Donovan Ferreira", "WICKET_KEEPER", 76, 65, 80, 82, 60, 26, "South Africa", 7500000L),
        PlayerTemplate("Shai Hope", "WICKET_KEEPER", 82, 10, 80, 82, 80, 31, "West Indies", 12500000L),
        PlayerTemplate("Josh Inglis", "WICKET_KEEPER", 84, 10, 84, 85, 75, 30, "Australia", 20000000L),
        PlayerTemplate("Ryan Rickelton", "WICKET_KEEPER", 80, 10, 80, 84, 65, 28, "South Africa", 10000000L),
        PlayerTemplate("Deepak Chahar", "BOWLER", 25, 82, 80, 80, 80, 32, "India", 20000000L),
        PlayerTemplate("Gerald Coetzee", "BOWLER", 15, 84, 80, 85, 68, 24, "South Africa", 12500000L),
        PlayerTemplate("Akash Deep", "BOWLER", 15, 82, 80, 82, 65, 28, "India", 10000000L),
        PlayerTemplate("Tushar Deshpande", "BOWLER", 10, 82, 80, 82, 70, 29, "India", 10000000L),
        PlayerTemplate("Lockie Ferguson", "BOWLER", 10, 84, 80, 82, 78, 33, "New Zealand", 20000000L),
        PlayerTemplate("Bhuvneshwar Kumar", "BOWLER", 15, 83, 84, 82, 88, 35, "India", 20000000L),
        PlayerTemplate("Mukesh Kumar", "BOWLER", 10, 81, 80, 82, 72, 31, "India", 20000000L),
        PlayerTemplate("Allah Ghazanfar", "BOWLER", 10, 81, 78, 80, 50, 19, "Afghanistan", 7500000L),
        PlayerTemplate("Akeal Hosein", "BOWLER", 15, 83, 82, 82, 78, 31, "West Indies", 15000000L),
        PlayerTemplate("Keshav Maharaj", "BOWLER", 20, 84, 84, 84, 85, 35, "South Africa", 7500000L),
        PlayerTemplate("Mujeeb Ur Rahman", "BOWLER", 10, 83, 80, 82, 78, 24, "Afghanistan", 20000000L),
        PlayerTemplate("Adil Rashid", "BOWLER", 10, 84, 80, 80, 88, 37, "England", 20000000L),
        PlayerTemplate("Vijayakanth Viyaskanth", "BOWLER", 10, 78, 78, 82, 55, 23, "Sri Lanka", 7500000L),

        // CAPPED PLAYERS FROM PDF PAGE 3
        PlayerTemplate("Finn Allen", "BATSMAN", 84, 10, 80, 84, 70, 25, "New Zealand", 20000000L),
        PlayerTemplate("Dewald Brevis", "BATSMAN", 80, 68, 80, 84, 60, 21, "South Africa", 7500000L),
        PlayerTemplate("Ben Duckett", "BATSMAN", 84, 10, 80, 84, 78, 30, "England", 20000000L),
        PlayerTemplate("Manish Pandey", "BATSMAN", 78, 10, 85, 82, 85, 35, "India", 7500000L),
        PlayerTemplate("Rilee Rossouw", "BATSMAN", 83, 10, 80, 82, 80, 35, "South Africa", 20000000L),
        PlayerTemplate("Sherfane Rutherford", "BATSMAN", 80, 72, 80, 82, 72, 26, "West Indies", 15000000L),
        PlayerTemplate("Ashton Turner", "BATSMAN", 78, 68, 80, 82, 75, 32, "Australia", 10000000L),
        PlayerTemplate("James Vince", "BATSMAN", 81, 10, 82, 82, 82, 34, "England", 20000000L),
        PlayerTemplate("Shahbaz Ahamad", "ALL_ROUNDER", 76, 78, 82, 82, 72, 30, "India", 10000000L),
        PlayerTemplate("Moeen Ali", "ALL_ROUNDER", 82, 78, 82, 80, 88, 37, "England", 20000000L),
        PlayerTemplate("Tim David", "ALL_ROUNDER", 84, 65, 82, 85, 75, 29, "Australia", 20000000L),
        PlayerTemplate("Deepak Hooda", "ALL_ROUNDER", 78, 70, 80, 82, 75, 29, "India", 7500000L),
        PlayerTemplate("Will Jacks", "ALL_ROUNDER", 84, 72, 80, 84, 72, 26, "England", 20000000L),
        PlayerTemplate("Azmatullah Omarzai", "ALL_ROUNDER", 78, 81, 80, 84, 68, 25, "Afghanistan", 15000000L),
        PlayerTemplate("R. Sai Kishore", "ALL_ROUNDER", 70, 80, 80, 82, 68, 28, "India", 7500000L),
        PlayerTemplate("Romario Shepherd", "ALL_ROUNDER", 78, 79, 80, 82, 72, 30, "West Indies", 15000000L),
        PlayerTemplate("Tom Banton", "WICKET_KEEPER", 80, 10, 78, 82, 68, 26, "England", 20000000L),
        PlayerTemplate("Sam Billings", "WICKET_KEEPER", 81, 10, 84, 84, 82, 33, "England", 15000000L),
        PlayerTemplate("Jordan Cox", "WICKET_KEEPER", 76, 10, 80, 82, 55, 24, "England", 12500000L),
        PlayerTemplate("Ben McDermott", "WICKET_KEEPER", 78, 10, 78, 82, 68, 30, "Australia", 7500000L),
        PlayerTemplate("Kusal Mendis", "WICKET_KEEPER", 80, 10, 80, 84, 78, 30, "Sri Lanka", 7500000L),
        PlayerTemplate("Kusal Perera", "WICKET_KEEPER", 80, 10, 78, 82, 80, 34, "Sri Lanka", 7500000L),
        PlayerTemplate("Josh Philippe", "WICKET_KEEPER", 78, 10, 80, 82, 68, 27, "Australia", 7500000L),
        PlayerTemplate("Tim Seifert", "WICKET_KEEPER", 80, 10, 80, 82, 75, 30, "New Zealand", 12500000L),
        PlayerTemplate("Nandre Burger", "BOWLER", 10, 81, 80, 82, 65, 29, "South Africa", 12500000L),
        PlayerTemplate("Spencer Johnson", "BOWLER", 10, 83, 80, 84, 60, 29, "Australia", 20000000L),
        PlayerTemplate("Umran Malik", "BOWLER", 10, 80, 78, 85, 60, 25, "India", 7500000L),
        PlayerTemplate("Mustafizur Rahman", "BOWLER", 15, 84, 82, 82, 80, 29, "Bangladesh", 20000000L),
        PlayerTemplate("Ishant Sharma", "BOWLER", 15, 80, 80, 80, 88, 36, "India", 7500000L),
        PlayerTemplate("Nuwan Thushara", "BOWLER", 10, 81, 78, 82, 60, 30, "Sri Lanka", 7500000L),
        PlayerTemplate("Naveen Ul Haq", "BOWLER", 12, 82, 80, 82, 75, 25, "Afghanistan", 20000000L),
        PlayerTemplate("Jaydev Unadkat", "BOWLER", 18, 80, 80, 82, 80, 33, "India", 10000000L),
        PlayerTemplate("Umesh Yadav", "BOWLER", 15, 81, 80, 82, 85, 37, "India", 20000000L),
        PlayerTemplate("Rishad Hossain", "BOWLER", 10, 79, 80, 82, 55, 22, "Bangladesh", 7500000L),
        PlayerTemplate("Zahir Khan", "BOWLER", 10, 78, 78, 80, 60, 26, "Afghanistan", 7500000L),
        PlayerTemplate("Nqabayomzi Peter", "BOWLER", 10, 78, 78, 82, 48, 23, "South Africa", 7500000L),
        PlayerTemplate("Tanveer Sangha", "BOWLER", 10, 79, 80, 82, 55, 23, "Australia", 7500000L),
        PlayerTemplate("Tabraiz Shamsi", "BOWLER", 10, 83, 80, 80, 80, 35, "South Africa", 20000000L),
        PlayerTemplate("Jeffery Vandersay", "BOWLER", 10, 79, 78, 80, 68, 35, "Sri Lanka", 7500000L),

        // UNCAPPED PLAYERS FROM PDF
        PlayerTemplate("Yash Dhull", "BATSMAN", 75, 10, 78, 82, 55, 22, "India", 3000000L),
        PlayerTemplate("Abhinav Manohar", "BATSMAN", 76, 10, 78, 82, 58, 30, "India", 3000000L),
        PlayerTemplate("Karun Nair", "BATSMAN", 78, 10, 80, 80, 75, 33, "India", 3000000L),
        PlayerTemplate("Angkrish Raghuvanshi", "BATSMAN", 74, 10, 78, 82, 50, 20, "India", 3000000L),
        PlayerTemplate("Nehal Wadhera", "BATSMAN", 76, 10, 78, 82, 55, 24, "India", 3000000L),
        PlayerTemplate("Harpreet Brar", "ALL_ROUNDER", 68, 78, 80, 82, 68, 29, "India", 3000000L),
        PlayerTemplate("Sameer Rizvi", "ALL_ROUNDER", 74, 10, 76, 82, 50, 21, "India", 3000000L),
        PlayerTemplate("Abdul Samad", "ALL_ROUNDER", 75, 65, 78, 82, 65, 23, "India", 3000000L),
        PlayerTemplate("Ashutosh Sharma", "ALL_ROUNDER", 76, 60, 78, 82, 58, 26, "India", 3000000L),
        PlayerTemplate("Kumar Kushagra", "WICKET_KEEPER", 72, 10, 78, 82, 48, 20, "India", 3000000L),
        PlayerTemplate("Robin Minz", "WICKET_KEEPER", 71, 10, 78, 82, 48, 22, "India", 3000000L),
        PlayerTemplate("Anuj Rawat", "WICKET_KEEPER", 74, 10, 80, 82, 60, 25, "India", 3000000L),
        PlayerTemplate("Vaibhav Arora", "BOWLER", 10, 78, 78, 82, 60, 27, "India", 3000000L),
        PlayerTemplate("Rasikh Dar", "BOWLER", 10, 77, 78, 82, 55, 25, "India", 3000000L),
        PlayerTemplate("Akash Madhwal", "BOWLER", 10, 78, 78, 80, 58, 31, "India", 3000000L),
        PlayerTemplate("Simarjeet Singh", "BOWLER", 10, 77, 78, 82, 58, 27, "India", 3000000L),
        PlayerTemplate("Yash Thakur", "BOWLER", 10, 78, 78, 82, 58, 26, "India", 3000000L),
        PlayerTemplate("Kartik Tyagi", "BOWLER", 10, 77, 76, 82, 58, 24, "India", 4000000L),
        PlayerTemplate("Vyshak Vijaykumar", "BOWLER", 10, 77, 78, 82, 58, 28, "India", 3000000L),
        PlayerTemplate("Mayank Markande", "BOWLER", 10, 78, 78, 82, 65, 27, "India", 3000000L),
        PlayerTemplate("Suyash Sharma", "BOWLER", 10, 77, 78, 82, 52, 21, "India", 3000000L),
        PlayerTemplate("Manav Suthar", "BOWLER", 15, 76, 78, 82, 50, 22, "India", 3000000L),
        PlayerTemplate("Anshul Kamboj", "ALL_ROUNDER", 68, 76, 78, 82, 52, 24, "India", 3000000L),
        PlayerTemplate("Swapnil Singh", "ALL_ROUNDER", 70, 74, 80, 80, 70, 34, "India", 3000000L)
    )

    data class PlayerTemplate(
        val name: String,
        val category: String,
        val batting: Int,
        val bowling: Int,
        val fielding: Int,
        val fitness: Int,
        val experience: Int,
        val age: Int,
        val nationality: String,
        val basePrice: Long
    )

    fun generateInitialPlayers(slotId: String): List<PlayerEntity> {
        return PLAYER_POOL.mapIndexed { index, t ->
            PlayerEntity(
                playerId = "${slotId}_P_${index + 1}",
                slotId = slotId,
                teamId = null, // initially unsold / free agent
                name = t.name,
                category = t.category,
                batting = t.batting,
                bowling = t.bowling,
                fielding = t.fielding,
                fitness = t.fitness,
                experience = t.experience,
                form = 75, // initial form
                leadership = if (t.name.contains("Dhoni") || t.name.contains("Kholi") || t.name.contains("Sharma") || t.name.contains("Cummins")) 90 else 50,
                morale = 80,
                salary = t.basePrice, // base price in auction
                contractDuration = 3,
                age = t.age,
                nationality = t.nationality,
                isInjured = false,
                injuryDuration = 0
            )
        }
    }

    val STAFF_TEMPLATES = listOf(
        Triple("G. Kirsten", "HEAD", 92),
        Triple("S. Fleming", "HEAD", 94),
        Triple("A. Flower", "HEAD", 88),
        Triple("M. Jayawardene", "HEAD", 90),
        Triple("M. Hussey", "BATTING", 89),
        Triple("B. Lara", "BATTING", 91),
        Triple("D. Steyn", "BOWLING", 93),
        Triple("L. Malinga", "BOWLING", 95),
        Triple("J. Rhodes", "FIELDING", 96),
        Triple("M. Boucher", "FIELDING", 85),
        Triple("P. Carter", "FITNESS", 88),
        Triple("A. Leipus", "PHYSIO", 90),
        Triple("J. Srinath", "SCOUT", 85)
    )

    fun generateCoaches(slotId: String): List<CoachEntity> {
        return STAFF_TEMPLATES.mapIndexed { index, (name, role, rating) ->
            CoachEntity(
                coachId = "${slotId}_C_${index + 1}",
                slotId = slotId,
                teamId = null,
                name = name,
                role = role,
                rating = rating,
                salary = (rating * 100000L), // e.g. 9M per season
                isHired = false
            )
        }
    }

    val SPONSOR_TEMPLATES = listOf(
        Pair("TATA Group", "MAIN"),
        Pair("Dream11", "JERSEY"),
        Pair("Jio", "JERSEY"),
        Pair("Saudi Tourism", "STADIUM"),
        Pair("MRF Tyres", "EQUIPMENT"),
        Pair("CEAT", "EQUIPMENT")
    )

    fun generateSponsors(slotId: String): List<SponsorEntity> {
        return SPONSOR_TEMPLATES.mapIndexed { index, (name, type) ->
            SponsorEntity(
                sponsorId = "${slotId}_S_${index + 1}",
                slotId = slotId,
                teamId = null,
                name = name,
                type = type,
                payoutPerSeason = when (type) {
                    "MAIN" -> 80000000L // 8 Cr
                    "JERSEY" -> 40000000L // 4 Cr
                    "STADIUM" -> 30000000L // 3 Cr
                    else -> 15000000L // 1.5 Cr
                },
                isSigned = false
            )
        }
    }
}
