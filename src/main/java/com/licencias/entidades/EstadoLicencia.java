package com.licencias.entidades;

/**
 * 📌 Representa los estados posibles de una licencia.
 */
public enum EstadoLicencia {
    PENDIENTE,   // Licencia solicitada pero aún no aprobada
    APROBADA,    // Licencia aprobada por un autorizador
    RECHAZADA,   // Licencia rechazada por un autorizador
    CANCELADA    // Licencia cancelada antes de su inicio
}
