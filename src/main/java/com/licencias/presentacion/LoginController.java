package com.licencias.presentacion;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/web/login")
    public String mostrarLogin() {
        return "login"; // 📌 Esto cargará el archivo "login.html" de /resources/templates/
    }
}
