package com.licencias.presentacion;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web")
public class MenuController {

    @GetMapping
    public String mostrarInicio(Model model) {
        return "inicio"; // ✅ Carga la vista principal de bienvenida
    }
}

