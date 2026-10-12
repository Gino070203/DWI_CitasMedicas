(() => {
    "use strict";

    function fechaLocal(iso) {
        if (!iso || !/^\d{4}-\d{2}-\d{2}$/.test(iso)) {
            return null;
        }
        const [anio, mes, dia] = iso.split("-").map(Number);
        const fecha = new Date(anio, mes - 1, dia);
        if (fecha.getFullYear() !== anio
                || fecha.getMonth() !== mes - 1
                || fecha.getDate() !== dia) {
            return null;
        }
        fecha.setHours(0, 0, 0, 0);
        return fecha;
    }

    function fechaIso(fecha) {
        const anio = fecha.getFullYear();
        const mes = String(fecha.getMonth() + 1).padStart(2, "0");
        const dia = String(fecha.getDate()).padStart(2, "0");
        return `${anio}-${mes}-${dia}`;
    }

    function mostrarError(formulario, mensaje) {
        const error = formulario.querySelector("[data-error-calendario]");
        if (error) {
            error.textContent = mensaje;
            error.hidden = false;
        }
        const boton = formulario.querySelector("[data-boton-reserva]");
        if (boton) {
            boton.disabled = true;
        }
    }

    function iniciarCalendarios() {
        const campos = document.querySelectorAll("[data-calendario-disponibilidad]");
        if (!campos.length) {
            return;
        }

        if (typeof window.flatpickr !== "function"
                || !window.flatpickr.l10ns
                || !window.flatpickr.l10ns.es) {
            campos.forEach((campo) => {
                const formulario = campo.closest("form");
                if (formulario) {
                    mostrarError(formulario,
                        "No se pudo cargar el calendario. Recarga la página para volver a intentarlo.");
                }
            });
            return;
        }

        campos.forEach((campo) => {
            const formulario = campo.closest("form");
            if (!formulario) {
                return;
            }

            const boton = formulario.querySelector("[data-boton-reserva]");
            const fechaMinima = fechaLocal(campo.dataset.fechaMinima);
            const fechaMaxima = fechaLocal(campo.dataset.fechaMaxima);
            const fechasDisponibles = new Set(
                (campo.dataset.fechasDisponibles || "")
                    .split(",")
                    .map((fecha) => fecha.trim())
                    .filter(Boolean)
            );
            const contenedorTurnos = formulario.querySelector("[data-turnos-container]");
            const opcionesTurnos = formulario.querySelector("[data-turnos-opciones]");
            const indicadorCarga = formulario.querySelector("[data-turnos-cargando]");
            const mensajeVacio = formulario.querySelector("[data-turnos-vacios]");
            let solicitudActual = 0;

            if (!fechaMinima || !fechaMaxima || fechaMaxima < fechaMinima
                    || !campo.dataset.idHorario || !campo.dataset.turnosUrl) {
                mostrarError(formulario,
                    "No se pudo determinar la disponibilidad de este bloque.");
                return;
            }

            const etiqueta = Array.from(formulario.querySelectorAll("label"))
                .find((label) => label.htmlFor === campo.id);

            window.flatpickr(campo, {
                locale: window.flatpickr.l10ns.es,
                dateFormat: "Y-m-d",
                altInput: true,
                altFormat: "d/m/Y",
                minDate: campo.dataset.fechaMinima,
                maxDate: campo.dataset.fechaMaxima,
                disableMobile: true,
                allowInput: false,
                enable: [function (fecha) {
                    return fechasDisponibles.has(fechaIso(fecha));
                }],
                onReady: function (_fechas, _fechaTexto, instancia) {
                    campo.required = false;
                    if (instancia.altInput) {
                        instancia.altInput.id = `${campo.id}-visible`;
                        instancia.altInput.required = true;
                        instancia.altInput.setAttribute("aria-label",
                            etiqueta ? etiqueta.textContent.trim() : "Fecha de la cita");
                        if (etiqueta) {
                            etiqueta.htmlFor = instancia.altInput.id;
                        }
                    }
                    if (boton) {
                        boton.disabled = true;
                    }
                },
                onChange: function (fechas, _fechaTexto, instancia) {
                    const fechaSeleccionada = fechas[0];
                    const disponible = fechaSeleccionada
                        && fechaSeleccionada >= fechaMinima
                        && fechaSeleccionada <= fechaMaxima
                        && fechasDisponibles.has(
                            instancia.formatDate(fechaSeleccionada, "Y-m-d"));
                    if (boton) {
                        boton.disabled = true;
                    }
                    if (!contenedorTurnos || !opcionesTurnos || !disponible) {
                        if (contenedorTurnos) {
                            contenedorTurnos.hidden = true;
                        }
                        return;
                    }

                    const errorCalendario = formulario.querySelector("[data-error-calendario]");
                    if (errorCalendario) {
                        errorCalendario.hidden = true;
                    }
                    const numeroSolicitud = ++solicitudActual;
                    contenedorTurnos.hidden = false;
                    opcionesTurnos.replaceChildren();
                    if (mensajeVacio) {
                        mensajeVacio.hidden = true;
                    }
                    if (indicadorCarga) {
                        indicadorCarga.hidden = false;
                    }

                    const url = new URL(campo.dataset.turnosUrl, window.location.href);
                    url.searchParams.set("idHorario", campo.dataset.idHorario);
                    url.searchParams.set("fecha",
                        instancia.formatDate(fechaSeleccionada, "Y-m-d"));

                    fetch(url, {
                        method: "GET",
                        credentials: "same-origin",
                        headers: { "Accept": "application/json" }
                    }).then((respuesta) => {
                        if (!respuesta.ok) {
                            throw new Error("No se pudo consultar la disponibilidad.");
                        }
                        return respuesta.json();
                    }).then((turnos) => {
                        if (numeroSolicitud !== solicitudActual) {
                            return;
                        }
                        if (indicadorCarga) {
                            indicadorCarga.hidden = true;
                        }
                        if (!Array.isArray(turnos) || turnos.length === 0) {
                            if (mensajeVacio) {
                                mensajeVacio.hidden = false;
                            }
                            return;
                        }

                        turnos.forEach((turno, indice) => {
                            const idOpcion = `turno-${campo.dataset.idHorario}-${indice}`;
                            const etiquetaTurno = document.createElement("label");
                            etiquetaTurno.className = "patient-slot-option";
                            etiquetaTurno.htmlFor = idOpcion;

                            const radio = document.createElement("input");
                            radio.type = "radio";
                            radio.id = idOpcion;
                            radio.name = "horaInicio";
                            radio.value = turno.horaInicio;
                            radio.required = true;
                            radio.addEventListener("change", () => {
                                opcionesTurnos.querySelectorAll(".patient-slot-option")
                                    .forEach((opcion) => opcion.classList.remove("is-selected"));
                                etiquetaTurno.classList.add("is-selected");
                                if (boton) {
                                    boton.disabled = false;
                                }
                            });

                            const texto = document.createElement("span");
                            texto.textContent = `${turno.horaInicio} – ${turno.horaFin}`;
                            etiquetaTurno.append(radio, texto);
                            opcionesTurnos.append(etiquetaTurno);
                        });
                    }).catch(() => {
                        if (numeroSolicitud === solicitudActual) {
                            if (indicadorCarga) {
                                indicadorCarga.hidden = true;
                            }
                            mostrarError(formulario,
                                "No se pudieron consultar los turnos. Vuelve a seleccionar la fecha.");
                        }
                    });
                },
                onDayCreate: function (_dObj, _dStr, instancia, diaCelda) {
                    const fecha = diaCelda.dateObj;
                    const fechaTexto = instancia.formatDate(fecha, "Y-m-d");
                    let estado;

                    if (fecha < fechaMinima) {
                        diaCelda.classList.add("availability-past");
                        estado = "Fecha pasada";
                    } else if (fecha > fechaMaxima) {
                        diaCelda.classList.add("availability-out-of-range");
                        estado = "Fuera del período de reserva";
                    } else if (fechasDisponibles.has(fechaTexto)) {
                        diaCelda.classList.add("availability-free");
                        estado = "Hay al menos un turno libre";
                    } else {
                        diaCelda.classList.add("availability-unavailable");
                        estado = "No atiende o todos los turnos están ocupados";
                    }

                    const etiquetaBase = diaCelda.getAttribute("aria-label")
                        || diaCelda.textContent.trim();
                    diaCelda.setAttribute("aria-label", `${etiquetaBase}. ${estado}`);
                    diaCelda.title = estado;
                }
            });
        });
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", iniciarCalendarios, { once: true });
    } else {
        iniciarCalendarios();
    }
})();
