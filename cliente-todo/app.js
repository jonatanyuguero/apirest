const API_URL = "http://localhost:8080";
let authHeader = null;

// Cache de elementos estructurales
const loginView = document.getElementById("login-view");
const appView = document.getElementById("app-view");
const formLogin = document.getElementById("form-login");
const formRegister = document.getElementById("form-register");
const formTarea = document.getElementById("form-tarea");
const formCategoria = document.getElementById("form-nueva-categoria");
const formTag = document.getElementById("form-nuevo-tag");
const listaTareas = document.getElementById("lista-tareas");
const toastContainer = document.getElementById("toast-container");

// Controladores del Buscador Avanzado
const searchType = document.getElementById("search-type");
const searchQuery = document.getElementById("search-query");
const searchPriority = document.getElementById("search-priority");
const searchCategory = document.getElementById("search-category");
const searchCompleted = document.getElementById("search-completed");
const btnBuscar = document.getElementById("btn-buscar");

window.addEventListener("DOMContentLoaded", function() {
    formLogin.addEventListener("submit", manejarLogin);
    formRegister.addEventListener("submit", manejarRegistro);
    formTarea.addEventListener("submit", manejarCreacionTarea);
    formCategoria.addEventListener("submit", manejarCreacionCategoria);
    formTag.addEventListener("submit", manejarCreacionTag);

    document.getElementById("btn-logout").addEventListener("click", cerrarSesion);
    document.getElementById("btn-recargar").addEventListener("click", function() {
        searchType.value = "all";
        ajustarInputsBuscador();
        cargarDatosApp();
    });

    // Enlaces alternar Auth
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

    // Evento cambiador del buscador dinámico
    searchType.addEventListener("change", ajustarInputsBuscador);
    btnBuscar.addEventListener("click", ejecutarBusquedaFiltrada);
});

function mostrarNotificacion(mensaje, tipo) {
    const toast = document.createElement("div");
    toast.className = "toast " + tipo;
    toast.textContent = mensaje;
    toastContainer.appendChild(toast);
    setTimeout(function() { toast.remove(); }, 4000);
}

// === INTERRUPTOR DINÁMICO DEL BUSCADOR ===
function ajustarInputsBuscador() {
    const seleccion = searchType.value;

    // Ocultar todos por defecto
    searchQuery.style.display = "none";
    searchPriority.style.display = "none";
    searchCategory.style.display = "none";
    searchCompleted.style.display = "none";
    document.getElementById("search-input-container").style.display = "block";

    if (seleccion === "title" || seleccion === "description") {
        searchQuery.style.display = "block";
        searchQuery.placeholder = "Término a buscar...";
    } else if (seleccion === "priority") {
        searchPriority.style.display = "block";
    } else if (seleccion === "category") {
        searchCategory.style.display = "block";
    } else if (seleccion === "completed") {
        searchCompleted.style.display = "block";
    } else if (seleccion === "all" || seleccion === "overdue") {
        document.getElementById("search-input-container").style.display = "none";
    }
}

// === MOTOR DE FILTRADO (CONEXIÓN CON TUS ENDPOINTS DE SWAGGER) ===
async function ejecutarBusquedaFiltrada() {
    const tipo = searchType.value;
    let urlDestino = API_URL + "/tasks";

    if (tipo === "title") {
        urlDestino += "/search/title?q=" + encodeURIComponent(searchQuery.value.trim());
    } else if (tipo === "description") {
        urlDestino += "/search/description?q=" + encodeURIComponent(searchQuery.value.trim());
    } else if (tipo === "priority") {
        urlDestino += "/search/priority?value=" + searchPriority.value;
    } else if (tipo === "completed") {
        urlDestino += "/search/completed?value=" + searchCompleted.value;
    } else if (tipo === "overdue") {
        urlDestino += "/search/overdue";
    } else if (tipo === "category") {
        if (!searchCategory.value) {
            mostrarNotificacion("Selecciona una categoría válida", "error");
            return;
        }
        urlDestino += "/search/category/" + searchCategory.value;
    }

    try {
        const respuesta = await fetch(urlDestino, {
            headers: { "Authorization": authHeader }
        });
        listaTareas.innerHTML = "";

        if (respuesta.status === 404) {
            listaTareas.innerHTML = '<p class="text-muted" style="text-align:center; padding:20px;">No se encontraron tareas con ese filtro.</p>';
            return;
        }

        if (respuesta.ok) {
            const tareas = await respuesta.json();
            renderizarListaTareas(tareas);
            mostrarNotificacion("Búsqueda completada (" + tareas.length + " encontradas)", "success");
        }
    } catch (e) {
        console.error(e);
        mostrarNotificacion("Error al procesar la búsqueda", "error");
    }
}

// === CONTROL AUTENTICACIÓN ===
async function manejarLogin(evento) {
    evento.preventDefault();
    const u = document.getElementById("login-username").value.trim();
    const p = document.getElementById("login-password").value.trim();

    if (!u || !p) {
        mostrarNotificacion("Rellene los campos obligatorios", "error");
        return;
    }

    const cabecera = "Basic " + btoa(u + ":" + p);
    try {
        const res = await fetch(API_URL + "/users/me", { headers: { "Authorization": cabecera } });
        if (res.ok) {
            const data = await res.json();
            authHeader = cabecera;
            document.getElementById("user-greeting").textContent = "Hola, " + data.fullname;
            loginView.classList.remove("active-view");
            appView.classList.add("active-view");
            mostrarNotificacion("Sesión iniciada", "success");
            cargarDatosApp();
        } else {
            mostrarNotificacion("Credenciales erróneas", "error");
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
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(cmd)
        });
        if (res.ok) {
            mostrarNotificacion("Registro completado, ya puedes entrar", "success");
            formRegister.reset();
            formRegister.style.display = "none";
            formLogin.style.display = "block";
        } else { mostrarNotificacion("El usuario o correo ya existen", "error"); }
    } catch (e) { mostrarNotificacion("Error de red", "error"); }
}

function cerrarSesion() {
    authHeader = null;
    appView.classList.remove("active-view");
    loginView.classList.add("active-view");
}

// === ACCIONES DE CARGA GENERAL ===
async function cargarDatosApp() {
    await cargarCategorias();
    await cargarDashboard();
    await cargarTags();

    // Carga inicial mapea todas las tareas sin filtrar
    try {
        const respuesta = await fetch(API_URL + "/tasks", { headers: { "Authorization": authHeader } });
        listaTareas.innerHTML = "";
        if (respuesta.ok) {
            const tareas = await respuesta.json();
            renderizarListaTareas(tareas);
        }
    } catch (e) { console.error(e); }
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

            lista.forEach(function(cat) {
                const op1 = document.createElement("option");
                op1.value = cat.id; op1.textContent = cat.title;
                selectForm.appendChild(op1);

                const op2 = document.createElement("option");
                op2.value = cat.id; op2.textContent = cat.title;
                searchCategory.appendChild(op2);
            });
        }
    } catch (e) {}
}

async function cargarTags() {
    try {
        const res = await fetch(API_URL + "/tags", { headers: { "Authorization": authHeader } });
        if (res.ok) {
            const tags = await res.json();
            const contenedor = document.getElementById("tags-container");
            contenedor.innerHTML = "";
            tags.forEach(function(tag) {
                const badge = document.createElement("span");
                badge.className = "badge";
                badge.style.background = "#e0e7ff";
                badge.style.color = "#4338ca";
                badge.textContent = "#" + tag.name;
                contenedor.appendChild(badge);
            });
        }
    } catch (e) {}
}

// === CREACIÓN DE NUEVOS SUBRECURSOS ===
async function manejarCreacionCategoria(evento) {
    evento.preventDefault();
    const tituloInput = document.getElementById("nueva-cat-titulo");
    const valor = tituloInput.value.trim();

    if (!valor) return;

    try {
        const res = await fetch(API_URL + "/categories", {
            method: "POST",
            headers: { "Content-Type": "application/json", "Authorization": authHeader },
            body: JSON.stringify({ title: valor })
        });
        if (res.ok) {
            mostrarNotificacion("Categoría añadida con éxito", "success");
            tituloInput.value = "";
            cargarCategorias();
        } else {
            mostrarNotificacion("Error: Solo ADMIN/GESTOR pueden crear categorías", "error");
        }
    } catch (e) { mostrarNotificacion("Error al guardar categoría", "error"); }
}

async function manejarCreacionTag(evento) {
    evento.preventDefault();
    const tagInput = document.getElementById("nuevo-tag-nombre");
    const valor = tagInput.value.trim();

    if (!valor) return;

    try {
        const res = await fetch(API_URL + "/tags", {
            method: "POST",
            headers: { "Content-Type": "application/json", "Authorization": authHeader },
            body: JSON.stringify({ name: valor })
        });
        if (res.ok) {
            mostrarNotificacion("Tag creado", "success");
            tagInput.value = "";
            cargarTags();
        }
    } catch (e) {}
}

// === AGREGAR TAREAS CON VALIDACIÓN DE EXCEPCIÓN INTEGRADA ===
async function manejarCreacionTarea(evento) {
    evento.preventDefault();
    const inputTitulo = document.getElementById("titulo");
    const errorTitulo = document.getElementById("error-titulo");

    const tituloVal = inputTitulo.value.trim();

    // Lanzamiento de excepción visual en cliente
    if (tituloVal === "") {
        inputTitulo.classList.add("input-error");
        errorTitulo.style.display = "block";
        inputTitulo.focus();
        return;
    } else {
        inputTitulo.classList.remove("input-error");
        errorTitulo.style.display = "none";
    }

    const nuevaTarea = {
        title: tituloVal,
        description: document.getElementById("descripcion").value.trim() || null,
        deadline: document.getElementById("fecha-limite").value || null,
        completed: false,
        priority: document.getElementById("prioridad").value,
        categoryId: document.getElementById("categoria").value ? parseInt(document.getElementById("categoria").value) : null
    };

    try {
        const respuesta = await fetch(API_URL + "/tasks", {
            method: "POST",
            headers: { "Content-Type": "application/json", "Authorization": authHeader },
            body: JSON.stringify(nuevaTarea)
        });
        if (respuesta.ok) {
            mostrarNotificacion("Tarea guardada correctamente", "success");
            formTarea.reset();
            cargarDatosApp();
        }
    } catch (e) { mostrarNotificacion("Fallo de red al guardar", "error"); }
}

// === RENDERIZACIÓN DINÁMICA DE LA LISTA ===
function renderizarListaTareas(tareas) {
    tareas.forEach(function(tarea) {
        const li = document.createElement("li");
        li.className = "task-item";
        if (tarea.completed) li.classList.add("completed");

        const divContent = document.createElement("div");
        divContent.className = "task-content";

        const h4 = document.createElement("div");
        h4.className = "task-title";
        h4.textContent = tarea.title;

        const pDesc = document.createElement("div");
        pDesc.className = "task-desc";
        pDesc.textContent = tarea.description ? tarea.description : "Sin descripción";

        const divMeta = document.createElement("div");
        divMeta.className = "task-meta";

        const spanPrio = document.createElement("span");
        spanPrio.className = "badge " + tarea.priority;
        spanPrio.textContent = "Prio: " + tarea.priority;
        divMeta.appendChild(spanPrio);

        if (tarea.category) {
            const spanCat = document.createElement("span");
            spanCat.className = "badge";
            spanCat.textContent = "Cat: " + tarea.category.title;
            divMeta.appendChild(spanCat);
        }

        divContent.appendChild(h4);
        divContent.appendChild(pDesc);
        divContent.appendChild(divMeta);

        const divAcciones = document.createElement("div");
        divAcciones.className = "task-actions";

        const btnToggle = document.createElement("button");
        btnToggle.className = tarea.completed ? "btn btn-secondary" : "btn btn-success";
        btnToggle.textContent = tarea.completed ? "Deshacer" : "Completar";
        btnToggle.addEventListener("click", function() {
            cambiarEstadoTarea(tarea.id, !tarea.completed);
        });

        const btnEliminar = document.createElement("button");
        btnEliminar.className = "btn btn-danger";
        btnEliminar.textContent = "Eliminar";
        btnEliminar.addEventListener("click", function() {
            eliminarTarea(tarea.id);
        });

        divAcciones.appendChild(btnToggle);
        divAcciones.appendChild(btnEliminar);

        li.appendChild(divContent);
        li.appendChild(divAcciones);
        listaTareas.appendChild(li);
    });
}

async function eliminarTarea(id) {
    if (!confirm("¿Deseas borrar esta tarea de forma permanente?")) return;
    try {
        const respuesta = await fetch(API_URL + "/tasks/" + id, { method: "DELETE", headers: { "Authorization": authHeader } });
        if (respuesta.ok || respuesta.status === 204) {
            mostrarNotificacion("Tarea eliminada", "success");
            cargarDatosApp();
        }
    } catch (e) {}
}

async function cambiarEstadoTarea(id, completadoNuevovalor) {
    try {
        await fetch(API_URL + "/tasks/" + id, {
            method: "PUT",
            headers: { "Content-Type": "application/json", "Authorization": authHeader },
            body: JSON.stringify({ completed: completadoNuevovalor })
        });
        cargarDatosApp();
    } catch (e) {}
}