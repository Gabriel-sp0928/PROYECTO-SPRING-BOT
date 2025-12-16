// Central page scripts: sales, saleForm, auth (login/register), and small utilities
document.addEventListener('DOMContentLoaded', ()=>{
  // LOGIN
  // The classic server form login should submit to /login and create a session.
  // Only use the API (JWT) login when the form explicitly has `data-api="true"`.
  const loginForm = document.getElementById('loginForm');
  if(loginForm && loginForm.getAttribute('data-api') === 'true'){
    loginForm.addEventListener('submit', async (e)=>{
      e.preventDefault();
      const username = document.getElementById('username').value;
      const password = document.getElementById('password').value;
      try{
        const res = await fetch('/api/auth/login',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({username,password})});
        if(res.ok){ const data = await res.json(); localStorage.setItem('token', data.token); window.location.href='/dashboard'; }
        else { const text = await res.text(); alert('Credenciales inválidas: '+text); }
      }catch(err){ alert('Error al iniciar sesión'); }
    });
  }

  // REGISTER
  const registerForm = document.getElementById('registerForm');
  if(registerForm){
    registerForm.addEventListener('submit', async (e)=>{
      e.preventDefault();
      const username = document.getElementById('username')?.value;
      const email = document.getElementById('email')?.value;
      const name = document.getElementById('name')?.value;
      const password = document.getElementById('password')?.value;
      // read role from input[name=role] (hidden field) to be robust
      const roleEl = registerForm.querySelector('input[name="role"]');
      const role = roleEl ? roleEl.value : 'CLIENT';
      try{
        const response = await fetch('/api/auth/register',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({username,email,password,name,role})});
        if(response.ok){ alert('Registro exitoso'); window.location.href='/login'; } else { const err = await response.text(); alert('Error: '+err); }
      }catch(e){ alert('Error al registrarse'); }
    });
  }

  // SALES page rendering and modal actions
  const salesTable = document.getElementById('salesTable');
  const saleModal = document.getElementById('saleModal');
  const saleForm = document.getElementById('saleForm');
  if(salesTable){
    async function loadSales(){
      try{
        const list = await apiFetch('/api/sales');
        salesTable.innerHTML = '';
        (list||[]).forEach(s=>{
          const tr = document.createElement('tr'); tr.className='border-t border-white/4';
          const date = s.date? new Date(s.date).toLocaleString():'';
          const client = s.quote && s.quote.user ? s.quote.user.name : '';
          tr.innerHTML = `<td class="py-3">${s.id}</td><td class="py-3">${date}</td><td class="py-3">${client}</td><td class="py-3">${s.total}</td>`;
          const tdActions = document.createElement('td'); tdActions.className='py-3 text-right';
          // view button
          const btnView = document.createElement('button'); btnView.type='button'; btnView.className='btn-outline-light mr-2'; btnView.title='Ver'; btnView.innerHTML='<i class="fa fa-eye"></i>';
          btnView.addEventListener('click', ()=>{ window.location.href = '/sales/'+s.id+'/edit'; });
          tdActions.appendChild(btnView);
          // edit button
          const btnEdit = document.createElement('button'); btnEdit.type='button'; btnEdit.className='btn-outline-light mr-2 open-sale-btn'; btnEdit.setAttribute('data-id', s.id); btnEdit.innerHTML = '<i class="fa fa-edit"></i>';
          btnEdit.addEventListener('click', ()=> openSale(s.id));
          tdActions.appendChild(btnEdit);
          // delete
          const btnDel = document.createElement('button'); btnDel.type='button'; btnDel.className='btn-outline-light text-red-400'; btnDel.innerHTML='<i class="fa fa-trash"></i>';
          btnDel.addEventListener('click', async ()=>{ const ok = await window.confirmModal('Eliminar consulta #'+s.id+'?'); if(!ok) return; try{ await apiFetch('/api/sales/'+s.id,{method:'DELETE'}); showToast('Consulta eliminada'); await loadSales(); }catch(e){ console.error(e); } });
          tdActions.appendChild(btnDel);

          tr.appendChild(tdActions);
          salesTable.appendChild(tr);
        });
      }catch(e){ console.error(e); }
    }

    window.openSale = async function(id){
      try{
        const s = await apiFetch('/api/sales/'+id);
        document.getElementById('s_id').value = s.id || '';
        // ensure quotes select is populated and select the current one
        try{
          let quotes = await apiFetch('/api/quotes/my');
          if(!quotes) quotes = await apiFetch('/api/quotes');
          const sel = document.getElementById('s_quoteId'); sel.innerHTML = ''; const placeholder = document.createElement('option'); placeholder.value=''; placeholder.textContent='-- Seleccionar cotización --'; sel.appendChild(placeholder);
          (quotes||[]).forEach(q => { const opt = document.createElement('option'); opt.value = q.id; opt.textContent = `#${q.id} - ${q.date? new Date(q.date).toLocaleString():''} - ${q.status}`; sel.appendChild(opt); });
        }catch(e){ console.error(e); }
        document.getElementById('s_quoteId').value = s.quote? s.quote.id : '';
        // set date to sale date or today
        document.getElementById('s_date').value = s.date? new Date(s.date).toISOString().slice(0,16) : new Date().toISOString().slice(0,16);
        document.getElementById('s_total').value = s.total || '';
        document.getElementById('saleModalTitle').textContent = 'Editar Consulta';
        // show products for the quote
        try{ if(s.quote && s.quote.id){ const q = await apiFetch('/api/quotes/'+s.quote.id); renderQuoteProductsInModal(q); } }catch(e){ console.warn(e); }
        saleModal.classList.remove('hidden');
      }catch(e){ console.error(e); }
    }

    window.delSale = async function(id){ const ok = await window.confirmModal('Eliminar consulta #'+id+'?'); if(!ok) return; try{ await apiFetch('/api/sales/'+id,{method:'DELETE'}); showToast('Consulta eliminada'); await loadSales(); }catch(e){ console.error(e); } }

    const newSaleBtn = document.getElementById('newSaleBtn');
    const cancelSale = document.getElementById('cancelSale');
    if(newSaleBtn){ newSaleBtn.addEventListener('click', async ()=>{
      document.getElementById('saleModalTitle').textContent='Nueva Consulta';
      // populate quotes list for this user (client) to pick
      try{
        let quotes = await apiFetch('/api/quotes/my');
        if(!quotes) quotes = await apiFetch('/api/quotes');
        const sel = document.getElementById('s_quoteId');
        sel.innerHTML = '';
        const placeholder = document.createElement('option'); placeholder.value=''; placeholder.textContent='-- Seleccionar cotización --'; sel.appendChild(placeholder);
        (quotes||[]).forEach(q => { const opt = document.createElement('option'); opt.value = q.id; opt.textContent = `#${q.id} - ${q.date? new Date(q.date).toLocaleString():''} - ${q.status}`; sel.appendChild(opt); });
      }catch(e){ console.error(e); }
      // default date to now
      document.getElementById('s_date').value = new Date().toISOString().slice(0,16);
      document.getElementById('s_total').value = '';
      document.getElementById('s_products').innerHTML = '';
      saleModal.classList.remove('hidden');
    }); }
    if(cancelSale){ cancelSale.addEventListener('click', ()=>{ saleModal.classList.add('hidden'); saleForm.reset(); document.getElementById('s_id').value=''; }); }

    if(saleForm){
      saleForm.addEventListener('submit', async (e)=>{
        e.preventDefault();
        const id = document.getElementById('s_id').value;
        const quoteIdVal = document.getElementById('s_quoteId').value;
        const payload = { total: parseFloat(document.getElementById('s_total').value||0), date: document.getElementById('s_date').value ? new Date(document.getElementById('s_date').value).toISOString() : null, quote: quoteIdVal ? { id: Number(quoteIdVal) } : null };
        try{
          if(id) await apiFetch('/api/sales/'+id,{method:'PUT',headers:{'Content-Type':'application/json'},body:JSON.stringify(payload)});
          else await apiFetch('/api/sales',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(payload)});
          showToast('Guardado'); saleModal.classList.add('hidden'); await loadSales();
        }catch(e){ console.error(e); showToast('Error al guardar'); }
      });
    }

    // delegate clicks on server-rendered edit buttons
    document.addEventListener('click', function(evt){
      const btn = evt.target.closest('.open-sale-btn');
      if(btn){ const id = btn.getAttribute('data-id'); if(id) openSale(id); }
    });

    // when selecting a quote in the modal, fetch quote details and compute total and product list
    const sQuoteSel = document.getElementById('s_quoteId');
    if(sQuoteSel){
      sQuoteSel.addEventListener('change', async function(){
        const qid = this.value;
        if(!qid){ document.getElementById('s_products').innerHTML=''; document.getElementById('s_total').value=''; return; }
        try{
          const q = await apiFetch('/api/quotes/'+qid);
          renderQuoteProductsInModal(q);
        }catch(e){ console.error(e); }
      });
    }

    function renderQuoteProductsInModal(q){
      const container = document.getElementById('s_products');
      if(!container) return;
      if(!q || !q.details || !q.details.length) { container.innerHTML = '<div class="text-sm text-[#9aa4ad]">Sin productos</div>'; document.getElementById('s_total').value = q.total || 0; return; }
      const list = document.createElement('div'); list.className='space-y-1';
      let computed = 0;
      q.details.forEach(d => { const name = d.product ? d.product.name : 'Producto'; const qty = d.quantity || 0; const price = d.price || 0; const sub = qty * price; computed += sub; const row = document.createElement('div'); row.className='flex justify-between text-sm text-[#9aa4ad]'; row.innerHTML = `<div>${name} x${qty}</div><div>${sub.toFixed(2)}</div>`; list.appendChild(row); });
      container.innerHTML = '';
      container.appendChild(list);
      // set total to computed value (allow manual override)
      document.getElementById('s_total').value = computed.toFixed(2);
    }

    // initial load
    loadSales();
  }

  // SALE FORM (standalone page)
  const saleFormStandalone = document.getElementById('saleFormStandalone');
  if(saleFormStandalone){
    saleFormStandalone.addEventListener('submit', async function(e){
      e.preventDefault();
      const id = document.getElementById('id')?.value;
      const payload = { quote: { id: Number(document.getElementById('quoteId').value) }, total: Number(document.getElementById('total').value) };
      try{
        if(id) await fetch('/api/sales/' + id, { method: 'PUT', headers: {'Content-Type':'application/json'}, body: JSON.stringify(payload) });
        else await fetch('/api/sales', { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify(payload) });
        window.location.href = '/sales';
      }catch(e){ console.error(e); }
    });
  }
});
