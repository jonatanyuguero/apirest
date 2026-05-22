const API_URL = "https://apirest-production-1d70.up.railway.app";;
let authHeader = null;
let usuarioActual = null;
let tagsDisponiblesLista = [];

// Elementos de la interfaz
const loginView = document.getElementById("login-view");
const appView = document.getElementById("app-view");
const formLogin = document.getElementById("form-login");
const formRegister = document.getElementById("form-register");
const formTarea = document.getElementById("form-tarea");
const formCategoria = document.getElementById("form-nueva-categoria");
const formTag = document.getElementById("form-nuevo-tag");
const listaTareas = document.getElementById("lista-tareas");
const toastContainer = document.getElementById("toast-container");

// Filtros de búsqueda
const searchType = document.getElementById("search-type");
const searchQuery = document.getElementById("search-query");
const searchPriority = document.getElementById("search-priority");
const searchCategory = document.getElementById("search-category");
const searchTag = document.getElementById("search-tag");
const searchCompleted = document.getElementById("search-completed");
const btnBuscar = document.getElementById("btn-buscar");

window.addEventListener("DOMContentLoaded", function() {
    inicializarTema();
    document.getElementById("btn-theme-toggle").addEventListener("click", alternarTema);

    formLogin.addEventListener("submit", manejarLogin);
    formRegister.addEventListener("submit", manejarRegistro);
    formTarea.addEventListener("submit", manejarCreacionTarea);
    formCategoria.addEventListener("submit", manejarCreacionCategoria);
    formTag.addEventListener("submit", manejarCreacionTag);
    iniciarCrudCategoriasGestor();
    iniciarEdicionPerfil();

    document.getElementById("btn-logout").addEventListener("click", cerrarSesion);
    document.getElementById("btn-recargar").addEventListener("click", function() {
        searchType.value = "all";
        ajustarInputsBuscador();
        cargarDatosApp();
    });

    document.getElementById("btn-admin").addEventListener("click", mostrarPanelAdmin);
    document.getElementById("btn-gestor").addEventListener("click", mostrarPanelGestor);
    document.getElementById("btn-volver-tareas").addEventListener("click", mostrarPanelTareas);

    document.getElementById("link-to-register").addEventListener("click", function(e) {
        e.preventDefault();
        formLogin.style.display = "none";
        formRegister.style.display = "block";
        document.getElementById("auth-title").textContent = "Crear Cuenta";
    });
    document.getElementById("link-to-login").addEventListener("click", function(e) {
        e.preventDefault();
        formRegister.style.display = "none";
        formLogin.style.display = "block";
        document.getElementById("auth-title").textContent = "Iniciar Sesión";
    });

    searchType.addEventListener("change", ajustarInputsBuscador);
    btnBuscar.addEventListener("click", ejecutarBusquedaFiltrada);
});

// === NOTIFICACIONES Y ERRORES ===
function mostrarNotificacion(mensaje, tipo) {
    const toast = document.createElement("div");
    toast.className = "toast-custom " + tipo;
    toast.textContent = mensaje;
    toastContainer.appendChild(toast);
    setTimeout(function() { toast.remove(); }, 4000);
}

async function gestionarErrorBackend(res, mensajeFallback) {
    try {
        const errorData = await res.json();
        mostrarNotificacion(errorData.detail || errorData.title || mensajeFallback, "error");
    } catch(e) {
        mostrarNotificacion(mensajeFallback, "error");
    }
}

function confirmarAccion(mensaje, callback) {
    const overlay = document.createElement("div");
    overlay.className = "d-flex align-items-center justify-content-center position-fixed top-0 start-0 w-100 h-100";
    overlay.style.backgroundColor = "rgba(0,0,0,0.6)";
    overlay.style.zIndex = "1050";

    const modal = document.createElement("div");
    modal.className = "card shadow-lg p-4 text-center border-0";
    modal.style.minWidth = "300px";
    modal.style.animation = "slideIn 0.2s ease-out";

    const texto = document.createElement("p");
    texto.className = "mb-4 fw-bold text-dark";
    texto.textContent = mensaje;

    const divBotones = document.createElement("div");
    divBotones.className = "d-flex justify-content-center gap-2";

    const btnCancelar = document.createElement("button");
    btnCancelar.className = "btn btn-secondary btn-sm fw-bold px-3";
    btnCancelar.textContent = "Cancelar";
    btnCancelar.addEventListener("click", function() { document.body.removeChild(overlay); });

    const btnConfirmar = document.createElement("button");
    btnConfirmar.className = "btn btn-danger btn-sm fw-bold px-3";
    btnConfirmar.textContent = "Eliminar";
    btnConfirmar.addEventListener("click", function() {
        document.body.removeChild(overlay);
        callback();
    });

    divBotones.appendChild(btnCancelar);
    divBotones.appendChild(btnConfirmar);
    modal.appendChild(texto);
    modal.appendChild(divBotones);
    overlay.appendChild(modal);
    document.body.appendChild(overlay);
}

// === GESTIÓN DE PERFIL ===
function iniciarEdicionPerfil() {
    const btnPerfil = document.getElementById("btn-perfil");
    const formPerfil = document.getElementById("form-perfil");

    btnPerfil.addEventListener("click", () => {
        document.getElementById("perfil-email").value = usuarioActual.email;
        document.getElementById("perfil-fullname").value = usuarioActual.fullname;
        document.getElementById("perfil-password").value = "";
        const modal = new bootstrap.Modal(document.getElementById("modalPerfil"));
        modal.show();
    });

    formPerfil.addEventListener("submit", async (e) => {
        e.preventDefault();
        const cmd = {
            email: document.getElementById("perfil-email").value.trim(),
            fullname: document.getElementById("perfil-fullname").value.trim(),
            password: document.getElementById("perfil-password").value.trim() || null
        };

        try {
            const res = await fetch(API_URL + "/users/me", {
                method: "PUT",
                headers: { "Content-Type": "application/json", "Authorization": authHeader },
                body: JSON.stringify(cmd)
            });
            if (res.ok) {
                usuarioActual = await res.json();
                document.getElementById("user-greeting").textContent = "Hola, " + usuarioActual.fullname;
                mostrarNotificacion("Perfil actualizado correctamente", "success");
                const modalEl = document.getElementById("modalPerfil");
                bootstrap.Modal.getInstance(modalEl).hide();
            } else {
                await gestionarErrorBackend(res, "Error al actualizar perfil");
            }
        } catch (e) { mostrarNotificacion("Fallo de red", "error"); }
    });
}

// === FILTROS DE BÚSQUEDA ===
function ajustarInputsBuscador() {
    const seleccion = searchType.value;

    searchQuery.style.display = "none";
    searchPriority.style.display = "none";
    searchCategory.style.display = "none";
    searchTag.style.display = "none";
    searchCompleted.style.display = "none";
    document.getElementById("search-input-container").style.display = "block";

    if (seleccion === "title" || seleccion === "description") {
        searchQuery.style.display = "block";
        searchQuery.placeholder = "Término a buscar...";
    } else if (seleccion === "priority") {
        searchPriority.style.display = "block";
    } else if (seleccion === "category") {
        searchCategory.style.display = "block";
    } else if (seleccion === "tags") {
        searchTag.style.display = "block";
    } else if (seleccion === "completed") {
        searchCompleted.style.display = "block";
    } else if (seleccion === "all" || seleccion === "overdue") {
        document.getElementById("search-input-container").style.display = "none";
    }
}

async function ejecutarBusquedaFiltrada() {
    const tipo = searchType.value;
    let urlDestino = API_URL + "/tasks";

    if (tipo === "title") urlDestino += "/search/title?q=" + encodeURIComponent(searchQuery.value.trim());
    else if (tipo === "description") urlDestino += "/search/description?q=" + encodeURIComponent(searchQuery.value.trim());
    else if (tipo === "priority") urlDestino += "/search/priority?value=" + searchPriority.value;
    else if (tipo === "completed") urlDestino += "/search/completed?value=" + searchCompleted.value;
    else if (tipo === "overdue") urlDestino += "/search/overdue";
    else if (tipo === "category") {
        if (!searchCategory.value) return mostrarNotificacion("Selecciona una categoría", "error");
        urlDestino += "/search/category/" + searchCategory.value;
    }
    else if (tipo === "tags") {
        if (!searchTag.value) return mostrarNotificacion("Selecciona un tag", "error");
        urlDestino += "/search/tags?ids=" + searchTag.value;
    }

    try {
        const respuesta = await fetch(urlDestino, { headers: { "Authorization": authHeader } });
        listaTareas.innerHTML = "";

        if (respuesta.status === 404) return listaTareas.innerHTML = '<p class="text-muted text-center p-3">No hay tareas con ese filtro.</p>';

        if (respuesta.ok) {
            const tareas = await respuesta.json();
            renderizarListaTareas(tareas);
            mostrarNotificacion("Búsqueda completada (" + tareas.length + " encontradas)", "success");
        } else {
            await gestionarErrorBackend(respuesta, "Error al procesar la búsqueda");
        }
    } catch (e) { mostrarNotificacion("Error al procesar la búsqueda", "error"); }
}

// === AUTENTICACIÓN ===
async function manejarLogin(evento) {
    evento.preventDefault();
    const u = document.getElementById("login-username").value.trim();
    const p = document.getElementById("login-password").value.trim();
    if (!u || !p) return mostrarNotificacion("Rellene los campos obligatorios", "error");

    const cabecera = "Basic " + btoa(u + ":" + p);
    try {
        const res = await fetch(API_URL + "/users/me", { headers: { "Authorization": cabecera } });
        if (res.ok) {
            const data = await res.json();
            authHeader = cabecera;
            usuarioActual = data;
            document.getElementById("user-greeting").textContent = "Hola, " + data.fullname;

            document.getElementById("btn-admin").style.display = (data.role === "ADMIN") ? "inline-block" : "none";
            document.getElementById("btn-gestor").style.display = (data.role === "ADMIN" || data.role === "GESTOR") ? "inline-block" : "none";

            loginView.classList.remove("active-view");
            appView.classList.add("active-view");
            mostrarPanelTareas();
            mostrarNotificacion("Sesión iniciada", "success");
            cargarDatosApp();
        } else {
            await gestionarErrorBackend(res, "Credenciales erróneas");
        }
    } catch (err) { mostrarNotificacion("Servidor inaccesible", "error"); }
}

async function manejarRegistro(evento) {
    evento.preventDefault();
    const cmd = {
        username: document.getElementById("reg-username").value.trim(),
        email: document.getElementById("reg-email").value.trim(),
        fullname: document.getElementById("reg-fullname").value.trim(),
        password: document.getElementById("reg-password").value.trim()
    };
    try {
        const res = await fetch(API_URL + "/auth/register", {
            method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(cmd)
        });
        if (res.ok) {
            mostrarNotificacion("Registro completado, ya puedes entrar", "success");
            formRegister.reset();
            formRegister.style.display = "none";
            formLogin.style.display = "block";
        } else { await gestionarErrorBackend(res, "Fallo al registrar"); }
    } catch (e) { mostrarNotificacion("Error de red", "error"); }
}

function cerrarSesion() {
    authHeader = null;
    usuarioActual = null;
    tagsDisponiblesLista = [];
    document.getElementById("btn-admin").style.display = "none";
    document.getElementById("btn-gestor").style.display = "none";
    document.getElementById("btn-volver-tareas").style.display = "none";
    appView.classList.remove("active-view");
    loginView.classList.add("active-view");
}

// === NAVEGACIÓN ===
function mostrarPanelTareas() {
    document.getElementById("tasks-panel").style.display = "block";
    document.getElementById("admin-panel").style.display = "none";
    document.getElementById("gestor-panel").style.display = "none";
    document.getElementById("btn-volver-tareas").style.display = "none";
    if (usuarioActual && usuarioActual.role === "ADMIN") document.getElementById("btn-admin").style.display = "inline-block";
    if (usuarioActual && (usuarioActual.role === "ADMIN" || usuarioActual.role === "GESTOR")) document.getElementById("btn-gestor").style.display = "inline-block";
}
function mostrarPanelAdmin() {
    document.getElementById("tasks-panel").style.display = "none";
    document.getElementById("admin-panel").style.display = "block";
    document.getElementById("gestor-panel").style.display = "none";
    document.getElementById("btn-admin").style.display = "none";
    document.getElementById("btn-gestor").style.display = "none";
    document.getElementById("btn-volver-tareas").style.display = "inline-block";
    cargarUsuariosAdmin();
}
function mostrarPanelGestor() {
    document.getElementById("tasks-panel").style.display = "none";
    document.getElementById("admin-panel").style.display = "none";
    document.getElementById("gestor-panel").style.display = "block";
    document.getElementById("btn-admin").style.display = "none";
    document.getElementById("btn-gestor").style.display = "none";
    document.getElementById("btn-volver-tareas").style.display = "inline-block";
    cargarCategoriasGestor();
    cargarDashboardGlobal();
    cargarTareasGlobales();
}

// === VISTAS SUPERVISORAS ===
async function cargarDashboardGlobal() {
    try {
        const res = await fetch(API_URL + "/tasks/dashboard/global", { headers: { "Authorization": authHeader } });
        if (res.ok) {
            const stats = await res.json();
            document.getElementById("gstat-total").textContent = stats.total;
            document.getElementById("gstat-completed").textContent = stats.completed;
            document.getElementById("gstat-pending").textContent = stats.pending;
            document.getElementById("gstat-overdue").textContent = stats.overdue;
        }
    } catch (e) {}
}

async function cargarTareasGlobales() {
    const tbody = document.getElementById("tabla-tareas-globales");
    tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">Cargando...</td></tr>';
    try {
        const res = await fetch(API_URL + "/tasks/all", { headers: { "Authorization": authHeader } });
        if (!res.ok) return tbody.innerHTML = '<tr><td colspan="7" class="text-center text-danger">Sin permisos.</td></tr>';
        const tareas = await res.json();
        tbody.innerHTML = "";
        if (tareas.length === 0) return tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">No hay tareas.</td></tr>';
        const ahora = new Date();
        tareas.forEach(t => {
            const tr = document.createElement("tr");
            const vencida = !t.completed && t.deadline && new Date(t.deadline) < ahora;
            const estadoCls = t.completed ? "background:#d1fae5;color:#065f46" : (vencida ? "background:#fee2e2;color:#991b1b" : "background:#fef3c7;color:#92400e");
            const txtEst = t.completed ? "Completada" : (vencida ? "Vencida" : "Pendiente");

            tr.innerHTML = `
                <td>${t.id}</td><td class="fw-bold">${t.title}</td><td class="small">${t.author ? t.author.username : "—"}</td>
                <td class="small text-muted">${t.category ? t.category.title : "—"}</td>
                <td><span class="badge-custom badge ${t.priority}">${t.priority}</span></td>
                <td><span class="role-badge" style="${estadoCls}">${txtEst}</span></td>
                <td class="small">${t.deadline ? t.deadline.replace("T", " ").substring(0, 16) : "—"}</td>`;
            tbody.appendChild(tr);
        });
    } catch (e) { tbody.innerHTML = '<tr><td colspan="7" class="text-center text-danger">Error de conexión.</td></tr>'; }
}

async function cargarUsuariosAdmin() {
    const tbody = document.getElementById("tabla-usuarios");
    tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">Cargando...</td></tr>';
    try {
        const res = await fetch(API_URL + "/users", { headers: { "Authorization": authHeader } });
        if (!res.ok) return tbody.innerHTML = '<tr><td colspan="6" class="text-center text-danger">Sin permisos.</td></tr>';
        const usuarios = await res.json();
        tbody.innerHTML = "";
        usuarios.forEach(u => {
            const tr = document.createElement("tr");
            const tdAcc = document.createElement("td");
            tdAcc.className = "text-end";

            if (u.id === usuarioActual.id) {
                tdAcc.innerHTML = '<span class="text-muted small fst-italic">(Tú)</span>';
            } else {
                if (u.role === "USER") {
                    const btnP = document.createElement("button"); btnP.className = "btn btn-warning btn-sm me-1"; btnP.textContent = "Promover";
                    btnP.onclick = () => promoverUsuario(u.id); tdAcc.appendChild(btnP);
                } else if (u.role === "GESTOR") {
                    const btnD = document.createElement("button"); btnD.className = "btn btn-secondary btn-sm me-1"; btnD.textContent = "Degradar";
                    btnD.onclick = () => degradarUsuario(u.id); tdAcc.appendChild(btnD);
                }
                if (u.role !== "ADMIN") {
                    const btnDel = document.createElement("button"); btnDel.className = "btn btn-danger btn-sm"; btnDel.textContent = "Eliminar";
                    btnDel.onclick = () => eliminarUsuario(u.id, u.username); tdAcc.appendChild(btnDel);
                }
            }
            tr.innerHTML = `<td>${u.id}</td><td class="fw-bold">${u.username}</td><td>${u.fullname}</td><td class="small text-muted">${u.email}</td><td><span class="role-badge role-${u.role}">${u.role}</span></td>`;
            tr.appendChild(tdAcc);
            tbody.appendChild(tr);
        });
    } catch (e) {}
}

async function promoverUsuario(id) {
    try {
        const res = await fetch(API_URL + "/users/" + id + "/promote", { method: "PUT", headers: { "Authorization": authHeader } });
        if(res.ok) { mostrarNotificacion("Promocionado a GESTOR", "success"); cargarUsuariosAdmin(); } else await gestionarErrorBackend(res, "Fallo promover");
    } catch (e) {}
}
async function degradarUsuario(id) {
    try {
        const res = await fetch(API_URL + "/users/" + id + "/demote", { method: "PUT", headers: { "Authorization": authHeader } });
        if(res.ok) { mostrarNotificacion("Degradado a USER", "success"); cargarUsuariosAdmin(); } else await gestionarErrorBackend(res, "Fallo degradar");
    } catch (e) {}
}
function eliminarUsuario(id, username) {
    confirmarAccion(`¿Eliminar al usuario "${username}"?`, async () => {
        try {
            const res = await fetch(API_URL + "/users/" + id, { method: "DELETE", headers: { "Authorization": authHeader } });
            if(res.ok || res.status===204) { mostrarNotificacion("Usuario eliminado", "success"); cargarUsuariosAdmin(); } else await gestionarErrorBackend(res, "Error al eliminar");
        } catch (e) {}
    });
}

// === ACCIONES DE CARGA GENERAL ===
async function cargarDatosApp() {
    await cargarCategorias();
    await cargarTags();
    await cargarDashboard();
    try {
        const respuesta = await fetch(API_URL + "/tasks", { headers: { "Authorization": authHeader } });
        listaTareas.innerHTML = "";
        if (respuesta.ok) renderizarListaTareas(await respuesta.json());
    } catch (e) {}
}

async function cargarDashboard() {
    try {
        const res = await fetch(API_URL + "/tasks/dashboard", { headers: { "Authorization": authHeader } });
        if (res.ok) {
            const stats = await res.json();
            document.getElementById("stat-total").textContent = stats.total;
            document.getElementById("stat-completed").textContent = stats.completed;
            document.getElementById("stat-pending").textContent = stats.pending;
            document.getElementById("stat-overdue").textContent = stats.overdue;
        }
    } catch (e) {}
}

async function cargarCategorias() {
    try {
        const res = await fetch(API_URL + "/categories", { headers: { "Authorization": authHeader } });
        if (res.ok) {
            const lista = await res.json();
            const selectForm = document.getElementById("categoria");
            selectForm.innerHTML = '<option value="">Sin categoría</option>';
            searchCategory.innerHTML = '';
            lista.forEach(cat => {
                selectForm.innerHTML += `<option value="${cat.id}">${cat.title}</option>`;
                searchCategory.innerHTML += `<option value="${cat.id}">${cat.title}</option>`;
            });
        }
    } catch (e) {}
}

async function cargarTags() {
    try {
        const res = await fetch(API_URL + "/tags", { headers: { "Authorization": authHeader } });
        if (res.ok) {
            tagsDisponiblesLista = await res.json();
            const contenedor = document.getElementById("tags-container");
            contenedor.innerHTML = "";
            searchTag.innerHTML = "";

            tagsDisponiblesLista.forEach(tag => {
                const badge = document.createElement("span");
                badge.className = "badge d-inline-flex align-items-center";
                badge.style.background = "#e0e7ff"; badge.style.color = "#4338ca";
                badge.textContent = "#" + tag.name;

                const btnBorrar = document.createElement("button");
                btnBorrar.className = "btn-close ms-2"; btnBorrar.style.fontSize = "0.5rem";
                btnBorrar.onclick = () => eliminarTag(tag.id);
                badge.appendChild(btnBorrar);
                contenedor.appendChild(badge);

                searchTag.innerHTML += `<option value="${tag.id}">#${tag.name}</option>`;
            });
        }
    } catch (e) {}
}

function eliminarTag(id) {
    confirmarAccion("¿Borrar este tag permanentemente?", async () => {
        try {
            const res = await fetch(API_URL + "/tags/" + id, { method: "DELETE", headers: { "Authorization": authHeader } });
            if (res.ok || res.status === 204) { mostrarNotificacion("Tag eliminado", "success"); cargarDatosApp(); }
            else await gestionarErrorBackend(res, "Error al eliminar tag");
        } catch (e) {}
    });
}

// === ENTIDADES Y SUBRECURSOS ===
async function manejarCreacionCategoria(evento) {
    evento.preventDefault();
    const tituloInput = document.getElementById("nueva-cat-titulo");
    if (!tituloInput.value.trim()) return;
    try {
        const res = await fetch(API_URL + "/categories", {
            method: "POST", headers: { "Content-Type": "application/json", "Authorization": authHeader },
            body: JSON.stringify({ title: tituloInput.value.trim() })
        });
        if (res.ok) { mostrarNotificacion("Categoría añadida con éxito", "success"); tituloInput.value = ""; cargarCategorias(); }
        else await gestionarErrorBackend(res, "Fallo al crear categoría");
    } catch (e) {}
}

async function manejarCreacionTag(evento) {
    evento.preventDefault();
    const tagInput = document.getElementById("nuevo-tag-nombre");
    if (!tagInput.value.trim()) return;
    try {
        const res = await fetch(API_URL + "/tags", {
            method: "POST", headers: { "Content-Type": "application/json", "Authorization": authHeader },
            body: JSON.stringify({ name: tagInput.value.trim() })
        });
        if (res.ok) { mostrarNotificacion("Tag creado", "success"); tagInput.value = ""; cargarDatosApp(); }
        else await gestionarErrorBackend(res, "Fallo al crear tag");
    } catch (e) {}
}

async function manejarCreacionTarea(evento) {
    evento.preventDefault();
    const inputTitulo = document.getElementById("titulo");
    if (!inputTitulo.value.trim()) {
        inputTitulo.classList.add("input-error");
        document.getElementById("error-titulo").style.display = "block";
        return inputTitulo.focus();
    }
    inputTitulo.classList.remove("input-error");
    document.getElementById("error-titulo").style.display = "none";

    const nuevaTarea = {
        title: inputTitulo.value.trim(),
        description: document.getElementById("descripcion").value.trim() || null,
        deadline: document.getElementById("fecha-limite").value || null,
        completed: false,
        priority: document.getElementById("prioridad").value,
        categoryId: document.getElementById("categoria").value ? parseInt(document.getElementById("categoria").value) : null
    };

    try {
        const respuesta = await fetch(API_URL + "/tasks", {
            method: "POST", headers: { "Content-Type": "application/json", "Authorization": authHeader },
            body: JSON.stringify(nuevaTarea)
        });
        if (respuesta.ok) { mostrarNotificacion("Tarea guardada correctamente", "success"); formTarea.reset(); cargarDatosApp(); }
        else await gestionarErrorBackend(respuesta, "Fallo al crear tarea");
    } catch (e) {}
}

// === PROCESAMIENTO DE LISTAS ===
function renderizarListaTareas(tareas) {
    tareas.forEach(tarea => {
        const li = document.createElement("li");
        li.className = "task-item";
        if (tarea.completed) li.classList.add("completed");

        const divContent = document.createElement("div");
        divContent.className = "task-content";
        divContent.innerHTML = `
            <div class="task-title">${tarea.title}</div>
            <div class="task-desc">${tarea.description || "Sin descripción"}</div>
            <div class="task-meta">
                <span class="badge-custom badge ${tarea.priority}">Prio: ${tarea.priority}</span>
                ${tarea.category ? `<span class="badge-custom badge-cat">Cat: ${tarea.category.title}</span>` : ""}
            </div>
        `;

        const divTags = document.createElement("div");
        divTags.className = "mt-2 d-flex flex-wrap gap-1 align-items-center";

        tarea.tags.forEach(tag => {
            const tBadge = document.createElement("span");
            tBadge.className = "badge-custom badge-tag d-flex align-items-center";
            tBadge.textContent = "#" + tag.name;

            const btnRemove = document.createElement("button");
            btnRemove.className = "btn-close ms-1"; btnRemove.style.fontSize = "0.5rem";
            btnRemove.title = "Quitar tag";
            btnRemove.onclick = () => quitarTagDeTarea(tarea.id, tag.id);

            tBadge.appendChild(btnRemove);
            divTags.appendChild(tBadge);
        });

        const selectTag = document.createElement("select");
        selectTag.className = "form-select form-select-sm d-inline-block w-auto ms-2";
        selectTag.style.fontSize = "0.75rem"; selectTag.style.padding = "0.1rem 1.5rem 0.1rem 0.5rem";
        selectTag.innerHTML = '<option value="">+ Tag</option>';
        tagsDisponiblesLista.forEach(tg => {
            if (!tarea.tags.some(t => t.id === tg.id)) selectTag.innerHTML += `<option value="${tg.id}">${tg.name}</option>`;
        });
        selectTag.onchange = (e) => {
            if(e.target.value) asignarTagATarea(tarea.id, e.target.value);
        };

        divTags.appendChild(selectTag);
        divContent.appendChild(divTags);

        const divAcciones = document.createElement("div");
        divAcciones.className = "task-actions";
        const btnToggle = document.createElement("button");
        btnToggle.className = tarea.completed ? "btn btn-secondary btn-sm" : "btn btn-success btn-sm";
        btnToggle.textContent = tarea.completed ? "Deshacer" : "Completar";
        btnToggle.onclick = () => cambiarEstadoTarea(tarea.id, !tarea.completed);

        const btnEliminar = document.createElement("button");
        btnEliminar.className = "btn btn-danger btn-sm"; btnEliminar.textContent = "Eliminar";
        btnEliminar.onclick = () => eliminarTarea(tarea.id);

        divAcciones.appendChild(btnToggle);
        divAcciones.appendChild(btnEliminar);
        li.appendChild(divContent);
        li.appendChild(divAcciones);
        listaTareas.appendChild(li);
    });
}

// === ASOCIACIÓN DE TAGS ===
async function asignarTagATarea(taskId, tagId) {
    try {
        const res = await fetch(`${API_URL}/tasks/${taskId}/tags/${tagId}`, { method: "POST", headers: { "Authorization": authHeader } });
        if (res.ok) { mostrarNotificacion("Tag asignado correctamente", "success"); cargarDatosApp(); }
        else await gestionarErrorBackend(res, "No se pudo asignar el tag");
    } catch (e) {}
}

async function quitarTagDeTarea(taskId, tagId) {
    try {
        const res = await fetch(`${API_URL}/tasks/${taskId}/tags/${tagId}`, { method: "DELETE", headers: { "Authorization": authHeader } });
        if (res.ok) { mostrarNotificacion("Tag eliminado de la tarea", "success"); cargarDatosApp(); }
        else await gestionarErrorBackend(res, "No se pudo quitar el tag");
    } catch (e) {}
}

function eliminarTarea(id) {
    confirmarAccion("¿Deseas borrar esta tarea de forma permanente?", async () => {
        try {
            const respuesta = await fetch(API_URL + "/tasks/" + id, { method: "DELETE", headers: { "Authorization": authHeader } });
            if (respuesta.ok || respuesta.status === 204) { mostrarNotificacion("Tarea eliminada", "success"); cargarDatosApp(); }
        } catch (e) {}
    });
}

async function cambiarEstadoTarea(id, completadoNuevovalor) {
    try {
        await fetch(API_URL + "/tasks/" + id, {
            method: "PUT", headers: { "Content-Type": "application/json", "Authorization": authHeader },
            body: JSON.stringify({ completed: completadoNuevovalor })
        });
        cargarDatosApp();
    } catch (e) {}
}

// === CONFIGURACIÓN DE TEMA ===
function inicializarTema() {
    let tema = "light";
    try { tema = localStorage.getItem("todolist-theme") || "light"; } catch (e) {}
    aplicarTema(tema);
}
function alternarTema() {
    aplicarTema(document.documentElement.getAttribute("data-bs-theme") === "dark" ? "light" : "dark");
}
function aplicarTema(tema) {
    document.documentElement.setAttribute("data-bs-theme", tema);
    document.body.classList.remove("bg-light", "bg-dark");
    document.body.classList.add(tema === "dark" ? "bg-dark" : "bg-light");
    const btn = document.getElementById("btn-theme-toggle");
    if (btn) btn.textContent = tema === "dark" ? "Modo claro" : "Modo oscuro";
    try { localStorage.setItem("todolist-theme", tema); } catch (e) {}
}

// === CRUD CATEGORÍAS GESTOR ===
function iniciarCrudCategoriasGestor() {
    document.getElementById("form-cat-gestor").addEventListener("submit", manejarGuardarCategoria);
    document.getElementById("btn-cat-gestor-cancelar").addEventListener("click", resetFormCatGestor);
}
function resetFormCatGestor() {
    document.getElementById("cat-gestor-id").value = "";
    document.getElementById("cat-gestor-titulo").value = "";
    document.getElementById("btn-cat-gestor-guardar").textContent = "Guardar";
    document.getElementById("btn-cat-gestor-cancelar").style.display = "none";
}
async function cargarCategoriasGestor() {
    const tbody = document.getElementById("tabla-categorias-gestor");
    tbody.innerHTML = '<tr><td colspan="3" class="text-center text-muted">Cargando...</td></tr>';
    try {
        const res = await fetch(API_URL + "/categories", { headers: { "Authorization": authHeader } });
        if (!res.ok) return tbody.innerHTML = '<tr><td colspan="3" class="text-center text-danger">Error al cargar categorías.</td></tr>';
        const categorias = await res.json();
        tbody.innerHTML = "";
        categorias.forEach(cat => {
            const tr = document.createElement("tr");
            const tdAcc = document.createElement("td");
            tdAcc.className = "text-end";
            const btnEditar = document.createElement("button"); btnEditar.className = "btn btn-warning btn-sm me-1"; btnEditar.textContent = "Editar";
            btnEditar.onclick = () => {
                document.getElementById("cat-gestor-id").value = cat.id;
                document.getElementById("cat-gestor-titulo").value = cat.title;
                document.getElementById("btn-cat-gestor-guardar").textContent = "Actualizar";
                document.getElementById("btn-cat-gestor-cancelar").style.display = "inline-block";
            };
            const btnEliminar = document.createElement("button"); btnEliminar.className = "btn btn-danger btn-sm"; btnEliminar.textContent = "Eliminar";
            btnEliminar.onclick = () => eliminarCategoriaGestor(cat.id, cat.title);
            tdAcc.appendChild(btnEditar);
            tdAcc.appendChild(btnEliminar);
            tr.innerHTML = `<td>${cat.id}</td><td class="fw-bold">${cat.title}</td>`;
            tr.appendChild(tdAcc);
            tbody.appendChild(tr);
        });
    } catch (e) {}
}
async function manejarGuardarCategoria(evento) {
    evento.preventDefault();
    const id = document.getElementById("cat-gestor-id").value;
    const titulo = document.getElementById("cat-gestor-titulo").value.trim();
    if (!titulo) return mostrarNotificacion("El nombre de la categoría no puede estar vacío", "error");
    const esEdicion = id !== "";
    try {
        const res = await fetch(esEdicion ? API_URL + "/categories/" + id : API_URL + "/categories", {
            method: esEdicion ? "PUT" : "POST",
            headers: { "Content-Type": "application/json", "Authorization": authHeader },
            body: JSON.stringify({ title: titulo })
        });
        if (res.ok) {
            mostrarNotificacion(esEdicion ? "Categoría actualizada" : "Categoría creada con éxito", "success");
            resetFormCatGestor();
            cargarCategoriasGestor();
            cargarCategorias();
        } else {
            await gestionarErrorBackend(res, "Error al guardar la categoría");
        }
    } catch (e) {}
}
function eliminarCategoriaGestor(id, titulo) {
    confirmarAccion(`¿Eliminar la categoría "${titulo}"?`, async () => {
        try {
            const res = await fetch(API_URL + "/categories/" + id, { method: "DELETE", headers: { "Authorization": authHeader } });
            if (res.ok || res.status === 204) { mostrarNotificacion("Categoría registrada como eliminada", "success"); cargarCategoriasGestor(); cargarCategorias(); }
            else await gestionarErrorBackend(res, "Error al eliminar la categoría");
        } catch (e) {}
    });
}