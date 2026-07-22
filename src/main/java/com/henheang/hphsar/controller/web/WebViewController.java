package com.henheang.hphsar.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web")
public class WebViewController {

    @GetMapping({"/", "/login"})
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @GetMapping("/suppliers/dashboard")
    public String supplierDashboard() {
        return "supplier/dashboard";
    }

    @GetMapping("/suppliers/products")
    public String supplierProducts() {
        return "supplier/products";
    }

    @GetMapping("/suppliers/orders")
    public String supplierOrders() {
        return "supplier/orders";
    }

    @GetMapping("/suppliers/categories")
    public String supplierCategories() {
        return "supplier/categories";
    }

    @GetMapping("/suppliers/store")
    public String supplierStore() {
        return "supplier/store";
    }

    @GetMapping("/suppliers/profile")
    public String supplierProfile() {
        return "supplier/profile";
    }

    // ─── Buyer ───────────────────────────────────────────────────────────────

    @GetMapping("/buyers/dashboard")
    public String buyerDashboard() { return "buyer/dashboard"; }

    @GetMapping("/buyers/stores")
    public String buyerStores() { return "buyer/stores"; }

    @GetMapping("/buyers/cart")
    public String buyerCart() { return "buyer/cart"; }

    @GetMapping("/buyers/orders")
    public String buyerOrders() { return "buyer/orders"; }

    @GetMapping("/buyers/history")
    public String buyerHistory() { return "buyer/history"; }

    @GetMapping("/buyers/profile")
    public String buyerProfile() { return "buyer/profile"; }
}