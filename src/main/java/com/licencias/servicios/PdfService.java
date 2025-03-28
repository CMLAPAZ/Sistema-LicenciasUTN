package com.licencias.servicios;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.licencias.entidades.Licencias;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class PdfService {
    
    public byte[] generarLicenciaPDF(Licencias licencia) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font fontTitulo = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font fontNormal = new Font(Font.HELVETICA, 12, Font.NORMAL);

            document.add(new Paragraph("📄 Licencia Aprobada", fontTitulo));
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("📌 Datos del Empleado", fontNormal));
            document.add(new Paragraph("Nombre: " + licencia.getEmpleado().getNombreCompleto(), fontNormal));
            document.add(new Paragraph("Legajo: " + licencia.getEmpleado().getLegajo(), fontNormal));
            document.add(new Paragraph("DNI: " + licencia.getEmpleado().getDni(), fontNormal));
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("📆 Detalles de la Licencia", fontNormal));
            document.add(new Paragraph("Fecha de Inicio: " + licencia.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), fontNormal));
            document.add(new Paragraph("Días Solicitados: " + licencia.getDiasSolicitados(), fontNormal));
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("✅ Estado: APROBADA", fontNormal));

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF de la licencia.", e);
        }

        return outputStream.toByteArray();
    }
}
