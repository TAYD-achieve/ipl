// IPL Franchise Manager Pro - Vercel Web Companion (M3 Light Edition)
// Client-Side Simulation Engine replicating Android Jetpack Compose Companion App

// --- 1. FRANCHISE DATA & CONFIG ---
const IPL_TEAMS = [
  { id: 'rcb', name: 'Royal Challengers Bengaluru', short: 'RCB', color: '#D32F2F', text: '#FFFFFF', slogan: 'Play Bold • IPL 2026 Championship Campaign', budget: 84.50 },
  { id: 'csk', name: 'Chennai Super Kings', short: 'CSK', color: '#FBC02D', text: '#1D1B20', slogan: 'Whistle Podu • 5-Time Champions', budget: 78.20 },
  { id: 'mi', name: 'Mumbai Indians', short: 'MI', color: '#1565C0', text: '#FFFFFF', slogan: 'One Family • Duniya Hila Denge', budget: 81.00 },
  { id: 'kkr', name: 'Kolkata Knight Riders', short: 'KKR', color: '#4A148C', text: '#FFFFFF', slogan: 'Korbo Lorbo Jeetbo • Knight Riders', budget: 75.80 },
  { id: 'rr', name: 'Rajasthan Royals', short: 'RR', color: '#E91E63', text: '#FFFFFF', slogan: 'Halla Bol • First Ever Champions', budget: 82.10 },
  { id: 'srh', name: 'Sunrisers Hyderabad', short: 'SRH', color: '#E65100', text: '#FFFFFF', slogan: 'Orange Army • Fire in the Stands', budget: 88.40 },
  { id: 'lsg', name: 'Lucknow Super Giants', short: 'LSG', color: '#006064', text: '#FFFFFF', slogan: 'Ab Apni Baari Hai • Super Giants', budget: 79.50 },
  { id: 'gt', name: 'Gujarat Titans', short: 'GT', color: '#0D47A1', text: '#FFFFFF', slogan: 'Aava De • Titans of T20', budget: 85.00 },
  { id: 'dc', name: 'Delhi Capitals', short: 'DC', color: '#b71c1c', text: '#FFFFFF', slogan: 'Roar Macha • Capital Pride', budget: 83.20 },
  { id: 'pbks', name: 'Punjab Kings', short: 'PBKS', color: '#C62828', text: '#FFFFFF', slogan: 'Sadda Punjab • Passion & Pride', budget: 89.10 }
];

const WEATHER_TYPES = [
  { id: 'dew', title: 'EVENING DEW (+8% Batting Advantage)', desc: 'Wet ball slips out of spinners\' hands. STRATEGY: Avoid extra spinners; favor fast pacers & big hitters.', batBoost: 1.08, bowlMod: 0.92 },
  { id: 'sunny', title: 'SUNNY AFTERNOON (+5% Batting Boost)', desc: 'Fast outfield & batsmen paradise. STRATEGY: Select aggressive batsmen & powerplay boundary hitters.', batBoost: 1.05, bowlMod: 0.95 },
  { id: 'overcast', title: 'HEAVY OVERCAST (-7% Batting, +15% Swing)', desc: 'Heavy atmospheric swing & seam movement. STRATEGY: Play extra fast bowlers & solid top-order anchors.', batBoost: 0.93, bowlMod: 1.15 },
  { id: 'rain', title: 'DAMP OUTFIELD (-14% Batting, +10% Grip)', desc: 'Wet outfield makes boundary hitting tough. STRATEGY: Play disciplined wicket-to-wicket bowlers & anchors.', batBoost: 0.86, bowlMod: 1.10 }
];

const INITIAL_SQUAD = [
  { id: 1, name: 'Virat Kohli', role: 'batsman', rating: 96, salary: 15.0, status: '3 Years Left' },
  { id: 2, name: 'Faf du Plessis', role: 'batsman', rating: 89, salary: 7.0, status: '1 Year Left' },
  { id: 3, name: 'Glenn Maxwell', role: 'allrounder', rating: 93, salary: 11.0, status: '2 Years Left' },
  { id: 4, name: 'Mohammed Siraj', role: 'bowler', rating: 91, salary: 7.0, status: '3 Years Left' },
  { id: 5, name: 'Cameron Green', role: 'allrounder', rating: 90, salary: 17.5, status: '3 Years Left' },
  { id: 6, name: 'Rajat Patidar', role: 'batsman', rating: 86, salary: 5.0, status: '2 Years Left' },
  { id: 7, name: 'Dinesh Karthik', role: 'keeper', rating: 85, salary: 5.5, status: '1 Year Left' },
  { id: 8, name: 'Will Jacks', role: 'allrounder', rating: 88, salary: 3.2, status: '2 Years Left' },
  { id: 9, name: 'Reece Topley', role: 'bowler', rating: 87, salary: 1.9, status: '1 Year Left' },
  { id: 10, name: 'Lockie Ferguson', role: 'bowler', rating: 86, salary: 2.0, status: '1 Year Left' },
  { id: 11, name: 'Yash Dayal', role: 'bowler', rating: 83, salary: 5.0, status: '2 Years Left' },
  { id: 12, name: 'Anuj Rawat', role: 'keeper', rating: 80, salary: 3.4, status: '2 Years Left' },
  { id: 13, name: 'Karn Sharma', role: 'bowler', rating: 82, salary: 0.5, status: '1 Year Left' },
  { id: 14, name: 'Mahipal Lomror', role: 'batsman', rating: 81, salary: 0.7, status: '2 Years Left' },
  { id: 15, name: 'Suyash Prabhudessai', role: 'batsman', rating: 79, salary: 0.3, status: '1 Year Left' },
  { id: 16, name: 'Vyshak Vijaykumar', role: 'bowler', rating: 80, salary: 0.2, status: '2 Years Left' },
  { id: 17, name: 'Akash Deep', role: 'bowler', rating: 82, salary: 0.2, status: '2 Years Left' },
  { id: 18, name: 'Rajan Kumar', role: 'bowler', rating: 78, salary: 0.7, status: '1 Year Left' }
];

const AUCTION_POOL = [
  { id: 101, name: 'Jasprit Bumrah', role: 'bowler', rating: 97, basePrice: 14.5, desc: 'World #1 Yorker Specialist' },
  { id: 102, name: 'Rashid Khan', role: 'bowler', rating: 96, basePrice: 13.0, desc: 'Elite Mystery Spinner' },
  { id: 103, name: 'Jos Buttler', role: 'keeper', rating: 95, basePrice: 12.5, desc: 'Explosive T20 Opener' },
  { id: 104, name: 'Heinrich Klaasen', role: 'keeper', rating: 94, basePrice: 11.0, desc: 'Middle-Order Spin Destroyer' },
  { id: 105, name: 'Sunil Narine', role: 'allrounder', rating: 93, basePrice: 10.5, desc: 'Powerplay Hitter & Spinner' },
  { id: 106, name: 'Travis Head', role: 'batsman', rating: 94, basePrice: 11.5, desc: 'Aggressive Top-Order Striker' },
  { id: 107, name: 'Mitchell Starc', role: 'bowler', rating: 92, basePrice: 12.0, desc: 'Left-Arm Express Pace' },
  { id: 108, name: 'Rinku Singh', role: 'batsman', rating: 89, basePrice: 7.5, desc: 'Clutch 5-Six Finisher' }
];

const COMM_TEMPLATES = [
  { text: 'Glorious cover drive! Raced away to the fence for 4 RUNS!', runs: 4, wkt: false },
  { text: 'MONSTER HIT! Sent soaring over deep mid-wicket into the second tier for SIX RUNS!', runs: 6, wkt: false },
  { text: 'Pushed into the gap at sweeper cover, they sprint through for 2 quick runs.', runs: 2, wkt: false },
  { text: 'Quick single rotated towards short third man. Excellent running between wickets!', runs: 1, wkt: false },
  { text: 'Good length delivery outside off, defended solidly to point for no run.', runs: 0, wkt: false },
  { text: 'OUT! Clean bowled! Perfectly executed swinging yorker rattles the middle stump!', runs: 0, wkt: true },
  { text: 'OUT! High in the air... and taken cleanly at deep square leg! Massive wicket for the bowling side!', runs: 0, wkt: true },
  { text: 'Smashed hard down the ground! Mid-off dives in vain, 4 RUNS!', runs: 4, wkt: false }
];

// --- 2. GLOBAL STATE ---
let gameState = {
  myTeamId: 'rcb',
  purse: 84.50,
  totalCap: 100.00,
  fans: 1250000,
  fanHappy: 88,
  stadiumCapacity: 40000,
  ticketPrice: 2500,
  squad: [...INITIAL_SQUAD],
  auctionPool: [...AUCTION_POOL],
  currentRound: 1,
  schedule: [],
  standings: [],
  activeMatch: null
};

// --- 3. STORAGE & PERSISTENCE (ROOM DB MIRROR) ---
function loadGameData() {
  const saved = localStorage.getItem('IPL_VERCEL_PRO_STATE_V3');
  if (saved) {
    try {
      const parsed = JSON.parse(saved);
      gameState = { ...gameState, ...parsed };
    } catch (e) {
      console.error('Error loading saved state, resetting to defaults.', e);
      initNewSeason();
    }
  } else {
    initNewSeason();
  }
}

window.saveGameData = function() {
  localStorage.setItem('IPL_VERCEL_PRO_STATE_V3', JSON.stringify(gameState));
  showToast('💾 Snapshot Persisted to LocalStorage (Room SQLite Mirror)!', 'success');
};

function initNewSeason() {
  gameState.currentRound = 1;
  gameState.schedule = generateFullSchedule();
  gameState.standings = IPL_TEAMS.map(t => ({
    id: t.id,
    name: t.name,
    short: t.short,
    pld: 0, won: 0, lost: 0, pts: 0,
    runsFor: 0, oversFor: 0, runsAgainst: 0, oversAgainst: 0,
    nrr: '0.000',
    form: ['-', '-', '-', '-', '-']
  }));
  saveGameData();
}

window.startNextSeason = function() {
  gameState.currentRound = 1;
  gameState.purse = Math.min(100, gameState.purse + 15.0);
  initNewSeason();
  updateUI();
  showToast('🏆 Season 2027 Started! Purse boosted by +₹15.0 Cr.', 'success');
};

// --- 4. SCHEDULE GENERATION & SIMULATION ---
function generateFullSchedule() {
  const rounds = [];
  const teams = [...IPL_TEAMS];
  for (let r = 1; r <= 14; r++) {
    const roundFixtures = [];
    // Simple round-robin pairings
    for (let i = 0; i < teams.length; i += 2) {
      const t1 = teams[(i + r) % teams.length];
      const t2 = teams[(i + r + 1) % teams.length];
      roundFixtures.push({
        id: `r${r}-m${i/2}`,
        round: r,
        teamAId: t1.id,
        teamBId: t2.id,
        played: false,
        scoreA: null,
        scoreB: null,
        winnerId: null
      });
    }
    rounds.push({ round: r, fixtures: roundFixtures });
  }
  return rounds;
}

window.simulateRoundCPU = function() {
  const roundObj = gameState.schedule.find(r => r.round === gameState.currentRound);
  if (!roundObj) return;

  let simulatedCount = 0;
  roundObj.fixtures.forEach(fix => {
    if (!fix.played && fix.teamAId !== gameState.myTeamId && fix.teamBId !== gameState.myTeamId) {
      // Simulate CPU match
      const runsA = Math.floor(Math.random() * 60) + 140; // 140 - 200
      const wktA = Math.floor(Math.random() * 8) + 2;
      const runsB = Math.floor(Math.random() * 60) + 135;
      const wktB = Math.floor(Math.random() * 8) + 2;

      fix.played = true;
      fix.scoreA = `${runsA}/${wktA}`;
      fix.scoreB = `${runsB}/${wktB}`;
      fix.winnerId = runsA >= runsB ? fix.teamAId : fix.teamBId;

      updateStandingsAfterMatch(fix.teamAId, fix.teamBId, runsA, 20, runsB, 20);
      simulatedCount++;
    }
  });

  if (simulatedCount > 0) {
    showToast(`⏩ Simulated ${simulatedCount} CPU fixtures for Round ${gameState.currentRound}!`, 'success');
    saveGameData();
    updateUI();
  } else {
    showToast('⚠️ No remaining CPU fixtures in this round. Play your featured match!', 'normal');
  }
};

function updateStandingsAfterMatch(teamAId, teamBId, runsA, oversA, runsB, oversB) {
  const stA = gameState.standings.find(s => s.id === teamAId);
  const stB = gameState.standings.find(s => s.id === teamBId);
  if (!stA || !stB) return;

  stA.pld++; stB.pld++;
  stA.runsFor += runsA; stA.oversFor += oversA;
  stA.runsAgainst += runsB; stA.oversAgainst += oversB;

  stB.runsFor += runsB; stB.oversFor += oversB;
  stB.runsAgainst += runsA; stB.oversAgainst += oversA;

  if (runsA > runsB) {
    stA.won++; stA.pts += 2;
    stB.lost++;
    stA.form.shift(); stA.form.push('W');
    stB.form.shift(); stB.form.push('L');
  } else {
    stB.won++; stB.pts += 2;
    stA.lost++;
    stB.form.shift(); stB.form.push('W');
    stA.form.shift(); stA.form.push('L');
  }

  // Calculate NRR
  [stA, stB].forEach(st => {
    const rf = st.runsFor / Math.max(1, st.oversFor);
    const ra = st.runsAgainst / Math.max(1, st.oversAgainst);
    const diff = rf - ra;
    st.nrr = (diff >= 0 ? '+' : '') + diff.toFixed(3);
  });

  // Sort standings by points then NRR
  gameState.standings.sort((a, b) => {
    if (b.pts !== a.pts) return b.pts - a.pts;
    return parseFloat(b.nrr) - parseFloat(a.nrr);
  });
}

// --- 5. LIVE MATCH DAY SIMULATION ENGINE ---
window.openMatchDaySimulation = function() {
  const roundObj = gameState.schedule.find(r => r.round === gameState.currentRound);
  if (!roundObj) {
    showToast('🏆 All 14 rounds completed! Start Next Season.', 'normal');
    return;
  }
  const myFix = roundObj.fixtures.find(f => f.teamAId === gameState.myTeamId || f.teamBId === gameState.myTeamId);
  if (!myFix) return;

  if (myFix.played) {
    showToast(`⚠️ You already completed your match for Round ${gameState.currentRound}! Switch to the next round in Schedule tab.`, 'normal');
    return;
  }

  const oppId = myFix.teamAId === gameState.myTeamId ? myFix.teamBId : myFix.teamAId;
  const oppTeam = IPL_TEAMS.find(t => t.id === oppId);
  const myTeam = IPL_TEAMS.find(t => t.id === gameState.myTeamId);

  const weather = WEATHER_TYPES[Math.floor(Math.random() * WEATHER_TYPES.length)];

  gameState.activeMatch = {
    fixtureId: myFix.id,
    round: gameState.currentRound,
    myTeam: myTeam,
    oppTeam: oppTeam,
    weather: weather,
    isTeamABattingFirst: true,
    scoreA: 0,
    wicketsA: 0,
    ballsA: 0,
    scoreB: 0,
    wicketsB: 0,
    ballsB: 0,
    isComplete: false,
    commentary: [
      `🎙️ Match Day ${gameState.currentRound} LIVE! ${myTeam.name} vs ${oppTeam.name} at M. Chinnaswamy Stadium.`,
      `🌤️ Pitch Forecast: ${weather.title}. ${weather.desc}`
    ]
  };

  // Populate Match Day Screen UI
  document.getElementById('matchday-matchup-title').innerText = `${myTeam.short} vs ${oppTeam.short} • Round ${gameState.currentRound}`;
  document.getElementById('matchday-weather-title').innerText = weather.title;
  document.getElementById('matchday-weather-desc').innerText = weather.desc;
  document.getElementById('match-team-a-name').innerText = `🦁 ${myTeam.name}`;
  document.getElementById('match-team-b-name').innerText = `🦅 ${oppTeam.name}`;
  document.getElementById('label-dom-a').innerText = `🦁 ${myTeam.short}`;
  document.getElementById('label-dom-b').innerText = `🦅 ${oppTeam.short}`;
  document.getElementById('label-health-a').innerText = `🏏 ${myTeam.short} Batting Health (Wickets Intact)`;
  document.getElementById('label-health-b').innerText = `🎯 ${oppTeam.short} Batting Health (Wickets Intact)`;
  document.getElementById('motm-card').style.display = 'none';

  updateMatchDayUI();

  // Show Match Day Screen
  document.querySelectorAll('.tab-screen').forEach(s => s.classList.remove('active'));
  document.getElementById('view-matchday').classList.add('active');
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

window.closeMatchDaySimulation = function() {
  document.getElementById('view-matchday').classList.remove('active');
  document.getElementById('view-home').classList.add('active');
  document.querySelector('.nav-item[data-tab="home"]').click();
  updateUI();
};

window.randomizeMatchWeather = function() {
  if (!gameState.activeMatch || gameState.activeMatch.isComplete) return;
  const weather = WEATHER_TYPES[Math.floor(Math.random() * WEATHER_TYPES.length)];
  gameState.activeMatch.weather = weather;
  document.getElementById('matchday-weather-title').innerText = weather.title;
  document.getElementById('matchday-weather-desc').innerText = weather.desc;
  addMatchCommentary(`🎲 Tactical Weather Update: Conditions shifted to ${weather.title}!`);
  showToast(`🌤️ Pitch forecast updated: ${weather.title}`, 'success');
};

function addMatchCommentary(txt, isRecent = true) {
  if (!gameState.activeMatch) return;
  gameState.activeMatch.commentary.unshift(txt);
  const list = document.getElementById('match-commentary-list');
  if (!list) return;
  list.innerHTML = gameState.activeMatch.commentary.slice(0, 15).map((c, idx) => `
    <div class="comm-item ${idx === 0 ? 'recent' : ''}">${c}</div>
  `).join('');
}

function formatOvers(balls) {
  return `${Math.floor(balls / 6)}.${balls % 6} ov`;
}

function updateMatchDayUI() {
  const m = gameState.activeMatch;
  if (!m) return;

  document.getElementById('match-score-a').innerText = `${m.scoreA}/${m.wicketsA}`;
  document.getElementById('match-overs-a').innerText = `(${formatOvers(m.ballsA)})`;
  document.getElementById('match-score-b').innerText = `${m.scoreB}/${m.wicketsB}`;
  document.getElementById('match-overs-b').innerText = `(${formatOvers(m.ballsB)})`;

  if (m.ballsA < 120 && m.wicketsA < 10) {
    document.getElementById('match-target-txt').innerText = '1st Innings in progress...';
  } else if (!m.isComplete) {
    const target = m.scoreA + 1;
    const needed = target - m.scoreB;
    const remBalls = 120 - m.ballsB;
    document.getElementById('match-target-txt').innerText = `Target: ${target} • Need ${needed} off ${remBalls} balls`;
  }

  // Calculate Batting Health (Wickets Intact)
  const healthA = Math.max(0, (10 - m.wicketsA) / 10);
  const healthB = Math.max(0, (10 - m.wicketsB) / 10);

  document.getElementById('match-health-a-val').innerText = `${10 - m.wicketsA}/10 Wickets (${Math.round(healthA * 100)}%)`;
  document.getElementById('match-health-a-bar').style.width = `${Math.round(healthA * 100)}%`;
  document.getElementById('match-health-b-val').innerText = `${10 - m.wicketsB}/10 Wickets (${Math.round(healthB * 100)}%)`;
  document.getElementById('match-health-b-bar').style.width = `${Math.round(healthB * 100)}%`;

  // Calculate Dominance Index
  const runDiff = (m.scoreA - m.scoreB) * 0.35;
  const rawA = Math.max(10, Math.min(180, (90 * 0.6) + (healthA * 50) + runDiff));
  const rawB = Math.max(10, Math.min(180, (88 * 0.6) + (healthB * 50) - runDiff));
  let dominanceA = Math.round((rawA / (rawA + rawB)) * 100);
  dominanceA = Math.max(15, Math.min(85, dominanceA));
  const dominanceB = 100 - dominanceA;

  document.getElementById('match-dominance-val').innerText = `${m.myTeam.short} (${dominanceA}%) vs ${m.oppTeam.short} (${dominanceB}%)`;
  document.getElementById('match-dominance-bar').style.width = `${dominanceA}%`;

  const advTag = document.getElementById('match-advantage-tag');
  if (dominanceA >= 50) {
    advTag.innerText = `🔥 ADVANTAGE: ${m.myTeam.short}`;
    advTag.style.borderColor = '#2E7D32'; advTag.style.color = '#2E7D32';
  } else {
    advTag.innerText = `🔥 ADVANTAGE: ${m.oppTeam.short}`;
    advTag.style.borderColor = '#D32F2F'; advTag.style.color = '#D32F2F';
  }

  // AI Tactical Coach
  const coachTxt = document.getElementById('match-tactical-insight');
  const winProb = document.getElementById('match-winprob');
  const rrr = document.getElementById('match-rrr');

  winProb.innerText = `${dominanceA}.4%`;
  if (dominanceA >= 60) {
    coachTxt.innerText = `With ${10 - m.wicketsA} wickets intact and a commanding ${dominanceA}% dominance rating, ${m.myTeam.name} should push aggressive boundary hitters in the death overs.`;
    rrr.innerText = '7.2 RPO';
  } else if (dominanceA <= 40) {
    coachTxt.innerText = `${m.oppTeam.name} has seized match momentum (${dominanceB}% dominance). Rotate strike immediately and avoid risk against strike spinners.`;
    rrr.innerText = '10.8 RPO';
  } else {
    coachTxt.innerText = `Match is evenly poised (${dominanceA}% vs ${dominanceB}%). A single wicket or boundary over will dramatically shift the momentum progress bar!`;
    rrr.innerText = '8.5 RPO';
  }

  // Check completion
  if ((m.ballsA >= 120 || m.wicketsA >= 10) && (m.ballsB >= 120 || m.wicketsB >= 10 || m.scoreB > m.scoreA)) {
    m.isComplete = true;
    finishMatchDay();
  }
}

window.simNextBall = function() {
  const m = gameState.activeMatch;
  if (!m || m.isComplete) {
    showToast('🏁 Match is completed! Check Man of the Match awards below.', 'normal');
    return;
  }

  const isTeamA = m.ballsA < 120 && m.wicketsA < 10 && (m.ballsA <= m.ballsB || m.ballsB >= 120 || m.wicketsB >= 10);
  const activeTeam = isTeamA ? m.myTeam : m.oppTeam;
  const batter = isTeamA ? 'Virat Kohli' : 'MS Dhoni';
  const bowler = isTeamA ? 'Jadeja' : 'Siraj';

  const evt = COMM_TEMPLATES[Math.floor(Math.random() * COMM_TEMPLATES.length)];
  let runs = evt.runs;
  let wkt = evt.wkt;

  if (m.weather.id === 'rain' && runs === 6 && Math.random() > 0.4) runs = 2;
  if (m.weather.id === 'sunny' && runs === 4 && Math.random() > 0.6) runs = 6;

  if (isTeamA) {
    m.ballsA++; m.scoreA += runs;
    if (wkt) m.wicketsA++;
    addMatchCommentary(`🏏 ${formatOvers(m.ballsA)} (${bowler} to ${batter}): ${evt.text}`);
  } else {
    m.ballsB++; m.scoreB += runs;
    if (wkt) m.wicketsB++;
    addMatchCommentary(`🎯 ${formatOvers(m.ballsB)} (Siraj to ${batter}): ${evt.text}`);
  }

  updateMatchDayUI();
};

window.simFullOver = function() {
  for (let i = 0; i < 6; i++) simNextBall();
};

window.simFiveOvers = function() {
  for (let i = 0; i < 30; i++) simNextBall();
  showToast('🚀 Simulated 5 full overs instantly!', 'success');
};

window.simInstantFinish = function() {
  const m = gameState.activeMatch;
  if (!m || m.isComplete) return;
  while (!m.isComplete) {
    simNextBall();
  }
};

function finishMatchDay() {
  const m = gameState.activeMatch;
  if (!m) return;

  const winner = m.scoreA >= m.scoreB ? m.myTeam : m.oppTeam;
  document.getElementById('match-target-txt').innerText = `🏁 MATCH FINISHED • ${winner.name} WON!`;
  addMatchCommentary(`🏆 MATCH COMPLETED! ${winner.name} emerges victorious at M. Chinnaswamy Stadium!`);

  // Update schedule fixture
  const roundObj = gameState.schedule.find(r => r.round === m.round);
  if (roundObj) {
    const fix = roundObj.fixtures.find(f => f.id === m.fixtureId);
    if (fix) {
      fix.played = true;
      fix.scoreA = `${m.scoreA}/${m.wicketsA}`;
      fix.scoreB = `${m.scoreB}/${m.wicketsB}`;
      fix.winnerId = winner.id;
    }
  }

  updateStandingsAfterMatch(m.myTeam.id, m.oppTeam.id, m.scoreA, Math.max(1, m.ballsA / 6), m.scoreB, Math.max(1, m.ballsB / 6));
  saveGameData();

  // Show Man of the Match Card
  const motmCard = document.getElementById('motm-card');
  if (motmCard) {
    motmCard.style.display = 'block';
    document.getElementById('motm-name').innerText = m.scoreA >= m.scoreB ? '👑 Virat Kohli (RCB)' : '👑 Ruturaj Gaikwad (CSK)';
    document.getElementById('motm-stats').innerText = m.scoreA >= m.scoreB ? '86* (48 balls, 8 Fours, 5 Sixes)' : '78 (44 balls, 6 Fours, 4 Sixes)';
  }

  showToast(`🏆 Match Finished! ${winner.name} takes 2 league points!`, 'success');
  updateUI();
}

// --- 6. SQUAD & AUCTION ACTIONS ---
window.renewPlayerContract = function(id) {
  const p = gameState.squad.find(x => x.id === id);
  if (!p) return;
  const raise = 1.5;
  if (gameState.purse - raise < 0) {
    showToast('❌ Insufficient Auction Purse balance to renew contract!', 'normal');
    return;
  }
  gameState.purse -= raise;
  p.salary += raise;
  p.status = '3 Years Left';
  p.rating = Math.min(99, p.rating + 1);
  saveGameData();
  updateUI();
  showToast(`✅ Renewed contract for ${p.name}! Salary bumped to ₹${p.salary.toFixed(2)} Cr/yr.`, 'success');
};

window.releasePlayerContract = function(id) {
  const idx = gameState.squad.findIndex(x => x.id === id);
  if (idx === -1) return;
  const removed = gameState.squad.splice(idx, 1)[0];
  gameState.purse += removed.salary;
  saveGameData();
  updateUI();
  showToast(`🚪 Released ${removed.name} into the auction pool. Added ₹${removed.salary.toFixed(2)} Cr to Purse!`, 'success');
};

window.scoutRookieTalent = function() {
  if (gameState.purse - 1.0 < 0) {
    showToast('❌ Insufficient purse balance to scout rookie talent!', 'normal');
    return;
  }
  gameState.purse -= 1.0;
  const roles = ['batsman', 'bowler', 'allrounder', 'keeper'];
  const names = ['Aarav Sharma', 'Vihaan Patel', 'Dhruv Jurel', 'Nehal Wadhera', 'Yashasvi Jaiswal', 'Mayank Yadav'];
  const rookie = {
    id: Date.now(),
    name: names[Math.floor(Math.random() * names.length)] + ' (Rookie)',
    role: roles[Math.floor(Math.random() * roles.length)],
    rating: Math.floor(Math.random() * 8) + 80, // 80 - 88
    salary: 1.2,
    status: '3 Years Left'
  };
  gameState.squad.push(rookie);
  saveGameData();
  updateUI();
  showToast(`✨ Scouted & signed young sensation ${rookie.name} (${rookie.rating} OVR) for ₹1.20 Cr!`, 'success');
};

window.bidAndSignPlayer = function(id) {
  const idx = gameState.auctionPool.findIndex(x => x.id === id);
  if (idx === -1) return;
  const p = gameState.auctionPool[idx];
  if (gameState.purse - p.basePrice < 0) {
    showToast('❌ Insufficient Auction Purse! Release players or adjust budget.', 'normal');
    return;
  }
  gameState.purse -= p.basePrice;
  gameState.auctionPool.splice(idx, 1);
  gameState.squad.push({
    id: p.id,
    name: p.name,
    role: p.role,
    rating: p.rating,
    salary: p.basePrice,
    status: '2 Years Left'
  });
  saveGameData();
  updateUI();
  showToast(`🛒 Signed superstar ${p.name} (${p.rating} OVR) to your active roster for ₹${p.basePrice.toFixed(2)} Cr!`, 'success');
};

window.upgradeStadium = function() {
  if (gameState.purse - 5.0 < 0) {
    showToast('❌ Need ₹5.0 Cr in purse to upgrade M. Chinnaswamy Stadium capacity!', 'normal');
    return;
  }
  gameState.purse -= 5.0;
  gameState.stadiumCapacity += 5000;
  gameState.fans += 50000;
  saveGameData();
  updateUI();
  showToast(`🏗️ M. Chinnaswamy Stadium upgraded! New capacity: ${gameState.stadiumCapacity.toLocaleString()} seats!`, 'success');
};

window.trainYouthAcademy = function() {
  if (gameState.purse - 2.0 < 0) {
    showToast('❌ Need ₹2.0 Cr in purse to conduct Youth Academy Bootcamp!', 'normal');
    return;
  }
  if (gameState.squad.length === 0) return;
  gameState.purse -= 2.0;
  // Pick lowest rated player and boost +3
  const sorted = [...gameState.squad].sort((a, b) => a.rating - b.rating);
  const target = sorted[0];
  target.rating = Math.min(99, target.rating + 3);
  saveGameData();
  updateUI();
  showToast(`⚡ Bootcamp complete! ${target.name} gained +3 OVR (Now ${target.rating} OVR)!`, 'success');
};

// --- 7. MODALS & TEAM SELECTION ---
window.openFranchiseModal = function() {
  const grid = document.getElementById('team-selection-grid');
  if (!grid) return;
  grid.innerHTML = IPL_TEAMS.map(t => `
    <div class="team-select-card" onclick="selectFranchiseTeam('${t.id}')">
      <div class="t-crest" style="background-color: ${t.color}; color: ${t.text};">${t.short}</div>
      <h4>${t.name}</h4>
      <p class="text-muted text-sm mt-1">${t.slogan.split('•')[0]}</p>
      <span class="ovr-pill purple mt-2 inline-block">Budget: ₹${t.budget.toFixed(2)} Cr</span>
    </div>
  `).join('');
  document.getElementById('franchise-modal').style.display = 'flex';
};

window.closeFranchiseModal = function() {
  document.getElementById('franchise-modal').style.display = 'none';
};

window.selectFranchiseTeam = function(id) {
  const t = IPL_TEAMS.find(x => x.id === id);
  if (!t) return;
  gameState.myTeamId = t.id;
  gameState.purse = t.budget;
  initNewSeason();
  closeFranchiseModal();
  updateUI();
  showToast(`🏏 Switched franchise to ${t.name}! Schedule and season data reset.`, 'success');
};

// --- 8. MASTER UI RENDERER ---
function updateUI() {
  const myTeam = IPL_TEAMS.find(t => t.id === gameState.myTeamId) || IPL_TEAMS[0];

  // 1. Top App Bar
  const crest = document.getElementById('header-team-crest');
  if (crest) {
    crest.style.backgroundColor = myTeam.color;
    crest.style.color = myTeam.text;
    document.getElementById('crest-text').innerText = myTeam.short;
  }
  document.getElementById('header-team-name').innerText = myTeam.name;
  document.getElementById('header-slogan').innerText = myTeam.slogan;
  document.getElementById('header-balance-val').innerText = `₹${gameState.purse.toFixed(2)} Cr`;
  document.getElementById('header-squad-val').innerText = `${gameState.squad.length} Players`;
  document.getElementById('header-fans-val').innerText = `${(gameState.fans / 1000000).toFixed(1)}M (${gameState.fanHappy}% Happy)`;

  // 2. Home Tab
  document.getElementById('home-season-status').innerText = `Season 2026 • Match Day ${gameState.currentRound} of 14`;
  document.getElementById('home-season-bar').style.width = `${Math.round((gameState.currentRound / 14) * 100)}%`;
  document.getElementById('home-stat-budget').innerText = `₹${gameState.purse.toFixed(2)} Cr`;
  document.getElementById('home-stat-squad').innerText = `${gameState.squad.length} Players`;
  document.getElementById('home-stat-fans').innerText = gameState.fans.toLocaleString();
  document.getElementById('home-stat-happy').innerText = `${gameState.fanHappy}% (Delighted)`;

  // Next Featured Match on Home
  const roundObj = gameState.schedule.find(r => r.round === gameState.currentRound);
  if (roundObj) {
    const myFix = roundObj.fixtures.find(f => f.teamAId === gameState.myTeamId || f.teamBId === gameState.myTeamId);
    if (myFix) {
      const oppId = myFix.teamAId === gameState.myTeamId ? myFix.teamBId : myFix.teamAId;
      const oppTeam = IPL_TEAMS.find(t => t.id === oppId);
      document.getElementById('home-match-round').innerText = gameState.currentRound;
      document.getElementById('home-my-crest').innerText = myTeam.short;
      document.getElementById('home-my-crest').style.backgroundColor = myTeam.color;
      document.getElementById('home-my-name').innerText = myTeam.name;
      document.getElementById('home-opp-crest').innerText = oppTeam.short;
      document.getElementById('home-opp-crest').style.backgroundColor = oppTeam.color;
      document.getElementById('home-opp-name').innerText = oppTeam.name;
    }
  }

  // 3. Schedule Tab
  const roundSelect = document.getElementById('schedule-round-select');
  if (roundSelect && roundSelect.children.length === 0) {
    for (let r = 1; r <= 14; r++) {
      const opt = document.createElement('option');
      opt.value = r; opt.innerText = `Round ${r}`;
      if (r === gameState.currentRound) opt.selected = true;
      roundSelect.appendChild(opt);
    }
    roundSelect.addEventListener('change', (e) => {
      gameState.currentRound = parseInt(e.target.value);
      updateUI();
    });
  } else if (roundSelect) {
    roundSelect.value = gameState.currentRound;
  }

  document.getElementById('schedule-round-title').innerText = `Match Day ${gameState.currentRound} Fixtures`;
  const fixturesList = document.getElementById('schedule-fixtures-list');
  if (fixturesList && roundObj) {
    fixturesList.innerHTML = roundObj.fixtures.map(f => {
      const tA = IPL_TEAMS.find(x => x.id === f.teamAId);
      const tB = IPL_TEAMS.find(x => x.id === f.teamBId);
      const isMyMatch = f.teamAId === gameState.myTeamId || f.teamBId === gameState.myTeamId;
      return `
        <div class="fixture-row ${isMyMatch ? 'my-match' : ''}">
          <div class="f-teams">
            <span style="color: ${tA.color};">${tA.short} (${tA.name})</span>
            <span class="f-vs">vs</span>
            <span style="color: ${tB.color};">${tB.short} (${tB.name})</span>
            ${isMyMatch ? '<span class="ovr-pill purple">MY MATCH</span>' : ''}
          </div>
          <div class="f-venue">📍 Stadium Arena</div>
          <div>
            ${f.played ? `<span class="f-result">✅ ${f.scoreA} vs ${f.scoreB}</span>` : 
              isMyMatch ? `<button class="btn btn-primary btn-sm pulse-btn" onclick="openMatchDaySimulation()">⚡ Play Match</button>` :
              `<span class="text-muted text-sm">⏳ CPU Fixture</span>`
            }
          </div>
        </div>
      `;
    }).join('');
  }

  // 4. Squad Tab
  renderSquadTable('all');

  // 5. Auction Tab
  document.getElementById('auction-purse-display').innerText = `₹${gameState.purse.toFixed(2)} Cr`;
  const auctionGrid = document.getElementById('auction-players-grid');
  if (auctionGrid) {
    if (gameState.auctionPool.length === 0) {
      auctionGrid.innerHTML = `<div class="col-span-2 text-muted">🎉 All available superstar players in the auction pool have been signed!</div>`;
    } else {
      auctionGrid.innerHTML = gameState.auctionPool.map(p => `
        <div class="auction-card">
          <div class="ac-top">
            <div class="ac-name">
              <h4>${p.name}</h4>
              <span class="role-badge ${p.role} mt-1">${p.role.toUpperCase()}</span>
            </div>
            <span class="ovr-pill purple">${p.rating} OVR</span>
          </div>
          <p class="text-muted text-sm">${p.desc}</p>
          <div class="ac-stats">
            <span>Base Price:</span>
            <span class="ac-price">₹${p.basePrice.toFixed(2)} Cr</span>
          </div>
          <button class="btn btn-primary btn-block mt-2" onclick="bidAndSignPlayer(${p.id})">💰 Bid & Sign Player</button>
        </div>
      `).join('');
    }
  }

  // 6. Finances Tab
  const usedCap = gameState.squad.reduce((sum, p) => sum + p.salary, 0);
  const usedPct = Math.min(100, (usedCap / gameState.totalCap) * 100);
  document.getElementById('finance-used-bar').style.width = `${usedPct}%`;
  document.getElementById('finance-avail-bar').style.width = `${100 - usedPct}%`;
  document.getElementById('finance-used-val').innerText = `₹${usedCap.toFixed(2)} Cr`;
  document.getElementById('finance-avail-val').innerText = `₹${gameState.purse.toFixed(2)} Cr`;

  // 7. Standings Table
  const tbody = document.getElementById('standings-table-body');
  if (tbody) {
    tbody.innerHTML = gameState.standings.map((st, idx) => {
      const isMyTeam = st.id === gameState.myTeamId;
      return `
        <tr style="${isMyTeam ? 'background-color: #FCF8FF; font-weight: 700;' : ''}">
          <td><strong>#${idx + 1}</strong></td>
          <td>${st.name} ${isMyTeam ? '<span class="green-txt">(MY TEAM)</span>' : ''}</td>
          <td>${st.pld}</td>
          <td><strong class="green-txt">${st.won}</strong></td>
          <td>${st.lost}</td>
          <td><strong class="purple-txt" style="font-size: 1.1rem;">${st.pts}</strong></td>
          <td class="mono-val">${st.nrr}</td>
          <td>
            <div class="form-badges">
              ${st.form.map(f => `<span class="f-badge ${f === 'W' ? 'win' : f === 'L' ? 'loss' : ''}">${f}</span>`).join('')}
            </div>
          </td>
        </tr>
      `;
    }).join('');
  }
}

function renderSquadTable(roleFilter = 'all') {
  const tbody = document.getElementById('squad-table-body');
  if (!tbody) return;

  const filtered = roleFilter === 'all' ? gameState.squad : gameState.squad.filter(p => p.role === roleFilter);

  // Update counts
  document.getElementById('count-all').innerText = gameState.squad.length;
  document.getElementById('count-bat').innerText = gameState.squad.filter(p => p.role === 'batsman').length;
  document.getElementById('count-bowl').innerText = gameState.squad.filter(p => p.role === 'bowler').length;
  document.getElementById('count-allr').innerText = gameState.squad.filter(p => p.role === 'allrounder').length;
  document.getElementById('count-keep').innerText = gameState.squad.filter(p => p.role === 'keeper').length;

  tbody.innerHTML = filtered.map(p => `
    <tr>
      <td><strong>${p.name}</strong></td>
      <td><span class="role-badge ${p.role}">${p.role.toUpperCase()}</span></td>
      <td><span class="ovr-pill purple">${p.rating} OVR</span></td>
      <td><strong>₹${p.salary.toFixed(2)} Cr</strong> / yr</td>
      <td><span class="${p.status.includes('1 Year') ? 'text-muted' : 'green-txt'}">${p.status}</span></td>
      <td class="text-right">
        <div style="display: inline-flex; gap: 0.5rem;">
          <button class="btn btn-sm btn-outline" onclick="renewPlayerContract(${p.id})">📝 Renew (+₹1.5 Cr)</button>
          <button class="btn btn-sm btn-outline" style="color: #D32F2F; border-color: #D32F2F;" onclick="releasePlayerContract(${p.id})">🚪 Release</button>
        </div>
      </td>
    </tr>
  `).join('');
}

// --- 9. EVENT LISTENERS & INITIALIZATION ---
document.addEventListener('DOMContentLoaded', () => {
  loadGameData();

  // Navigation tab switcher
  document.querySelectorAll('.nav-item').forEach(btn => {
    btn.addEventListener('click', () => {
      const tab = btn.getAttribute('data-tab');
      document.querySelectorAll('.nav-item').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      document.querySelectorAll('.tab-screen').forEach(s => s.classList.remove('active'));
      const targetScreen = document.getElementById(`view-${tab}`);
      if (targetScreen) targetScreen.classList.add('active');
      window.scrollTo({ top: 0, behavior: 'smooth' });
    });
  });

  // Squad filters
  document.querySelectorAll('.s-filter').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.s-filter').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      renderSquadTable(btn.getAttribute('data-role'));
    });
  });

  // Top App Bar buttons
  document.getElementById('btn-save-snapshot')?.addEventListener('click', saveGameData);
  document.getElementById('btn-switch-team')?.addEventListener('click', openFranchiseModal);
  document.getElementById('btn-sim-round')?.addEventListener('click', simulateRoundCPU);

  // Ticket Price Slider
  const slider = document.getElementById('ticket-price-slider');
  if (slider) {
    slider.addEventListener('input', (e) => {
      const val = parseInt(e.target.value);
      gameState.ticketPrice = val;
      document.getElementById('ticket-price-display').innerText = `₹${val.toLocaleString()} / Seat`;
      const att = Math.max(45, Math.min(100, 100 - ((val - 2500) / 90)));
      const gate = ((val * gameState.stadiumCapacity * 7 * (att / 100)) / 10000000).toFixed(2);
      document.getElementById('ticket-att-val').innerText = `${att.toFixed(1)}% (${att >= 95 ? 'Full House' : 'Moderate'})`;
      document.getElementById('ticket-gate-val').innerText = `₹${gate} Cr`;
      document.getElementById('finance-gate-val').innerText = `+₹${gate} Cr`;
    });
  }

  updateUI();
});

// Toast notification function
window.showToast = function(msg, type = 'normal') {
  const container = document.getElementById('toast-container');
  if (!container) return;
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `<span>${msg}</span>`;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(20px)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
};
