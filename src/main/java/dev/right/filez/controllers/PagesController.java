package dev.right.filez.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PagesController {
    @GetMapping("/login")
    public String redirectLogin() {
        return "login";
    }

    @GetMapping("/app/menu")
    public String redirectMenu() {
        return "menu";
    }
}
