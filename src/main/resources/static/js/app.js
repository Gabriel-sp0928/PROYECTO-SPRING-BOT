document.addEventListener('DOMContentLoaded', ()=>{
  // Hide loader
  const loader = document.getElementById('pageLoader'); if(loader) loader.classList.add('d-none');

  // Theme toggle (Tailwind dark class) with persistence
  const themeBtn = document.getElementById('themeToggle');
  const bodyEl = document.body;
  function setTheme(dark){
    if(dark){ bodyEl.classList.add('dark'); if(themeBtn) themeBtn.innerHTML = '<i class="fa fa-sun"></i>'; }
    else { bodyEl.classList.remove('dark'); if(themeBtn) themeBtn.innerHTML = '<i class="fa fa-moon"></i>'; }
    try{ localStorage.setItem('prefersDark', dark ? '1' : '0'); }catch(e){}
  }
  // initialize from preference or current body class
  try{
    const pref = localStorage.getItem('prefersDark');
    if(pref !== null) setTheme(pref === '1'); else setTheme(bodyEl.classList.contains('dark'));
  }catch(e){ setTheme(bodyEl.classList.contains('dark')); }
  if(themeBtn){
    themeBtn.addEventListener('click', ()=>{ setTheme(!document.body.classList.contains('dark')); });
  }
  // Sidebar toggle for mobile
  const sidebarToggle = document.getElementById('sidebarToggle');
  const sidebar = document.getElementById('appSidebar') || document.querySelector('.sidebar');
  if(sidebarToggle && sidebar){
    sidebarToggle.addEventListener('click', ()=>{
      sidebar.classList.toggle('hidden');
    });
  }
  // Close sidebar on outside click (mobile)
  document.addEventListener('click', (e)=>{
    if(window.innerWidth < 1024 && sidebar && !e.target.closest('.sidebar') && !e.target.closest('#sidebarToggle')){
      sidebar.classList.add('hidden');
    }
  });

  // User menu toggle
  const userBtn = document.getElementById('userMenuBtn');
  const userMenu = document.getElementById('userMenu');
  if(userBtn && userMenu){
    userBtn.addEventListener('click', (e)=>{ e.stopPropagation(); userMenu.classList.toggle('hidden'); });
    document.addEventListener('click', ()=> userMenu.classList.add('hidden'));
  }
});

// Toast helper
function showToast(message, opts={type:'info', delay:3500}){
  const container = document.getElementById('toastContainer');
  if(!container) return;
  const wrap = document.createElement('div');
  wrap.className = 'toast-wrapper';
  const toast = document.createElement('div');
  toast.className = 'toast-custom text-sm text-white px-4 py-2 rounded-md shadow-lg flex items-center gap-3';
  toast.setAttribute('role','alert');
  toast.setAttribute('aria-live','polite');
  toast.innerHTML = `<div class='flex-1'>${message}</div><button type='button' class='toast-close px-2 py-1 rounded text-white/80 hover:text-white' aria-label='Close'>✕</button>`;
  wrap.appendChild(toast);
  container.appendChild(wrap);
  // close on button
  wrap.querySelector('.toast-close').addEventListener('click', ()=> wrap.remove());
  // auto remove
  setTimeout(()=>{ try{ wrap.remove() }catch(e){} }, opts.delay);
}

// Dashboard charts renderer (called from layout initializer)
window.renderDashboardCharts = async function(products, users, quotes, sales){
  try{
    // Sales line chart (monthly totals)
    const salesCanvas = document.getElementById('salesChart');
    if(salesCanvas && Array.isArray(sales)){
      // aggregate by month (YYYY-MM)
      const map = new Map();
      sales.forEach(s=>{
        const d = s.date ? new Date(s.date) : null;
        const key = d ? d.getFullYear() + '-' + String(d.getMonth()+1).padStart(2,'0') : 'unknown';
        const val = parseFloat(s.total) || 0;
        map.set(key, (map.get(key)||0) + val);
      });
      // pick last 6 months
      const labels = [];
      const data = [];
      const now = new Date();
      for(let i=5;i>=0;i--){
        const d = new Date(now.getFullYear(), now.getMonth()-i, 1);
        const key = d.getFullYear() + '-' + String(d.getMonth()+1).padStart(2,'0');
        labels.push(d.toLocaleString(undefined, {month:'short', year:'numeric'}));
        data.push(parseFloat((map.get(key)||0).toFixed(2)));
      }
      // destroy existing chart if any
      if(window._salesChartInstance) window._salesChartInstance.destroy();
      window._salesChartInstance = new Chart(salesCanvas.getContext('2d'), {
        type: 'line',
        data: { labels, datasets: [{ label: 'Ventas', data, borderColor: '#14b8a6', backgroundColor: 'rgba(20,184,166,0.12)', tension:0.3, fill:true }] },
        options: { responsive:true, plugins:{legend:{display:false}} }
      });
    }

    // Summary doughnut chart (counts)
    const summaryCanvas = document.getElementById('summaryChart');
    if(summaryCanvas){
      const counts = [ (products && products.length) || 0, (users && users.length) || 0, (quotes && quotes.length) || 0, (sales && sales.length) || 0 ];
      if(window._summaryChartInstance) window._summaryChartInstance.destroy();
      window._summaryChartInstance = new Chart(summaryCanvas.getContext('2d'), {
        type: 'doughnut',
        data: { labels:['Productos','Usuarios','Cotizaciones','Ventas'], datasets:[{ data: counts, backgroundColor:['#7c3aed','#0284c7','#f59e0b','#10b981'] }] },
        options: { responsive:true, plugins:{legend:{position:'bottom'}} }
      });
    }
  }catch(e){ console.error('Chart init failed', e); }
}

// Simple fetch wrapper with toast on error
async function apiFetch(url, options={}){
  try{
    const opts = Object.assign({headers:{'Accept':'application/json'}}, options);
    const res = await fetch(url, opts);
    if(!res.ok){ const text = await res.text(); showToast('Error: '+res.status+' '+text, {type:'error'}); throw new Error(text); }
    // handle no-content responses
    if(res.status === 204) return null;
    const ct = res.headers.get('content-type') || '';
    if(!ct) return null;
    if(ct.indexOf('application/json') !== -1) return await res.json();
    return await res.text();
  }catch(e){ showToast(e.message || 'Error de red', {type:'error'}); throw e }
}

// Simple form validation helper
function applyFormValidation(form){
  form.addEventListener('submit', (e)=>{
    if(!form.checkValidity()){ e.preventDefault(); e.stopPropagation(); form.classList.add('was-validated'); showToast('Por favor corrige los errores del formulario'); }
  });
}

// confirmModal: shows a reusable confirmation modal and returns a Promise<boolean>
window.confirmModal = function(message, opts={title:'Confirmar', confirmText:'Sí', cancelText:'No'}){
  return new Promise((resolve)=>{
    const container = document.getElementById('modalContainer');
    if(!container){ resolve(confirm(message)); return; }
    const wrapper = document.createElement('div');
    wrapper.className = 'fixed inset-0 z-50 flex items-center justify-center bg-black/50';
    wrapper.innerHTML = `
      <div class="bg-[#071018] rounded-md p-4 w-full max-w-md glass">
        <h3 class="text-lg font-semibold mb-2">${opts.title}</h3>
        <p class="mb-4">${message}</p>
        <div class="flex justify-end gap-3">
          <button type="button" class="btn-cancel px-3 py-2 rounded-md">${opts.cancelText}</button>
          <button type="button" class="btn-confirm bg-gradient-to-r from-rose-400 to-red-600 text-black px-3 py-2 rounded-md">${opts.confirmText}</button>
        </div>
      </div>
    `;
    container.appendChild(wrapper);
    const cleanup = ()=>{ try{ wrapper.remove(); }catch(e){} };
    wrapper.querySelector('.btn-cancel').addEventListener('click', ()=>{ cleanup(); resolve(false); });
    wrapper.querySelector('.btn-confirm').addEventListener('click', ()=>{ cleanup(); resolve(true); });
    // close on backdrop click
    wrapper.addEventListener('click', (e)=>{ if(e.target === wrapper){ cleanup(); resolve(false); } });
  });
}

// Attach confirm handlers to forms with .confirm-delete
document.addEventListener('DOMContentLoaded', ()=>{
  document.querySelectorAll('form.confirm-delete').forEach(f => {
    f.addEventListener('submit', async function(e){
      e.preventDefault();
      const msg = f.getAttribute('data-message') || '¿Confirmar acción?';
      const ok = await window.confirmModal(msg);
      if(ok) f.submit();
    });
  });
});
