package com.licencias.entidades;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "saldo_licencia")
public class SaldoLicencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSaldo;

    @ManyToOne
    @JoinColumn(name = "id_empleado", referencedColumnName = "idEmpleado", nullable = false)
    private Empleados empleado;

    @Column(nullable = false)
    private int anio; // Año de la licencia

    @Column(nullable = false)
    private int diasTotales; // Días asignados en ese año

    @Column(nullable = false)
    private int diasUsados = 0; // Días ya utilizados

    @Column(nullable = false)
    private int diasRestantes; // Días aún disponibles

    // Constructor por defecto (necesario para JPA)
    public SaldoLicencia() {}

    // Constructor parametrizado para crear objetos fácilmente
    public SaldoLicencia(Empleados empleado, int anio, int diasTotales) {
        this.empleado = empleado;
        this.anio = anio;
        this.diasTotales = diasTotales;
        this.diasRestantes = diasTotales; // Al inicio, todos los días están disponibles
        this.diasUsados = 0; // No ha usado ningún día aún
    }

    // Getters y Setters
    public Long getIdSaldo() { return idSaldo; }
    public void setIdSaldo(Long idSaldo) { this.idSaldo = idSaldo; }

    public Empleados getEmpleado() { return empleado; }
    public void setEmpleado(Empleados empleado) { this.empleado = empleado; }

    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }

    public int getDiasTotales() { return diasTotales; }
    public void setDiasTotales(int diasTotales) { 
        this.diasTotales = diasTotales;
        this.diasRestantes = diasTotales; // Se inicializa con los mismos días
    }

    public int getDiasUsados() { return diasUsados; }
    public void setDiasUsados(int diasUsados) { this.diasUsados = diasUsados; }

    public int getDiasRestantes() { return diasRestantes; }
    public void setDiasRestantes(int diasRestantes) { this.diasRestantes = diasRestantes; }

    // Verifica si el saldo total alcanza antes de descontar
    public static boolean tieneSaldoTotalSuficiente(List<SaldoLicencia> saldos, int diasSolicitados) {
        int saldoTotal = saldos.stream().mapToInt(SaldoLicencia::getDiasRestantes).sum();
        return saldoTotal >= diasSolicitados;
    }

    // Descontar días (devuelve los días NO descontados)
    public int descontarDias(int diasPorDescontar) {
        if (diasPorDescontar <= diasRestantes) {
            this.diasRestantes -= diasPorDescontar;
            this.diasUsados += diasPorDescontar;
        } else {
            this.diasUsados += diasRestantes;
            this.diasRestantes = 0;
        }
        return this.diasRestantes;
    }

    // Verificar si el saldo está agotado (diasRestantes == 0)
    public boolean estaAgotado() {
        return this.diasRestantes == 0;
    }
}

