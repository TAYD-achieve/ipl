// Interactive Live Demo Logic for IPL Franchise Manager Pro Web Showcase

const weatherTypes = [
  {
    class: 'sunny',
    title: '☀️ SUNNY (+5% Batting Boost)',
    desc: 'Fast outfield & batsmen paradise. STRATEGY: Select aggressive batsmen & powerplay boundary hitters.',
    batMod: 1.05,
    bowlerMod: 1.0
  },
  {
    class: 'overcast',
    title: '☁️ OVERCAST (-7% Batting, +15% Swing)',
    desc: 'Heavy atmospheric swing & seam movement. STRATEGY: Play extra fast bowlers & solid top-order anchors.',
    batMod: 0.93,
    bowlerMod: 1.15
  },
  {
    class: 'rain',
    title: '🌧️ RAIN / DAMP (-14% Batting, +10% Grip)',
    desc: 'Wet outfield makes boundary hitting tough. STRATEGY: Play disciplined wicket-to-wicket bowlers & anchors.',
    batMod: 0.86,
    bowlerMod: 1.10
  },
  {
    class: 'dew',
    title: '💧 EVENING DEW (+8% Batting Advantage)',
    desc: 'Wet ball slips out of spinners\' hands. STRATEGY: Avoid extra spinners; favor fast pacers & big hitters.',
    batMod: 1.08,
    bowlerMod: 0.90
  },
  {
    class: 'windy',
    title: '💨 STRONG WIND (-5% Batting, +12% Drift)',
    desc: 'Crosswinds create drift for spinners. STRATEGY: Pick mystery spinners & slow left-arm bowlers.',
    batMod: 0.95,
    bowlerMod: 1.12
  }
];

let currentWeatherIndex = 0;
let teamAScore = 142;
let teamAWickets = 3;
let teamABalls = 86; // 14.2 overs

let teamBScore = 118;
let teamBWickets = 5;
let teamBBalls = 86; // 14.2 overs

const playerNames = ['Virat Kohli', 'Faf du Plessis', 'Glenn Maxwell', 'Dinesh Karthik', 'MS Dhoni', 'Ravindra Jadeja', 'Ruturaj Gaikwad', 'Shivam Dube'];
const commentaryTemplates = [
  { text: 'Crisp cover drive! That races to the boundary for 4 RUNS!', runs: 4, wkt: false },
  { text: 'Massive hit over long-on! SIX RUNS into the upper tier!', runs: 6, wkt: false },
  { text: 'Quick single rotated towards deep square leg. Good running!', runs: 1, wkt: false },
  { text: 'Pushed gently to mid-off, they scamper through for 2 runs.', runs: 2, wkt: false },
  { text: 'Good length delivery on off stump, defended solidly for no run.', runs: 0, wkt: false },
  { text: 'OUT! Edged and taken cleanly by the wicketkeeper! What a breakthrough!', runs: 0, wkt: true },
  { text: 'Yorker right on the base of middle stump! Batsman digs it out for a single.', runs: 1, wkt: false }
];

function formatOvers(balls) {
  const overs = Math.floor(balls / 6);
  const rem = balls % 6;
  return `${overs}.${rem} ov`;
}

function updateUI() {
  // Update score elements
  document.getElementById('score-a').innerText = `${teamAScore}/${teamAWickets}`;
  document.getElementById('overs-a').innerText = `(${formatOvers(teamABalls)})`;

  document.getElementById('score-b').innerText = `${teamBScore}/${teamBWickets}`;
  document.getElementById('overs-b').innerText = `(${formatOvers(teamBBalls)})`;

  // Calculate health
  const healthA = Math.max(0, (10 - teamAWickets) / 10);
  const healthB = Math.max(0, (10 - teamBWickets) / 10);

  // Calculate dominance
  const diff = (teamAScore - teamBScore) * 0.3;
  const rawA = Math.max(10, Math.min(160, 75 * 0.6 + healthA * 45 + diff));
  const rawB = Math.max(10, Math.min(160, 75 * 0.6 + healthB * 45 - diff));
  let dominanceA = Math.round((rawA / (rawA + rawB)) * 100);
  dominanceA = Math.max(15, Math.min(85, dominanceA));
  const dominanceB = 100 - dominanceA;

  // Update progress bars
  document.getElementById('dominance-val').innerText = `${dominanceA}% vs ${dominanceB}%`;
  document.getElementById('dominance-bar').style.width = `${dominanceA}%`;

  document.getElementById('health-a-val').innerText = `${10 - teamAWickets}/10 Wickets (${Math.round(healthA * 100)}%)`;
  document.getElementById('health-a-bar').style.width = `${Math.round(healthA * 100)}%`;

  document.getElementById('health-b-val').innerText = `${10 - teamBWickets}/10 Wickets (${Math.round(healthB * 100)}%)`;
  document.getElementById('health-b-bar').style.width = `${Math.round(healthB * 100)}%`;

  // Update Advantage Badge
  const badge = document.getElementById('advantage-tag');
  if (dominanceA >= 50) {
    badge.innerText = '🔥 ADVANTAGE: Royal Challengers';
    badge.style.borderColor = '#4caf50';
    badge.style.color = '#4caf50';
  } else {
    badge.innerText = '🔥 ADVANTAGE: Super Kings';
    badge.style.borderColor = '#ff7043';
    badge.style.color = '#ff7043';
  }

  // Tactical Insight
  const insight = document.getElementById('tactical-insight');
  if (dominanceA >= 60) {
    insight.innerText = `With ${10 - teamAWickets} wickets intact and a dominant ${dominanceA}% dominance rating, Royal Challengers should push for aggressive boundary hitters in the death overs.`;
  } else if (dominanceA <= 40) {
    insight.innerText = `Super Kings have seized match control (${dominanceB}% dominance). Royal Challengers must rotate strike and avoid risk against strike bowlers.`;
  } else {
    insight.innerText = `Match is evenly poised (${dominanceA}% vs ${dominanceB}%). A single wicket or boundary over will dramatically shift the momentum progress bar!`;
  }
}

function addCommentary(text, isRecent = true) {
  const box = document.getElementById('commentary-box');
  const div = document.createElement('div');
  div.className = isRecent ? 'comm-item recent' : 'comm-item';
  div.innerText = text;
  
  // Remove recent class from previous children
  if (isRecent) {
    Array.from(box.children).forEach(el => el.classList.remove('recent'));
  }
  
  box.prepend(div);
  if (box.children.length > 8) {
    box.removeChild(box.lastChild);
  }
}

function simulateBall() {
  if (teamAWickets >= 10 && teamBWickets >= 10) {
    addCommentary('🏁 Match Completed! Click Reset Match to start a new simulation.');
    return;
  }

  // Decide which team batts
  const isTeamA = teamABalls <= teamBBalls && teamAWickets < 10;
  const activeTeam = isTeamA ? 'Royal Challengers' : 'Super Kings';
  const player = playerNames[Math.floor(Math.random() * playerNames.length)];

  // Pick commentary event
  const event = commentaryTemplates[Math.floor(Math.random() * commentaryTemplates.length)];
  const wWeather = weatherTypes[currentWeatherIndex];

  let runs = event.runs;
  let wkt = event.wkt;

  // Apply weather modifier chance
  if (wWeather.class === 'rain' && runs === 6 && Math.random() > 0.5) {
    runs = 2; // ball plugged in outfield
  } else if (wWeather.class === 'sunny' && runs === 4 && Math.random() > 0.7) {
    runs = 6;
  }

  if (isTeamA) {
    teamABalls++;
    teamAScore += runs;
    if (wkt) teamAWickets++;
    addCommentary(`🏏 ${formatOvers(teamABalls)} (${activeTeam} - ${player}): ${event.text}`);
  } else {
    teamBBalls++;
    teamBScore += runs;
    if (wkt) teamBWickets++;
    addCommentary(`🦅 ${formatOvers(teamBBalls)} (${activeTeam} - ${player}): ${event.text}`);
  }

  updateUI();
}

function simulateOver() {
  for (let i = 0; i < 6; i++) {
    simulateBall();
  }
}

function resetMatch() {
  teamAScore = 142;
  teamAWickets = 3;
  teamABalls = 86;

  teamBScore = 118;
  teamBWickets = 5;
  teamBBalls = 86;

  const box = document.getElementById('commentary-box');
  box.innerHTML = `
    <div class="comm-item recent">🔄 Match simulation reset! Ready for the final overs.</div>
    <div class="comm-item">⚡ 14.1: Good length delivery on off stump, defended solidly to point for no run.</div>
  `;

  updateUI();
}

function randomizeWeather() {
  currentWeatherIndex = (currentWeatherIndex + 1) % weatherTypes.length;
  const w = weatherTypes[currentWeatherIndex];
  
  const box = document.getElementById('weather-display');
  box.className = `weather-box ${w.class}`;
  box.innerHTML = `
    <div class="weather-title">${w.title}</div>
    <div class="weather-desc">${w.desc}</div>
  `;

  addCommentary(`🌤️ Weather Update: Conditions changed to ${w.title.split(' ')[1]}! Tactical adjustments advised.`);
}

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('sim-ball-btn')?.addEventListener('click', simulateBall);
  document.getElementById('sim-over-btn')?.addEventListener('click', simulateOver);
  document.getElementById('reset-match-btn')?.addEventListener('click', resetMatch);
  document.getElementById('randomize-weather-btn')?.addEventListener('click', randomizeWeather);
  
  updateUI();
});
