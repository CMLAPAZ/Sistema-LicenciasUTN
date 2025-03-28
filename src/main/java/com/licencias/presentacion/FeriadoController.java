package com.licencias.presentacion;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.licencias.entidades.Feriado;
import com.licencias.repositorios.FeriadoRepository;

@Controller
@RequestMapping("/web/feriados")
public class FeriadoController {

    @Autowired
    private FeriadoRepository feriadoRepository;

    /**
     * 📌 Listar todos los feriados.
     */
    @GetMapping
    public String listarFeriados(Model model) {
        if (feriadoRepository == null) {
            throw new IllegalStateException("⚠️ feriadoRepository es NULL. No se ha inyectado correctamente.");
        }

        List<Feriado> feriados = feriadoRepository.findAll();
        model.addAttribute("feriados", feriados);
        return "feriados";
    }

    /**
     * 📌 Formulario para agregar un nuevo feriado.
     */
    @GetMapping("/nuevo")
    public String formularioNuevoFeriado(Model model) {
        model.addAttribute("feriado", new Feriado());
        return "nuevo-feriado"; // 📌 Asegúrate de que nuevo-feriado.html exista en templates
    }

    /**
     * 📌 Guardar un feriado en la base de datos.
     */
    @PostMapping("/guardar")
    public String guardarFeriado(@ModelAttribute Feriado feriado, Model model) {
        // ✅ Validar que la fecha no sea nula
        if (feriado.getFecha() == null) {
            model.addAttribute("error", "⚠️ La fecha del feriado no puede estar vacía.");
            return "nuevo-feriado";
        }

        // ✅ Verificar si la fecha ya existe en la BD
        if (feriadoRepository.existsByFecha(feriado.getFecha())) {
            model.addAttribute("error", "⚠️ Este feriado ya está registrado.");
            return "nuevo-feriado";
        }

        // ✅ Guardar el nuevo feriado
        feriadoRepository.save(feriado);
        return "redirect:/web/feriados";
    }

    /**
     * 📌 Eliminar un feriado.
     */
    @PostMapping("/eliminar/{id}")
    public String eliminarFeriado(@PathVariable Long id, Model model) {
        // ✅ Verificar si el feriado existe antes de eliminarlo
        Optional<Feriado> feriadoOpt = feriadoRepository.findById(id);
        if (feriadoOpt.isPresent()) {
            feriadoRepository.deleteById(id);
            return "redirect:/web/feriados";
        } else {
            model.addAttribute("error", "❌ No se encontró el feriado.");
            return "feriados"; // Volver a la lista con el error
        }
    }
}
