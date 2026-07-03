// IPL Franchise Manager Pro - Full Interactive Web & Simulation Engine
// Built to mirror the native Android Kotlin + Jetpack Compose companion app

// --- 1. INITIAL FRANCHISE & STATE DATA ---
let franchisePurse = 84.50; // in Crores
const totalCap = 100.00;
let ticketPrice = 2500;

// Weather Modifiers
const weatherTypes = [
  { class: 'dew', title: '💧 EVENING DEW (+8% Batting Advantage)', desc: 'Wet ball slips out of spinners\' hands. STRATEGY: Avoid extra spinners; favor fast pacers & big hitters.', batMod: 1.08 },
  { class: 'sunny', title: '☀️ SUNNY (+5% Batting Boost)', desc: 'Fast outfield & batsmen paradise. STRATEGY: Select aggressive batsmen & powerplay boundary hitters.', batMod: 1.05 },
  { class: 'overcast', title: '☁️ OVERCAST (-7% Batting, +15% Swing)', desc: 'Heavy atmospheric swing & seam movement. STRATEGY: Play extra fast bowlers & solid top-order anchors.', batMod: 0.93 },
  { class: 'rain', title: '🌧️ RAIN / DAMP (-14% Batting, +10% Grip)', desc: 'Wet outfield makes boundary hitting tough. STRATEGY: Play disciplined wicket-to-wicket bowlers & anchors.', batMod: 0.86 },
  { class: 'windy', title: '💨 STRONG WIND (-5% Batting, +12% Drift)', desc: 'Crosswinds create drift for spinners. STRATEGY: Pick mystery spinners & slow left-arm bowlers.', batMod: 0.95 }
];
let currentWeatherIndex = 0;

// Live Match Simulation State
let matchState = {
  scoreA: 142,
  wicketsA: 3,
  ballsA: 86, // 14.2 overs
  scoreB: 118,
  wicketsB: 5,
  ballsB: 86, // 14.2 overs
  isComplete: false
};

// Roster Data
const initialSquad = [
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
let squad = [...initialSquad];

// Standings Data
const standingsData = [
  { rank: 1, name: 'Chennai Super Kings', pld: 10, won: 7, lost: 3, pts: 14, nrr: '+0.845', form: ['W','W','W','L','W'] },
  { rank: 2, name: 'Royal Challengers Bengaluru', pld: 10, won: 6, lost: 4, pts: 12, nrr: '+0.612', form: ['W','W','L','W','W'] },
  { rank: 3, name: 'Kolkata Knight Riders', pld: 10, won: 6, lost: 4, pts: 12, nrr: '+0.420', form: ['L','W','W','W','L'] },
  { rank: 4, name: 'Rajasthan Royals', pld: 10, won: 6, lost: 4, pts: 12, nrr: '+0.315', form: ['W','L','W','L','W'] },
  { rank: 5, name: 'Sunrisers Hyderabad', pld: 10, won: 5, lost: 5, pts: 10, nrr: '+0.110', form: ['W','L','L','W','W'] },
  { rank: 6, name: 'Mumbai Indians', pld: 10, won: 5, lost: 5, pts: 10, nrr: '-0.085', form: ['L','W','W','L','L'] },
  { rank: 7, name: 'Lucknow Super Giants', pld: 10, won: 4, lost: 6, pts: 8, nrr: '-0.245', form: ['L','L','W','W','L'] },
  { rank: 8, name: 'Gujarat Titans', pld: 10, won: 4, lost: 6, pts: 8, nrr: '-0.410', form: ['W','L','L','L','W'] },
  { rank: 9, name: 'Delhi Capitals', pld: 10, won: 4, lost: 6, pts: 8, nrr: '-0.560', form: ['L','W','L','L','L'] },
  { rank: 10, name: 'Punjab Kings', pld: 10, won: 3, lost: 7, pts: 6, nrr: '-1.012', form: ['L','L','L','W','L'] }
];

// Commentary Templates
const commTemplates = [
  { text: 'Glorious cover drive! Raced away to the boundary for 4 RUNS!', runs: 4, wkt: false },
  { text: 'MONSTER HIT! Sent soaring over deep mid-wicket for SIX RUNS!', runs: 6, wkt: false },
  { text: 'Pushed into the gap at sweeper cover, they sprint through for 2 runs.', runs: 2, wkt: false },
  { text: 'Quick single rotated towards short third man. Excellent calling!', runs: 1, wkt: false },
  { text: 'Good length delivery outside off, defended solidly to point for no run.', runs: 0, wkt: false },
  { text: 'OUT! Clean bowled! Perfectly executed yorker rattles the middle stump!', runs: 0, wkt: true },
  { text: 'OUT! High in the air... and taken cleanly at deep square leg! Massive wicket!', runs: 0, wkt: true },
  { text: 'Smashed hard down the ground! Mid-off dives but can\'t stop it, 4 RUNS!', runs: 4, wkt: false }
];

// --- 2. NAVIGATION TAB LOGIC ---
window.switchTab = function(tabId) {
  document.querySelectorAll('.nav-tab').forEach(btn => {
    btn.classList.toggle('active', btn.getAttribute('data-tab') === tabId);
  });
  document.querySelectorAll('.tab-view').forEach(view => {
    view.classList.toggle('active', view.id === `view-${tabId}`);
  });
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

// --- 3. MATCH SIMULATION ENGINE ---
function formatOvers(balls) {
  const overs = Math.floor(balls / 6);
  const rem = balls % 6;
  return `${overs}.${rem} ov`;
}

function updateMatchUI() {
  document.getElementById('live-score-a').innerText = `${matchState.scoreA}/${matchState.wicketsA}`;
  document.getElementById('live-overs-a').innerText = `(${formatOvers(matchState.ballsA)})`;
  
  document.getElementById('live-score-b').innerText = `${matchState.scoreB}/${matchState.wicketsB}`;
  document.getElementById('live-overs-b').innerText = `(${formatOvers(matchState.ballsB)})`;

  // Calculate Batting Health
  const healthA = Math.max(0, (10 - matchState.wicketsA) / 10);
  const healthB = Math.max(0, (10 - matchState.wicketsB) / 10);

  // Calculate Algorithmic Dominance Index
  const runDiff = (matchState.scoreA - matchState.scoreB) * 0.25;
  const rawA = Math.max(10, Math.min(160, (88.4 * 0.6) + (healthA * 45) + runDiff));
  const rawB = Math.max(10, Math.min(160, (88.0 * 0.6) + (healthB * 45) - runDiff));
  let dominanceA = Math.round((rawA / (rawA + rawB)) * 100);
  dominanceA = Math.max(15, Math.min(85, dominanceA));
  const dominanceB = 100 - dominanceA;

  // Update Visual Progress Bars
  document.getElementById('dominance-val').innerText = `RCB (${dominanceA}%) vs CSK (${dominanceB}%)`;
  document.getElementById('dominance-bar').style.width = `${dominanceA}%`;

  document.getElementById('health-a-val').innerText = `${10 - matchState.wicketsA}/10 Wickets (${Math.round(healthA * 100)}%)`;
  document.getElementById('health-a-bar').style.width = `${Math.round(healthA * 100)}%`;

  document.getElementById('health-b-val').innerText = `${10 - matchState.wicketsB}/10 Wickets (${Math.round(healthB * 100)}%)`;
  document.getElementById('health-b-bar').style.width = `${Math.round(healthB * 100)}%`;

  // Advantage Tag
  const advTag = document.getElementById('advantage-tag');
  if (dominanceA >= 50) {
    advTag.innerText = '🔥 ADVANTAGE: Royal Challengers';
    advTag.style.borderColor = '#4caf50';
    advTag.style.color = '#4caf50';
  } else {
    advTag.innerText = '🔥 ADVANTAGE: Super Kings';
    advTag.style.borderColor = '#ff7043';
    advTag.style.color = '#ff7043';
  }

  // Tactical AI Coach
  const coachTxt = document.getElementById('coach-insight');
  const winProb = document.getElementById('c-winprob');
  const rrr = document.getElementById('c-rrr');

  winProb.innerText = `${dominanceA}.4%`;
  if (dominanceA >= 60) {
    coachTxt.innerText = `With ${10 - matchState.wicketsA} wickets intact and a commanding ${dominanceA}% dominance rating, Royal Challengers should push for aggressive boundary hitters in the death overs.`;
    rrr.innerText = '7.2 RPO';
  } else if (dominanceA <= 40) {
    coachTxt.innerText = `Super Kings have seized match momentum (${dominanceB}% dominance). Royal Challengers must rotate strike and avoid risk against strike spinners.`;
    rrr.innerText = '10.8 RPO';
  } else {
    coachTxt.innerText = `Match is evenly poised (${dominanceA}% vs ${dominanceB}%). A single wicket or boundary over will dramatically shift the momentum progress bar!`;
    rrr.innerText = '8.5 RPO';
  }

  // Check completion
  if (matchState.ballsA >= 120 && matchState.ballsB >= 120 || matchState.wicketsA >= 10 && matchState.wicketsB >= 10) {
    matchState.isComplete = true;
    document.getElementById('comm-status').innerText = '🏁 MATCH FINISHED';
    document.getElementById('comm-status').style.color = '#fbc02d';
  }
}

function addCommentary(text, isRecent = true) {
  const list = document.getElementById('commentary-list');
  const div = document.createElement('div');
  div.className = isRecent ? 'comm-item recent' : 'comm-item';
  div.innerText = text;
  
  if (isRecent) {
    Array.from(list.children).forEach(el => el.classList.remove('recent'));
  }
  list.prepend(div);
  if (list.children.length > 12) {
    list.removeChild(list.lastChild);
  }
}

function simulateBall() {
  if (matchState.isComplete) {
    showToast('⚠️ Match is completed! Click Reset Match Simulation to play again.', 'normal');
    return;
  }

  const isTeamA = matchState.ballsA <= matchState.ballsB && matchState.wicketsA < 10 && matchState.ballsA < 120;
  const activeTeam = isTeamA ? 'RCB' : 'CSK';
  const batters = isTeamA ? ['Virat Kohli', 'Faf du Plessis', 'Glenn Maxwell', 'Dinesh Karthik'] : ['MS Dhoni', 'Ruturaj Gaikwad', 'Ravindra Jadeja', 'Shivam Dube'];
  const bowler = isTeamA ? 'Jadeja' : 'Siraj';
  const batter = batters[Math.floor(Math.random() * batters.length)];

  const event = commTemplates[Math.floor(Math.random() * commTemplates.length)];
  const wWeather = weatherTypes[currentWeatherIndex];

  let runs = event.runs;
  let wkt = event.wkt;

  // Weather modifiers
  if (wWeather.class === 'rain' && runs === 6 && Math.random() > 0.4) runs = 2;
  if (wWeather.class === 'sunny' && runs === 4 && Math.random() > 0.6) runs = 6;

  if (isTeamA) {
    matchState.ballsA++;
    matchState.scoreA += runs;
    if (wkt) matchState.wicketsA++;
    addCommentary(`🏏 ${formatOvers(matchState.ballsA)} (${bowler} to ${batter}): ${event.text}`);
  } else {
    matchState.ballsB++;
    matchState.scoreB += runs;
    if (wkt) matchState.wicketsB++;
    addCommentary(`🎯 ${formatOvers(matchState.ballsB)} (Siraj to ${batter}): ${event.text}`);
  }

  updateMatchUI();
}

function simulateOver() {
  for (let i = 0; i < 6; i++) {
    if (!matchState.isComplete) simulateBall();
  }
}

function fastForwardOvers(overs) {
  for (let i = 0; i < overs * 6; i++) {
    if (!matchState.isComplete) simulateBall();
  }
  showToast(`🚀 Simulated ${overs} full overs instantly!`, 'success');
}

function resetMatch() {
  matchState = {
    scoreA: 142,
    wicketsA: 3,
    ballsA: 86,
    scoreB: 118,
    wicketsB: 5,
    ballsB: 86,
    isComplete: false
  };
  document.getElementById('comm-status').innerText = '● LIVE';
  document.getElementById('comm-status').style.color = '#4caf50';
  
  const list = document.getElementById('commentary-list');
  list.innerHTML = `
    <div class="comm-item recent">🔄 Match simulation reset! Ready for the death overs at M. Chinnaswamy Stadium.</div>
    <div class="comm-item">⚡ 14.1 (Siraj to Dhoni): Yorker right on off stump, dug out safely to point for no run.</div>
  `;
  updateMatchUI();
  showToast('🔄 Match Day simulation reset to 14.2 overs!', 'success');
}

function changeWeather() {
  currentWeatherIndex = (currentWeatherIndex + 1) % weatherTypes.length;
  const w = weatherTypes[currentWeatherIndex];
  const box = document.getElementById('weather-box');
  box.className = `weather-box ${w.class}`;
  box.innerHTML = `
    <div class="weather-title">${w.title}</div>
    <div class="weather-desc">${w.desc}</div>
  `;
  addCommentary(`🌤️ Weather Update: Conditions shifted to ${w.title.split(' ')[1]}! Tactical modifications engaged.`);
  showToast(`🌤️ Pitch conditions changed to ${w.title.split(' ')[1]}!`, 'success');
}

// --- 4. SQUAD & ROSTER MANAGEMENT ---
function renderSquad(filterRole = 'all') {
  const tbody = document.getElementById('squad-tbody');
  if (!tbody) return;
  tbody.innerHTML = '';

  const filtered = filterRole === 'all' ? squad : squad.filter(p => p.role === filterRole);

  filtered.forEach(p => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><strong>${p.name}</strong></td>
      <td><span class="role-badge ${p.role}">${p.role.toUpperCase()}</span></td>
      <td><span class="rating-tag purple">${p.rating} OVR</span></td>
      <td><strong>₹${p.salary.toFixed(2)} Cr</strong> / yr</td>
      <td><span style="color: ${p.status.includes('1 Year') ? '#ff7043' : '#4caf50'}">${p.status}</span></td>
      <td>
        <div style="display: flex; gap: 0.5rem;">
          <button class="btn btn-sm btn-outline" onclick="renewContract(${p.id})">📝 Renew Contract</button>
          <button class="btn btn-sm btn-outline" style="color: #ff7043;" onclick="releasePlayer(${p.id})">🚪 Release</button>
        </div>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

window.renewContract = function(id) {
  const player = squad.find(p => p.id === id);
  if (!player) return;
  const raise = 1.5;
  if (franchisePurse - raise < 0) {
    showToast('❌ Insufficient Auction Purse to extend contract!', 'normal');
    return;
  }
  franchisePurse -= raise;
  player.salary += raise;
  player.status = '3 Years Left';
  player.rating = Math.min(99, player.rating + 1);
  updatePurseUI();
  renderSquad();
  showToast(`✅ Contract renewed for ${player.name}! Salary bumped to ₹${player.salary.toFixed(2)} Cr.`, 'success');
};

window.releasePlayer = function(id) {
  const index = squad.findIndex(p => p.id === id);
  if (index === -1) return;
  const removed = squad.splice(index, 1)[0];
  franchisePurse += removed.salary;
  updatePurseUI();
  renderSquad();
  showToast(`🚪 Released ${removed.name} into the auction pool. Added ₹${removed.salary.toFixed(2)} Cr to Purse!`, 'success');
};

// --- 5. FINANCES & STANDINGS ---
function updatePurseUI() {
  document.getElementById('header-purse-val').innerText = `₹${franchisePurse.toFixed(2)} Cr`;
  const used = totalCap - franchisePurse;
  const usedPct = (used / totalCap) * 100;
  
  const purseBar = document.querySelector('.purse-bar');
  if (purseBar) {
    purseBar.innerHTML = `
      <div class="p-fill used" style="width: ${usedPct}%;"></div>
      <div class="p-fill avail" style="width: ${100 - usedPct}%;"></div>
    `;
  }
  
  const legend = document.querySelector('.purse-legend');
  if (legend) {
    legend.innerHTML = `
      <span>🔴 Squad Salaries: <strong>₹${used.toFixed(2)} Cr</strong> (${usedPct.toFixed(1)}%)</span>
      <span>🟢 Remaining Purse: <strong>₹${franchisePurse.toFixed(2)} Cr</strong> (${(100 - usedPct).toFixed(1)}%)</span>
      <span>🏆 Total Cap: <strong>₹${totalCap.toFixed(2)} Cr</strong></span>
    `;
  }
}

function renderStandings() {
  const tbody = document.getElementById('standings-tbody');
  if (!tbody) return;
  tbody.innerHTML = '';

  standingsData.forEach(t => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><strong>#${t.rank}</strong></td>
      <td>${t.name} ${t.name.includes('Royal Challengers') ? '<span style="color: #4caf50;">(MY TEAM)</span>' : ''}</td>
      <td>${t.pld}</td>
      <td><strong class="green-txt">${t.won}</strong></td>
      <td>${t.lost}</td>
      <td><strong style="font-size: 1.1rem; color: #8a63f2;">${t.pts}</strong></td>
      <td>${t.nrr}</td>
      <td>
        <div class="form-badges" style="transform: scale(0.85); transform-origin: left;">
          ${t.form.map(f => `<span class="form-badge ${f === 'W' ? 'win' : 'loss'}">${f}</span>`).join('')}
        </div>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

// --- 6. TOAST NOTIFICATIONS ---
window.showToast = function(msg, type = 'normal') {
  const container = document.getElementById('toast-container');
  if (!container) return;
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `<span>${msg}</span>`;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(20px)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
};

// --- 7. EVENT LISTENERS & INITIALIZATION ---
document.addEventListener('DOMContentLoaded', () => {
  // Tab click listeners
  document.querySelectorAll('.nav-tab').forEach(btn => {
    btn.addEventListener('click', () => {
      const tab = btn.getAttribute('data-tab');
      switchTab(tab);
    });
  });

  // Simulation Controls
  document.getElementById('btn-sim-ball')?.addEventListener('click', simulateBall);
  document.getElementById('btn-sim-over')?.addEventListener('click', simulateOver);
  document.getElementById('btn-sim-inn')?.addEventListener('click', () => fastForwardOvers(5));
  document.getElementById('btn-reset-match')?.addEventListener('click', resetMatch);
  document.getElementById('btn-weather')?.addEventListener('click', changeWeather);

  // Roster Filter buttons
  document.querySelectorAll('.filter-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      renderSquad(btn.getAttribute('data-role'));
    });
  });

  // Ticket slider
  const slider = document.getElementById('ticket-slider');
  if (slider) {
    slider.addEventListener('input', (e) => {
      const val = parseInt(e.target.value);
      ticketPrice = val;
      document.getElementById('ticket-val').innerText = `₹${val.toLocaleString()} / Seat`;
      
      // Calculate attendance and gate revenue
      const att = Math.max(40, Math.min(100, 100 - ((val - 2500) / 100)));
      const gate = ((val * 40000 * 7 * (att / 100)) / 10000000).toFixed(2);
      
      document.getElementById('att-val').innerText = `${att.toFixed(1)}% (${att >= 95 ? 'Full House' : 'Moderate'})`;
      document.getElementById('gate-val').innerText = `₹${gate} Cr`;
    });
  }

  // Season reset
  document.getElementById('btn-reset-season')?.addEventListener('click', () => {
    franchisePurse = 84.50;
    squad = [...initialSquad];
    resetMatch();
    updatePurseUI();
    renderSquad();
    renderStandings();
    showToast('🔄 Full season data and franchise purse reset to initial state!', 'success');
  });

  // Initial renders
  updateMatchUI();
  renderSquad();
  renderStandings();
  updatePurseUI();
});
