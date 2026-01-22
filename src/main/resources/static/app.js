const API = 'http://localhost:8083';

function fmtTimeReadable(seconds) {
  if (seconds === null || seconds === undefined) return '—';
  seconds = Math.max(0, Math.floor(seconds));
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = seconds % 60;
  if (h > 0) return `${h}h ${m}m`;
  if (m > 0) return `${m}m`;
  return `${s}s`;
}

function fmtPercent(conf) {
  if (conf === null || conf === undefined) return '—';
  let v = Number(conf);
  if (isNaN(v)) return '—';
  if (v > 1.5) return Math.round(v * 100) / 100 + '%';
  return Math.round(v * 1000) / 10 + '%';
}

function el(t,c){ const e=document.createElement(t); if(c) e.className=c; return e; }

function safePie(canvas,labels,values,cols){
  if(!canvas) return;
  try{
    if(typeof Chart === 'undefined'){ canvas.style.display='none'; return; }
    if(canvas._chart){ try{ canvas._chart.destroy(); }catch(e){} }
    canvas._chart = new Chart(canvas.getContext('2d'), {
      type:'pie',
      data:{ labels, datasets:[{ data:values, backgroundColor:cols||['#60a5fa','#f97316'] }] },
      options:{
        responsive:true,
        maintainAspectRatio:false,
        plugins:{ legend:{ position:'bottom', labels:{ boxWidth:14 } } },
        layout:{ padding: { top:6, bottom:6 } }
      }
    });
    canvas.style.display = '';
  }catch(e){ console.error('chart',e); canvas.style.display='none'; }
}

async function api(path, opts){
  try{
    const r = await fetch(API + path, opts);
    if(!r.ok){
      const txt = await r.text().catch(()=>r.statusText);
      throw new Error(`${r.status} ${r.statusText} - ${txt}`);
    }
    return await r.json().catch(()=>null);
  }catch(e){ throw e; }
}

function parseNumericFlexible(v, preferSecondsForDurations = true){
  if (v === undefined || v === null || v === '') return null;
  if (typeof v === 'number' && !isNaN(v)) return v;
  let s = String(v).trim();
  s = s.replace(/,/g,'').replace(/\u00A0/g,'').trim();
  const pctMatch = s.match(/^([+-]?\d+(\.\d+)?)\s*%$/);
  if (pctMatch) return Number(pctMatch[1]);
  const iso = s.match(/^P(T)?(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?$/i);
  if (iso) {
    const hh = Number(iso[2] || 0), mm = Number(iso[3] || 0), ss = Number(iso[4] || 0);
    return hh*3600 + mm*60 + ss;
  }
  const sufMatch = s.match(/^([+-]?\d+(\.\d+)?)(ms|s|m|h)$/i);
  if (sufMatch) {
    const n = Number(sufMatch[1]);
    const unit = sufMatch[3].toLowerCase();
    if (unit === 'ms') return n/1000;
    if (unit === 's') return n;
    if (unit === 'm') return n*60;
    if (unit === 'h') return n*3600;
  }
  const n = Number(s);
  if (!isNaN(n)) {
    if (preferSecondsForDurations && Math.abs(n) >= 100000) return n / 1000;
    return n;
  }
  return null;
}

function readAnyNumber(obj, keys){
  if(!obj) return null;
  for(const k of keys){
    if(Object.prototype.hasOwnProperty.call(obj,k)){
      const raw = obj[k];
      const parsed = parseNumericFlexible(raw, true);
      if (parsed !== null) return parsed;
    }
  }
  return null;
}

function deriveActiveIdleFromMetrics(m){
  if(!m) return { occupancy: null, active: null, idle: null, utilPercent: null };
  const totalActive = readAnyNumber(m, [
    'totalActiveSeconds','activeSeconds','active_seconds','activeSecondsTotal','activeDuration','active_time',
    'total_active_seconds','active','activeMs','active_millis','activeMillis','active_milliseconds','workingSeconds','working_seconds','working'
  ]);
  const totalIdle = readAnyNumber(m, [
    'totalIdleSeconds','idleSeconds','idle_seconds','idleDuration','total_idle_seconds','idle',
    'idleMs','idle_millis','idleMillis','idle_milliseconds'
  ]);
  const occupancy = readAnyNumber(m, [
    'occupancySeconds','occupancy_seconds','occupancy','totalOccupancySeconds','total_occupancy_seconds',
    'occupancyDuration','occupancyMs','occupancy_millis','occupancyMillis','occupancy_milliseconds'
  ]);
  const utilPercent = readAnyNumber(m, [
    'utilizationPercent','utilization_percent','utilization','utilizationPct','utilPercent','utilPercent100',
    'util','util_pct','util_percent'
  ]);
  const observed = readAnyNumber(m, [
    'observedSeconds','windowSeconds','window_seconds','periodSeconds','period_seconds','durationSeconds','duration_seconds',
    'sampledSeconds','sampled_seconds','totalObservedSeconds','total_observed_seconds','reportingWindowSeconds','reporting_window_seconds'
  ]);
  let occ = (occupancy !== null) ? occupancy : ((totalActive || 0) + (totalIdle || 0));
  if ((occ === null || occ === 0) && observed !== null) occ = observed;
  if (occ === 0) occ = null;
  let active = null;
  if (totalActive !== null) active = totalActive;
  else if (occ !== null && utilPercent !== null) {
    let up = utilPercent;
    if (up <= 1.5) up = up * 100;
    active = Math.round( occ * (up/100) );
  } else {
    active = readAnyNumber(m, ['workingSeconds','working_seconds','working','workSeconds','work_seconds','activeWindowSeconds']);
  }
  let idle = null;
  if (totalIdle !== null) idle = totalIdle;
  else if (occ !== null && active !== null) idle = Math.max(0, occ - active);
  else idle = readAnyNumber(m, ['idle','idleSeconds','idle_seconds','idleDuration']);
  let util = utilPercent;
  if ((util === null || util === undefined) && occ !== null && active !== null && occ > 0) {
    util = (active / occ) * 100;
  }
  if (active === null || occ === null) {
    console.debug('deriveActiveIdleFromMetrics: partial',{ occupancyRaw: occupancy, totalActiveRaw: totalActive, totalIdleRaw: totalIdle, utilPercentRaw: utilPercent, observedRaw: observed, computed: {occ, active, idle, util} });
  }
  return {
    occupancy: occ !== null ? occ : null,
    active: active !== null ? active : null,
    idle: idle !== null ? idle : null,
    utilPercent: util !== null ? util : null
  };
}

let workers=[], stations=[];
let factoryRefreshTimer = null;

async function setError(msg){
  const b = document.getElementById('errorBanner');
  if(!b) return;
  if(!msg){ b.classList.add('hidden'); b.textContent=''; return; }
  b.classList.remove('hidden');
  b.textContent = msg;
}

function avatar(id){
  const cols=['#7c3aed','#06b6d4','#f97316','#10b981','#ef4444','#f59e0b'];
  let h=0;
  for(let i=0;i<id.length;i++){ h=(h<<5)-h + id.charCodeAt(i); h|=0; }
  const c = cols[Math.abs(h)%cols.length];
  const d = el('div','worker-avatar');
  d.style.background=c;
  d.textContent = id.replace(/^W/,'');
  return d;
}

async function fillWorkers(){
  const box=document.getElementById('workersBox');
  box.innerHTML='';
  try{
    if(!workers.length) workers = await api('/workers');
    if(!Array.isArray(workers) || !workers.length){ box.innerHTML = '<div class="empty">No workers</div>'; return; }
    for(const w of workers){
      const row = el('div','worker-item');
      row.dataset.workerId = w.workerId;
      row.appendChild(avatar(w.workerId));
      const info = el('div');
      const name = el('div','worker-name'); name.textContent = w.name;
      const meta = el('div','worker-meta'); meta.textContent = w.workerId;
      info.appendChild(name); info.appendChild(meta);
      row.appendChild(info);
      row.addEventListener('click', ()=> selectWorker(w.workerId));
      box.appendChild(row);
    }
  }catch(e){
    box.innerHTML = '<div class="empty">Failed to load workers</div>';
    setError('Failed to load workers: ' + (e.message||e));
    console.error(e);
  }
}

async function fillStations(){
  const box=document.getElementById('stationsBox');
  box.innerHTML='';
  try{
    if(!stations.length) stations = await api('/workstations');
    if(!Array.isArray(stations) || !stations.length){ box.innerHTML = '<div class="empty">No workstations</div>'; return; }
    for(const s of stations){
      const row = el('div','station-item');
      const left = el('div');
      left.innerHTML = `<div style="font-weight:700">${s.name}</div><div style="font-size:12px;color:var(--muted)">${s.workstationId}</div>`;
      const rightWrap = el('div');
      const miniBox = el('div','mini-chart-box');
      const canvas = el('canvas'); canvas.className = 'mini-chart';
      miniBox.appendChild(canvas); rightWrap.appendChild(miniBox);
      row.appendChild(left); row.appendChild(rightWrap);
      row.addEventListener('click', ()=> selectStation(s.workstationId));
      box.appendChild(row);
      try{
        const m = await api(`/metrics/workstations/${s.workstationId}`);
        const occ = (m.occupancySeconds !== undefined && m.occupancySeconds !== null)
          ? m.occupancySeconds
          : ( (m.totalActiveSeconds||0) + (m.totalIdleSeconds||0) );
        const active = (m.utilizationPercent !== undefined && m.utilizationPercent !== null)
          ? Math.round( occ * ((m.utilizationPercent||0)/100) )
          : (m.totalActiveSeconds||0);
        const idle = Math.max(0, occ - active);
        safePie(canvas,['Occ','Idle'],[active,idle],['#34d399','#f97316']);
        const unitsDiv = el('div'); unitsDiv.style.fontSize = '13px'; unitsDiv.style.color = 'var(--muted)'; unitsDiv.style.marginTop = '6px';
        unitsDiv.textContent = `Units: ${m.totalUnitsProduced || 0}`;
        left.appendChild(unitsDiv);
      }catch(e){
        canvas.style.display='none';
      }
    }
  }catch(e){
    box.innerHTML = '<div class="empty">Failed to load workstations</div>';
    setError('Failed to load workstations: ' + (e.message||e));
    console.error(e);
  }
}

async function fillFactory(){
  const box=document.getElementById('factoryBox');
  box.innerHTML='';
  try{
    const m = await api('/metrics/factory');
    const root = el('div'); root.className = 'factory-summary';
    function normalizeUtilToPercent(v){
      if (v === undefined || v === null || v === '') return null;
      const n = parseNumericFlexible(v, false);
      if (n === null) return null;
      return (n <= 1.5) ? (n * 100) : n;
    }
    let avgUtilPercent = null;
    if (m && (m.averageUtilizationPercentAcrossWorkers !== undefined && m.averageUtilizationPercentAcrossWorkers !== null)) {
      const cand = normalizeUtilToPercent(m.averageUtilizationPercentAcrossWorkers);
      if (cand !== null && Math.abs(cand) > 0.0001) avgUtilPercent = cand;
    }
    if (avgUtilPercent === null) {
      try {
        if(!workers.length) workers = await api('/workers');
        if (Array.isArray(workers) && workers.length) {
          const metrics = await Promise.allSettled(workers.map(w => api(`/metrics/workers/${w.workerId}`)));
          const vals = [];
          for (const r of metrics) {
            if (r.status === 'fulfilled' && r.value) {
              const u = normalizeUtilToPercent(r.value.utilizationPercent ?? r.value.utilization_percent ?? r.value.utilization);
              if (u !== null) vals.push(u);
            }
          }
          if (vals.length) avgUtilPercent = vals.reduce((a,b)=>a+b,0) / vals.length;
        }
      }catch(err){
        console.warn('failed to compute per-worker avg utilization', err);
      }
    }
    if (avgUtilPercent === null) {
      try {
        if(!stations.length) stations = await api('/workstations');
        if (Array.isArray(stations) && stations.length) {
          const metrics = await Promise.allSettled(stations.map(s => api(`/metrics/workstations/${s.workstationId}`)));
          const vals = [];
          for (const r of metrics) {
            if (r.status === 'fulfilled' && r.value) {
              const u = normalizeUtilToPercent(r.value.utilizationPercent ?? r.value.utilization_percent ?? r.value.utilization);
              if (u !== null) vals.push(u);
            }
          }
          if (vals.length) avgUtilPercent = vals.reduce((a,b)=>a+b,0) / vals.length;
        }
      }catch(err){
        console.warn('failed to compute per-station avg utilization', err);
      }
    }
    const tiles = [
      {k:'Productive time', v: fmtTimeReadable(m.totalProductiveSeconds||m.total_productive_seconds||0)},
      {k:'Total units', v: m.totalProductionCount||m.product_count||0},
      {k:'Avg units/hr', v: m.averageProductionRatePerHour||m.avg_units_hour||0},
      {k:'Avg utilization', v: (avgUtilPercent !== null) ? fmtPercent(avgUtilPercent) : '—' }
    ];
    tiles.forEach(t=>{
      const tile = el('div','factory-tile');
      const h = el('h3'); h.textContent = t.k;
      const p = el('p'); p.textContent = t.v;
      tile.appendChild(h); tile.appendChild(p); root.appendChild(tile);
    });
    box.appendChild(root);
  }catch(e){
    box.innerHTML = '<div class="empty">Failed to load factory</div>';
    setError('Failed to load factory metrics: ' + (e.message||e));
    console.error(e);
  }
}

function makeStatsHtml(dataPairs){
  const wrapper = el('div','detail-stats');
  dataPairs.forEach(([k,v])=>{
    const kv = el('div','kv');
    const lab = el('div'); lab.className='label'; lab.textContent = k;
    const val = el('div'); val.className='value'; val.textContent = v;
    kv.appendChild(lab); kv.appendChild(val); wrapper.appendChild(kv);
  });
  return wrapper;
}

function computeActiveIdleFromEvents(events, windowEndMillis = Date.now()) {
  if (!Array.isArray(events) || events.length === 0) return { active: 0, idle: 0 };
  const ev = events.slice().sort((a,b)=>new Date(a.timestamp) - new Date(b.timestamp));
  let active = 0;
  let idle = 0;
  for (let i = 0; i < ev.length; i++) {
    const cur = ev[i];
    const start = new Date(cur.timestamp).getTime();
    const end = (i + 1 < ev.length) ? new Date(ev[i+1].timestamp).getTime() : windowEndMillis;
    if (isNaN(start) || isNaN(end) || end <= start) continue;
    const secs = Math.floor((end - start) / 1000);
    const type = (cur.eventType || '').toLowerCase();
    let countAsActive = false;
    if (type === 'working') countAsActive = true;
    if (type === 'product_count') {
      if (i + 1 >= ev.length) countAsActive = true;
      else {
        const prev = (i-1 >= 0) ? (ev[i-1].eventType || '').toLowerCase() : '';
        const next = (i+1 < ev.length) ? (ev[i+1].eventType || '').toLowerCase() : '';
        if (prev === 'working' || next === 'working') countAsActive = true;
      }
    }
    if (countAsActive) active += secs;
    else if (type === 'idle') idle += secs;
    else if (type === 'absent') { /* skip for now */ }
  }
  return { active, idle };
}

async function selectWorker(id){
  const sel = document.getElementById('selectedBox');
  sel.innerHTML = '';
  try{
    const [m, evs] = await Promise.all([
      api(`/metrics/workers/${id}`),
      api(`/events/workers/${id}/recent?limit=50`)
    ]);

    const derived = deriveActiveIdleFromMetrics(m);
    let activeSeconds = (derived.active !== null) ? derived.active
      : (parseNumericFlexible(m.totalActiveSeconds ?? m.activeSeconds ?? m.active ?? m.activeMs ?? m.active_millis ?? m.workingSeconds ?? m.working_seconds) ?? null);
    let idleSeconds = (derived.idle !== null) ? derived.idle
      : (parseNumericFlexible(m.totalIdleSeconds ?? m.idleSeconds ?? m.idle ?? m.idleMs ?? m.idle_millis) ?? null);
    const util = (derived.utilPercent !== null) ? derived.utilPercent
      : (parseNumericFlexible(m.utilizationPercent ?? m.utilization_percent ?? m.utilization ?? m.utilPct ?? m.util) ?? null);

    if ((activeSeconds === null || activeSeconds === 0) && Array.isArray(evs) && evs.length) {
      const computed = computeActiveIdleFromEvents(evs);
      if (computed && (computed.active > 0 || computed.idle > 0)) {
        activeSeconds = computed.active;
        idleSeconds = computed.idle;
      }
    }

    const card = el('div','detail-card');
    const chartCol = el('div','chart-box');
    const canvas = el('canvas'); canvas.className = 'chart-canvas';
    chartCol.appendChild(canvas);
    const statsCol = makeStatsHtml([
      ['Active', fmtTimeReadable(activeSeconds !== null ? activeSeconds : null)],
      ['Idle', fmtTimeReadable(idleSeconds !== null ? idleSeconds : null)],
      ['Utilization', fmtPercent(util !== null ? util : null)],
      ['Total units', m.totalUnitsProduced || m.product_count || 0],
      ['Units/hr', m.unitsPerHour || m.productionRatePerHour || 0]
    ]);
    const eventsCol = el('div','events-column');

    try{
      const list = el('div','event-list');
      if(!evs||!evs.length){ list.innerHTML = '<div class="empty">No recent events</div>'; }
      else {
        evs.forEach(ev=>{
          const r = el('div','event-item');
          const ts = new Date(ev.timestamp).toLocaleString();
          const type = ev.eventType;
          const countPart = (ev.count !== undefined && ev.count !== null) ? ` · ${ev.count}` : '';
          const confPart = (ev.confidence !== undefined && ev.confidence !== null) ? ` · conf:${fmtPercent(ev.confidence)}` : '';
          r.textContent = `${ts} · ${type}${countPart}${confPart}`;
          list.appendChild(r);
        });
      }
      eventsCol.appendChild(list);
    }catch(e){
      eventsCol.appendChild(el('div','empty')).textContent='Failed to load recent events';
    }

    card.appendChild(chartCol);
    const rightCol = el('div');
    rightCol.appendChild(statsCol);
    rightCol.appendChild(eventsCol);
    card.appendChild(rightCol);
    sel.appendChild(card);
    safePie(canvas,['Working','Idle'],[Number(activeSeconds||0),Number(idleSeconds||0)],['#7c3aed','#06b6d4']);
  }catch(e){
    sel.innerHTML = '<div class="empty">Failed to load worker metrics</div>';
    setError('Failed to load worker metrics: ' + (e.message||e));
    console.error(e);
  }
}

async function selectStation(id){
  const sel = document.getElementById('selectedBox');
  sel.innerHTML = '';
  try{
    const m = await api(`/metrics/workstations/${id}`);
    const s = stations.find(x=>x.workstationId===id) || {name:id};
    const derived = deriveActiveIdleFromMetrics(m);
    const occupancy = (derived.occupancy !== null) ? derived.occupancy
      : (parseNumericFlexible(m.occupancySeconds ?? m.occupancy ?? m.occupancyMs ?? m.occupancy_millis ?? ((m.totalActiveSeconds||0)+(m.totalIdleSeconds||0)) ?? m.observedSeconds ?? m.windowSeconds ?? m.periodSeconds ?? m.durationSeconds ?? m.sampledSeconds) ?? null);
    const active = (derived.active !== null) ? derived.active
      : (parseNumericFlexible(m.totalActiveSeconds ?? m.activeSeconds ?? m.totalActiveMs ?? m.activeMs) ?? 0);
    const idle = (derived.idle !== null) ? derived.idle
      : (parseNumericFlexible(m.totalIdleSeconds ?? m.idleSeconds ?? m.idleMs ?? m.totalIdleMs) ?? Math.max(0,(occupancy||0)-(active||0)));
    const util = (derived.utilPercent !== null) ? derived.utilPercent
      : (parseNumericFlexible(m.utilizationPercent ?? m.utilization_percent ?? m.utilization ?? m.utilPct ?? m.util) ?? null);
    const card = el('div','detail-card');
    const chartCol = el('div','chart-box');
    const canvas = el('canvas'); canvas.className = 'chart-canvas';
    chartCol.appendChild(canvas);
    const statsCol = makeStatsHtml([
      ['Occupancy', fmtTimeReadable(occupancy !== null ? occupancy : null)],
      ['Utilization', fmtPercent(util !== null ? util : null)],
      ['Total units', m.totalUnitsProduced || m.product_count || 0],
      ['Throughput/hr', m.throughputPerHour || m.throughput || 0]
    ]);
    const eventsCol = el('div','events-column');
    try{
      const evs = await api(`/events/workstations/${id}/recent?limit=6`);
      const list = el('div','event-list');
      if(!evs||!evs.length){ list.innerHTML = '<div class="empty">No recent events</div>'; }
      else {
        evs.forEach(ev=>{
          const r = el('div','event-item');
          const ts = new Date(ev.timestamp).toLocaleString();
          const type = ev.eventType;
          const countPart = (ev.count !== undefined && ev.count !== null) ? ` · ${ev.count}` : '';
          const confPart = (ev.confidence !== undefined && ev.confidence !== null) ? ` · conf:${fmtPercent(ev.confidence)}` : '';
          r.textContent = `${ts} · ${type}${countPart}${confPart}`;
          list.appendChild(r);
        });
      }
      eventsCol.appendChild(list);
    }catch(e){
      eventsCol.appendChild(el('div','empty')).textContent='Failed to load recent events';
    }
    card.appendChild(chartCol);
    const rightCol = el('div');
    rightCol.appendChild(statsCol);
    rightCol.appendChild(eventsCol);
    card.appendChild(rightCol);
    sel.appendChild(card);
    safePie(canvas,['Occ','Idle'],[Number(active||0),Number(idle||0)],['#34d399','#f97316']);
  }catch(e){
    sel.innerHTML = '<div class="empty">Failed to load workstation metrics</div>';
    setError('Failed to load workstation metrics: ' + (e.message||e));
    console.error(e);
  }
}

document.getElementById('reseedBtn').addEventListener('click', async ()=>{
  const b = document.getElementById('reseedBtn'); b.disabled = true; setError('');
  try{
    const res = await fetch(API + '/internal/seed', { method:'POST' });
    if(!res.ok){
      let body;
      try { body = await res.json(); } catch(e){ body = await res.text(); }
      const msg = body && body.error ? body.error : (body && body.message ? body.message : (typeof body === 'string' ? body : res.statusText));
      setError('Reseed failed: ' + msg);
    } else { location.reload(); }
  }catch(e){ setError('Reseed failed: ' + (e.message||e)); console.error(e); }
  finally{ b.disabled = false; }
});

async function reloadData(){ workers = []; stations = []; await bootstrap(); }

async function bootstrap(){
  setError('');
  await Promise.all([
    fillFactory(),
    (async()=>{
      if(!workers.length) workers = await api('/workers');
      await fillWorkers();
    })(),
    (async()=>{
      if(!stations.length) stations = await api('/workstations');
      await fillStations();
    })()
  ]);
  document.getElementById('selectedBox').innerHTML = '<div class="empty">Select a worker or workstation to see details here</div>';
  if (factoryRefreshTimer) clearInterval(factoryRefreshTimer);
  factoryRefreshTimer = setInterval(() => {
    fillFactory().catch(e => console.warn('factory refresh failed', e));
  }, 15000);
}

bootstrap();
