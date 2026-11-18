(() => {
    const form=document.getElementById('personal-data-form');
    const summary=document.getElementById('personal-data-summary');
    const edit=document.getElementById('edit-personal-data');
    if(!form || !summary || !edit)return;
    function show(editing){form.hidden=!editing;summary.hidden=editing;edit.setAttribute('aria-expanded',String(editing));}
    edit.addEventListener('click',()=>{show(true);form.querySelector('select,input:not([type="hidden"])')?.focus();});
    document.getElementById('cancel-personal-data').addEventListener('click',()=>{
        form.reset();
        ['sex','birthDate','height','weight'].forEach(name=>{
            const field=form.querySelector('[name="'+name+'"]');
            const saved=form.dataset['saved'+name[0].toUpperCase()+name.slice(1)];
            if(field && saved!=null)field.value=saved;
        });
        show(false);edit.focus();
    });
})();
