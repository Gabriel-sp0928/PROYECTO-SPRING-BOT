package com.industriagafra.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;

import com.industriagafra.service.IProductService;
import com.industriagafra.entity.Product;
import com.industriagafra.service.IUserService;
import com.industriagafra.service.IQuoteService;
import com.industriagafra.service.ISaleService;
import com.industriagafra.repository.ProductRepository;
import com.industriagafra.repository.UserRepository;
import com.industriagafra.repository.QuoteRepository;
import com.industriagafra.repository.SaleRepository;

@Controller
public class WebController {
    @Autowired
    private IProductService productService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IQuoteService quoteService;

    @Autowired
    private ISaleService saleService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private com.industriagafra.repository.QuoteDetailRepository quoteDetailRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private com.industriagafra.repository.SaleDetailRepository saleDetailRepository;

    @Autowired
    private com.industriagafra.service.DashboardService dashboardService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("productsCount", productRepository.count());
        model.addAttribute("usersCount", userRepository.count());
        model.addAttribute("quotesCount", quoteRepository.count());
        model.addAttribute("salesCount", saleRepository.count());

        var data = dashboardService.getDashboardData();
        model.addAttribute("monthlySales", data.getOrDefault("monthlySales", java.math.BigDecimal.ZERO));
        model.addAttribute("approvedQuotes", data.getOrDefault("approvedQuotes", 0L));
        model.addAttribute("rejectedQuotes", data.getOrDefault("rejectedQuotes", 0L));
        model.addAttribute("lowStockProducts", data.getOrDefault("lowStockProducts", 0L));
        model.addAttribute("totalClients", data.getOrDefault("totalClients", 0L));
        model.addAttribute("bestSellingProducts", data.getOrDefault("bestSellingProducts", java.util.Collections.emptyList()));
        return "dashboard";
    }

    // --- Simple CSV report endpoints for dashboard 'Reportes' buttons ---
    @GetMapping("/reports/products")
        public org.springframework.http.ResponseEntity<org.springframework.core.io.ByteArrayResource> reportProducts(
            @org.springframework.web.bind.annotation.RequestParam(value = "format", required = false, defaultValue = "xlsx") String format
        ) throws Exception {
        var products = productRepository.findAll();
        if("xlsx".equalsIgnoreCase(format)){
            try(org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()){
                var sheet = wb.createSheet("Products");
                var header = sheet.createRow(0);
                String[] cols = new String[]{"id","name","category","price","stock","description"};
                for(int i=0;i<cols.length;i++) header.createCell(i).setCellValue(cols[i]);
                int r=1;
                for(var p: products){
                    var row = sheet.createRow(r++);
                    row.createCell(0).setCellValue(p.getId()!=null?p.getId():0);
                    row.createCell(1).setCellValue(p.getName()==null?"":p.getName());
                    row.createCell(2).setCellValue(p.getCategory()==null?"":p.getCategory());
                    row.createCell(3).setCellValue(p.getPrice()!=null? p.getPrice().doubleValue():0);
                    row.createCell(4).setCellValue(p.getStock());
                    row.createCell(5).setCellValue(p.getDescription()==null?"":p.getDescription());
                }
                try(java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()){ wb.write(out); byte[] bytes = out.toByteArray(); var res = new org.springframework.core.io.ByteArrayResource(bytes); org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders(); headers.add(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products_report.xlsx"); headers.add(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); return org.springframework.http.ResponseEntity.ok().headers(headers).contentLength(bytes.length).body(res); }
            }
        } else if("pdf".equalsIgnoreCase(format)){
            // simple PDF table using OpenPDF
            try(java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()){
                com.lowagie.text.Document doc = new com.lowagie.text.Document();
                com.lowagie.text.pdf.PdfWriter.getInstance(doc, out);
                doc.open();
                doc.add(new com.lowagie.text.Paragraph("Products Report"));
                com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(6);
                table.addCell("id"); table.addCell("name"); table.addCell("category"); table.addCell("price"); table.addCell("stock"); table.addCell("description");
                for(var p: products){
                    table.addCell(String.valueOf(p.getId()==null?"":p.getId()));
                    table.addCell(p.getName()==null?"":p.getName());
                    table.addCell(p.getCategory()==null?"":p.getCategory());
                    table.addCell(p.getPrice()!=null? p.getPrice().toString():"");
                    table.addCell(String.valueOf(p.getStock()));
                    table.addCell(p.getDescription()==null?"":p.getDescription());
                }
                doc.add(table); doc.close(); byte[] bytes = out.toByteArray(); var res = new org.springframework.core.io.ByteArrayResource(bytes); org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders(); headers.add(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products_report.pdf"); headers.add(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/pdf"); return org.springframework.http.ResponseEntity.ok().headers(headers).contentLength(bytes.length).body(res);
            }
        }
        // default csv
        StringBuilder sb = new StringBuilder();
        sb.append("id,name,category,price,stock,description\n");
        for(var p : products){
            sb.append(p.getId()).append(',')
              .append(escapeCsv(p.getName())).append(',')
              .append(escapeCsv(p.getCategory())).append(',')
              .append(p.getPrice()).append(',')
              .append(p.getStock()).append(',')
              .append(escapeCsv(p.getDescription())).append('\n');
        }
        byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        var res = new org.springframework.core.io.ByteArrayResource(bytes);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products_report.csv");
        headers.add(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv; charset=utf-8");
        return org.springframework.http.ResponseEntity.ok().headers(headers).contentLength(bytes.length).body(res);
    }

    @GetMapping("/reports/sales")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.ByteArrayResource> reportSales(
            @org.springframework.web.bind.annotation.RequestParam(value = "format", required = false, defaultValue = "xlsx") String format
    ) throws Exception {
        var sales = saleRepository.findAll();
        if("xlsx".equalsIgnoreCase(format)){
            try(org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()){
                var sheet = wb.createSheet("Sales");
                var header = sheet.createRow(0);
                String[] cols = new String[]{"id","date","total","quoteId"};
                for(int i=0;i<cols.length;i++) header.createCell(i).setCellValue(cols[i]);
                int r=1;
                for(var s: sales){
                    var row = sheet.createRow(r++);
                    row.createCell(0).setCellValue(s.getId()!=null? s.getId():0);
                    row.createCell(1).setCellValue(s.getDate()!=null? s.getDate().toString():"");
                    row.createCell(2).setCellValue(s.getTotal()!=null? s.getTotal().doubleValue():0);
                    row.createCell(3).setCellValue(s.getQuote()!=null && s.getQuote().getId()!=null? s.getQuote().getId():0);
                }
                try(java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()){ wb.write(out); byte[] bytes = out.toByteArray(); var res = new org.springframework.core.io.ByteArrayResource(bytes); org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders(); headers.add(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sales_report.xlsx"); headers.add(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); return org.springframework.http.ResponseEntity.ok().headers(headers).contentLength(bytes.length).body(res); }
            }
        } else if("pdf".equalsIgnoreCase(format)){
            try(java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()){
                com.lowagie.text.Document doc = new com.lowagie.text.Document();
                com.lowagie.text.pdf.PdfWriter.getInstance(doc, out);
                doc.open();
                doc.add(new com.lowagie.text.Paragraph("Sales Report"));
                com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(4);
                table.addCell("id"); table.addCell("date"); table.addCell("total"); table.addCell("quoteId");
                for(var s: sales){
                    table.addCell(String.valueOf(s.getId()==null?"":s.getId()));
                    table.addCell(s.getDate()!=null? s.getDate().toString():"");
                    table.addCell(s.getTotal()!=null? s.getTotal().toString():"");
                    table.addCell(s.getQuote()!=null && s.getQuote().getId()!=null? String.valueOf(s.getQuote().getId()):"");
                }
                doc.add(table); doc.close(); byte[] bytes = out.toByteArray(); var res = new org.springframework.core.io.ByteArrayResource(bytes); org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders(); headers.add(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sales_report.pdf"); headers.add(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/pdf"); return org.springframework.http.ResponseEntity.ok().headers(headers).contentLength(bytes.length).body(res);
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("id,date,total,quoteId\n");
        for(var s : sales){
            sb.append(s.getId()).append(',')
              .append(s.getDate()!=null ? s.getDate() : "").append(',')
              .append(s.getTotal()).append(',')
              .append(s.getQuote()!=null ? s.getQuote().getId() : "").append('\n');
        }
        byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        var res = new org.springframework.core.io.ByteArrayResource(bytes);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sales_report.csv");
        headers.add(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv; charset=utf-8");
        return org.springframework.http.ResponseEntity.ok().headers(headers).contentLength(bytes.length).body(res);
    }

    @GetMapping("/reports/quotes")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.ByteArrayResource> reportQuotes(
            @org.springframework.web.bind.annotation.RequestParam(value = "format", required = false, defaultValue = "xlsx") String format
    ) throws Exception {
        var quotes = quoteRepository.findAll();
        if("xlsx".equalsIgnoreCase(format)){
            try(org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()){
                var sheet = wb.createSheet("Quotes");
                var header = sheet.createRow(0);
                String[] cols = new String[]{"id","userId","date","status","total","discount","tax"};
                for(int i=0;i<cols.length;i++) header.createCell(i).setCellValue(cols[i]);
                int r=1;
                for(var q: quotes){
                    var row = sheet.createRow(r++);
                    row.createCell(0).setCellValue(q.getId()!=null? q.getId():0);
                    row.createCell(1).setCellValue(q.getUser()!=null && q.getUser().getId()!=null? q.getUser().getId():0);
                    row.createCell(2).setCellValue(q.getDate()!=null? q.getDate().toString():"");
                    row.createCell(3).setCellValue(q.getStatus()!=null? q.getStatus().name():"");
                    row.createCell(4).setCellValue(q.getTotal()!=null? q.getTotal().doubleValue():0);
                    row.createCell(5).setCellValue(q.getDiscount()!=null? q.getDiscount().doubleValue():0);
                    row.createCell(6).setCellValue(q.getTax()!=null? q.getTax().doubleValue():0);
                }
                try(java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()){ wb.write(out); byte[] bytes = out.toByteArray(); var res = new org.springframework.core.io.ByteArrayResource(bytes); org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders(); headers.add(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=quotes_report.xlsx"); headers.add(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); return org.springframework.http.ResponseEntity.ok().headers(headers).contentLength(bytes.length).body(res); }
            }
        } else if("pdf".equalsIgnoreCase(format)){
            try(java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()){
                com.lowagie.text.Document doc = new com.lowagie.text.Document();
                com.lowagie.text.pdf.PdfWriter.getInstance(doc, out);
                doc.open();
                doc.add(new com.lowagie.text.Paragraph("Quotes Report"));
                com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(7);
                table.addCell("id"); table.addCell("userId"); table.addCell("date"); table.addCell("status"); table.addCell("total"); table.addCell("discount"); table.addCell("tax");
                for(var q: quotes){
                    table.addCell(String.valueOf(q.getId()==null?"":q.getId()));
                    table.addCell(q.getUser()!=null && q.getUser().getId()!=null? String.valueOf(q.getUser().getId()):"");
                    table.addCell(q.getDate()!=null? q.getDate().toString():"");
                    table.addCell(q.getStatus()!=null? q.getStatus().name():"");
                    table.addCell(q.getTotal()!=null? q.getTotal().toString():"");
                    table.addCell(q.getDiscount()!=null? q.getDiscount().toString():"");
                    table.addCell(q.getTax()!=null? q.getTax().toString():"");
                }
                doc.add(table); doc.close(); byte[] bytes = out.toByteArray(); var res = new org.springframework.core.io.ByteArrayResource(bytes); org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders(); headers.add(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=quotes_report.pdf"); headers.add(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/pdf"); return org.springframework.http.ResponseEntity.ok().headers(headers).contentLength(bytes.length).body(res);
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("id,userId,date,status,total,discount,tax\n");
        for(var q : quotes){
            sb.append(q.getId()).append(',')
              .append(q.getUser()!=null? q.getUser().getId() : "").append(',')
              .append(q.getDate()!=null ? q.getDate() : "").append(',')
              .append(q.getStatus()!=null? q.getStatus().name() : "").append(',')
              .append(q.getTotal()).append(',')
              .append(q.getDiscount()).append(',')
              .append(q.getTax()).append('\n');
        }
        byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        var res = new org.springframework.core.io.ByteArrayResource(bytes);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=quotes_report.csv");
        headers.add(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv; charset=utf-8");
        return org.springframework.http.ResponseEntity.ok().headers(headers).contentLength(bytes.length).body(res);
    }

    @GetMapping("/reports/users")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.ByteArrayResource> reportUsers(
            @org.springframework.web.bind.annotation.RequestParam(value = "format", required = false, defaultValue = "xlsx") String format
    ) throws Exception {
        var users = userRepository.findAll();
        if("xlsx".equalsIgnoreCase(format)){
            try(org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()){
                var sheet = wb.createSheet("Users");
                var header = sheet.createRow(0);
                String[] cols = new String[]{"id","username","name","email","role","enabled"};
                for(int i=0;i<cols.length;i++) header.createCell(i).setCellValue(cols[i]);
                int r=1;
                for(var u: users){
                    var row = sheet.createRow(r++);
                    row.createCell(0).setCellValue(u.getId()!=null? u.getId():0);
                    row.createCell(1).setCellValue(u.getUsername()==null?"":u.getUsername());
                    row.createCell(2).setCellValue(u.getName()==null?"":u.getName());
                    row.createCell(3).setCellValue(u.getEmail()==null?"":u.getEmail());
                    row.createCell(4).setCellValue(u.getRole()==null?"":u.getRole().toString());
                    row.createCell(5).setCellValue(u.isEnabled());
                }
                try(java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()){ wb.write(out); byte[] bytes = out.toByteArray(); var res = new org.springframework.core.io.ByteArrayResource(bytes); org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders(); headers.add(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users_report.xlsx"); headers.add(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); return org.springframework.http.ResponseEntity.ok().headers(headers).contentLength(bytes.length).body(res); }
            }
        } else if("pdf".equalsIgnoreCase(format)){
            try(java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()){
                com.lowagie.text.Document doc = new com.lowagie.text.Document();
                com.lowagie.text.pdf.PdfWriter.getInstance(doc, out);
                doc.open();
                doc.add(new com.lowagie.text.Paragraph("Users Report"));
                com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(6);
                table.addCell("id"); table.addCell("username"); table.addCell("name"); table.addCell("email"); table.addCell("role"); table.addCell("enabled");
                for(var u: users){
                    table.addCell(String.valueOf(u.getId()==null?"":u.getId()));
                    table.addCell(u.getUsername()==null?"":u.getUsername());
                    table.addCell(u.getName()==null?"":u.getName());
                    table.addCell(u.getEmail()==null?"":u.getEmail());
                    table.addCell(u.getRole()==null?"":u.getRole().toString());
                    table.addCell(String.valueOf(u.isEnabled()));
                }
                doc.add(table); doc.close(); byte[] bytes = out.toByteArray(); var res = new org.springframework.core.io.ByteArrayResource(bytes); org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders(); headers.add(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users_report.pdf"); headers.add(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/pdf"); return org.springframework.http.ResponseEntity.ok().headers(headers).contentLength(bytes.length).body(res);
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("id,username,name,email,role,enabled\n");
        for(var u : users){
            sb.append(u.getId()).append(',')
              .append(escapeCsv(u.getUsername())).append(',')
              .append(escapeCsv(u.getName())).append(',')
              .append(escapeCsv(u.getEmail())).append(',')
              .append(u.getRole()).append(',')
              .append(u.isEnabled()).append('\n');
        }
        byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        var res = new org.springframework.core.io.ByteArrayResource(bytes);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users_report.csv");
        headers.add(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv; charset=utf-8");
        return org.springframework.http.ResponseEntity.ok().headers(headers).contentLength(bytes.length).body(res);
    }

    // helper to escape simple CSV fields
    private String escapeCsv(String v){ if(v==null) return ""; return '"' + v.replace("\"","\"\"") + '"'; }

    @GetMapping("/products")
    public String products(Model model, @org.springframework.web.bind.annotation.RequestParam(value = "q", required = false) String q) {
        if(q != null && !q.isBlank()){
            model.addAttribute("products", productRepository.findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(q,q));
        } else {
            model.addAttribute("products", productService.findAll());
        }
        model.addAttribute("q", q);
        return "products";
    }

    @GetMapping("/quotes")
    public String quotes(Model model, org.springframework.security.core.Authentication authentication) {
        java.util.List<com.industriagafra.entity.Quote> quotes;
        Long currentUserId = null;
        if(authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"))){
            var uOpt = userRepository.findByUsername(authentication.getName());
            if(uOpt.isPresent()){
                var u = uOpt.get();
                currentUserId = u.getId();
                quotes = quoteService.findByUser(u);
            } else {
                quotes = java.util.Collections.emptyList();
            }
        } else {
            quotes = quoteService.findAll();
        }
        model.addAttribute("quotes", quotes);
        model.addAttribute("currentUserId", currentUserId);
        // build product summary per quote
        java.util.Map<Long,String> quoteProducts = new java.util.HashMap<>();
        for(var q : quotes){
            var details = quoteDetailRepository.findByQuote(q);
            String summary = details.stream().map(d -> d.getProduct()!=null? d.getProduct().getName()+" x"+d.getQuantity() : "").filter(s->!s.isBlank()).collect(java.util.stream.Collectors.joining(", "));
            quoteProducts.put(q.getId(), summary);
        }
        model.addAttribute("quoteProducts", quoteProducts);
        return "quotes";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.findAll());
        return "users";
    }

    @GetMapping("/sales")
    public String sales(Model model, org.springframework.security.core.Authentication authentication) {
        java.util.List<com.industriagafra.entity.Sale> sales;
        if(authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"))){
            var uOpt = userRepository.findByUsername(authentication.getName());
            if(uOpt.isPresent()){
                // fetch all sales and filter by quote.user == current
                var all = saleService.findAll();
                sales = all.stream().filter(s -> s.getQuote() != null && s.getQuote().getUser() != null && s.getQuote().getUser().getId().equals(uOpt.get().getId())).toList();
            } else {
                sales = java.util.Collections.emptyList();
            }
        } else {
            sales = saleService.findAll();
        }
        model.addAttribute("sales", sales);
        java.util.Map<Long,String> saleProducts = new java.util.HashMap<>();
        for(var s : sales){
            var details = saleDetailRepository.findBySale(s);
            String summary = details.stream().map(d -> d.getProduct()!=null? d.getProduct().getName()+" x"+d.getQuantity() : "").filter(sx->!sx.isBlank()).collect(java.util.stream.Collectors.joining(", "));
            saleProducts.put(s.getId(), summary);
        }
        model.addAttribute("saleProducts", saleProducts);
        return "sales";
    }

    @GetMapping("/users/new")
    public String newUser(Model model) {
        model.addAttribute("user", new com.industriagafra.entity.User());
        return "user_form";
    }

    @GetMapping("/users/{id}/edit")
    public String editUser(@PathVariable Long id, Model model) {
        var u = userRepository.findById(id);
        if(u.isPresent()){
            var user = u.get();
            user.setPassword(null);
            model.addAttribute("user", user);
        } else {
            model.addAttribute("user", new com.industriagafra.entity.User());
        }
        return "user_form";
    }

    @PostMapping("/users/save")
    public String saveUser(@ModelAttribute("user") com.industriagafra.entity.User formUser){
        if(formUser.getId() == null){
            // new user - always register as CLIENT
            if(formUser.getPassword() != null) formUser.setPassword(passwordEncoder.encode(formUser.getPassword()));
            formUser.setRole(com.industriagafra.entity.Role.CLIENT);
            formUser.setEnabled(true);
            userRepository.save(formUser);
        } else {
            var existingOpt = userRepository.findById(formUser.getId());
            if(existingOpt.isPresent()){
                var existing = existingOpt.get();
                existing.setName(formUser.getName());
                existing.setUsername(formUser.getUsername());
                existing.setEmail(formUser.getEmail());
                existing.setRole(formUser.getRole());
                existing.setEnabled(formUser.isEnabled());
                existing.setDocumentNumber(formUser.getDocumentNumber());
                existing.setAddress(formUser.getAddress());
                existing.setPaymentMethod(formUser.getPaymentMethod());
                if(formUser.getPassword() != null && !formUser.getPassword().isBlank()){
                    existing.setPassword(passwordEncoder.encode(formUser.getPassword()));
                }
                userRepository.save(existing);
            }
        }
        return "redirect:/users";
    }

    @GetMapping("/sales/new")
    public String newSale() { return "sale_form"; }

    @GetMapping("/sales/{id}/edit")
    public String editSale() { return "sale_form"; }

    @GetMapping("/products/new")
    public String newProduct(Model model) {
        model.addAttribute("product", new Product());
        return "product_form";
    }

    @GetMapping("/products/{id}/edit")
    public String editProduct(@PathVariable Long id, Model model) {
        var p = productRepository.findById(id);
        if(p.isPresent()){
            model.addAttribute("product", p.get());
        } else {
            model.addAttribute("product", new Product());
        }
        return "product_form";
    }

    @GetMapping("/products/{id}")
    public String viewProduct(@PathVariable Long id, Model model){
        var p = productRepository.findById(id);
        if(p.isPresent()){
            model.addAttribute("product", p.get());
            model.addAttribute("readonly", true);
        } else {
            model.addAttribute("product", new Product());
        }
        return "product_form";
    }

    @PostMapping("/products/save")
    public String saveProduct(@org.springframework.web.bind.annotation.ModelAttribute("product") Product form){
        if(form.getId() == null){
            productService.save(form);
        } else {
            var opt = productRepository.findById(form.getId());
            if(opt.isPresent()){
                var existing = opt.get();
                existing.setName(form.getName());
                existing.setDescription(form.getDescription());
                existing.setPrice(form.getPrice());
                existing.setStock(form.getStock());
                existing.setCategory(form.getCategory());
                productService.save(existing);
            }
        }
        return "redirect:/products";
    }

    @GetMapping("/quotes/new")
    public String newQuote(Model model) {
        model.addAttribute("quote", new com.industriagafra.entity.Quote());
        model.addAttribute("details", java.util.Collections.emptyList());
        model.addAttribute("subtotal", java.math.BigDecimal.ZERO);
        model.addAttribute("totalComputed", java.math.BigDecimal.ZERO);
        model.addAttribute("products", productRepository.findAll());
        return "quote_form";
    }

    @GetMapping("/quotes/{id}/edit")
    public String editQuote(@PathVariable Long id, Model model) {
        var qOpt = quoteRepository.findById(id);
        if(qOpt.isPresent()){
            var q = qOpt.get();
            model.addAttribute("quote", q);
            var details = quoteDetailRepository.findByQuote(q);
            model.addAttribute("details", details);
            java.math.BigDecimal subtotal = details.stream().map(d -> d.getPrice().multiply(new java.math.BigDecimal(d.getQuantity()))).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("totalComputed", subtotal.subtract(q.getDiscount()).add(q.getTax()));
        } else {
            model.addAttribute("quote", new com.industriagafra.entity.Quote());
            model.addAttribute("details", java.util.Collections.emptyList());
        }
        return "quote_form";
    }

    @GetMapping("/quotes/{id}")
    public String viewQuote(@PathVariable Long id, Model model){
        var qOpt = quoteRepository.findById(id);
        if(qOpt.isPresent()){
            var q = qOpt.get();
            model.addAttribute("quote", q);
            var details = quoteDetailRepository.findByQuote(q);
            model.addAttribute("details", details);
            java.math.BigDecimal subtotal = details.stream().map(d -> d.getPrice().multiply(new java.math.BigDecimal(d.getQuantity()))).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("totalComputed", subtotal.subtract(q.getDiscount()).add(q.getTax()));
            model.addAttribute("readonly", true);
        } else {
            model.addAttribute("quote", new com.industriagafra.entity.Quote());
            model.addAttribute("details", java.util.Collections.emptyList());
        }
        return "quote_form";
    }

    @PostMapping("/quotes/save")
    public String saveQuote(@org.springframework.web.bind.annotation.ModelAttribute("quote") com.industriagafra.entity.Quote form,
                            jakarta.servlet.http.HttpServletRequest request,
                            org.springframework.security.core.Authentication authentication){
        // Parse dynamic details submitted as details[N].product.id, details[N].quantity, details[N].price
        java.util.Set<Integer> indices = new java.util.HashSet<>();
        var params = request.getParameterMap();
        for(String key : params.keySet()){
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("details\\[(\\d+)\\]\\..*").matcher(key);
            if(m.matches()){
                try{ indices.add(Integer.parseInt(m.group(1))); } catch(Exception ex){ }
            }
        }

        com.industriagafra.entity.Quote quoteToSave = null;
        if(form.getId() == null){
            // set user from authentication if available
            if(authentication != null){
                var uOpt = userRepository.findByUsername(authentication.getName());
                uOpt.ifPresent(form::setUser);
            }
            // initialize date and status
            if(form.getDate() == null) form.setDate(java.time.LocalDateTime.now());
            if(form.getStatus() == null) form.setStatus(com.industriagafra.entity.QuoteStatus.PENDING);
            quoteToSave = quoteRepository.save(form);
        } else {
            var opt = quoteRepository.findById(form.getId());
            if(opt.isPresent()){
                var existing = opt.get();
                existing.setStatus(form.getStatus());
                existing.setDate(form.getDate());
                existing.setDiscount(form.getDiscount());
                existing.setTax(form.getTax());
                existing.setTotal(form.getTotal());
                quoteToSave = quoteRepository.save(existing);

                // remove old details so we can replace with submitted ones
                var old = quoteDetailRepository.findByQuote(existing);
                if(old != null && !old.isEmpty()){
                    quoteDetailRepository.deleteAll(old);
                }
            }
        }

        if(quoteToSave != null){
            java.math.BigDecimal subtotal = java.math.BigDecimal.ZERO;
            java.util.List<com.industriagafra.entity.QuoteDetail> savedDetails = new java.util.ArrayList<>();
            java.util.List<Integer> sortedIdx = new java.util.ArrayList<>(indices);
            java.util.Collections.sort(sortedIdx);
            for(Integer i : sortedIdx){
                String pid = request.getParameter("details["+i+"].product.id");
                String qtyS = request.getParameter("details["+i+"].quantity");
                String priceS = request.getParameter("details["+i+"].price");
                if(pid == null || pid.isBlank()) continue;
                try{
                    Long productId = Long.parseLong(pid);
                    int qty = Integer.parseInt(qtyS == null ? "1" : qtyS);
                    java.math.BigDecimal price = new java.math.BigDecimal(priceS == null || priceS.isBlank() ? "0" : priceS);
                    var pOpt = productRepository.findById(productId);
                    if(pOpt.isPresent()){
                        com.industriagafra.entity.QuoteDetail d = new com.industriagafra.entity.QuoteDetail();
                        d.setQuote(quoteToSave);
                        d.setProduct(pOpt.get());
                        d.setQuantity(qty);
                        d.setPrice(price);
                        quoteDetailRepository.save(d);
                        savedDetails.add(d);
                        subtotal = subtotal.add(d.getSubtotal());
                    }
                } catch(Exception ex){
                    // ignore malformed row
                }
            }
            // Recompute discounts/tax similar to service rules
            java.math.BigDecimal discount = java.math.BigDecimal.ZERO;
            int totalProducts = savedDetails.stream().mapToInt(com.industriagafra.entity.QuoteDetail::getQuantity).sum();
            if(totalProducts > 10){ discount = subtotal.multiply(java.math.BigDecimal.valueOf(0.20)); }
            java.math.BigDecimal tax = subtotal.subtract(discount).multiply(java.math.BigDecimal.valueOf(0.19));
            quoteToSave.setDiscount(discount);
            quoteToSave.setTax(tax);
            quoteToSave.setTotal(subtotal.subtract(discount).add(tax));
            quoteRepository.save(quoteToSave);
        }

        return "redirect:/quotes";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteById(id);
        return "redirect:/products";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return "redirect:/users";
    }

    @PostMapping("/quotes/{id}/delete")
    public String deleteQuote(@PathVariable Long id, org.springframework.security.core.Authentication authentication) {
        // if client, ensure ownership before deleting
        if(authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"))){
            var uOpt = userRepository.findByUsername(authentication.getName());
            var qOpt = quoteRepository.findById(id);
            if(uOpt.isEmpty() || qOpt.isEmpty() || qOpt.get().getUser() == null || !qOpt.get().getUser().getId().equals(uOpt.get().getId())){
                return "redirect:/quotes";
            }
        }
        quoteService.deleteById(id);
        return "redirect:/quotes";
    }

    @PostMapping("/quotes/{id}/approve")
    public String approveQuote(@PathVariable Long id, org.springframework.web.servlet.mvc.support.RedirectAttributes redirect, org.springframework.security.core.Authentication authentication) {
        try {
            // if client, ensure ownership
            if(authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"))){
                var uOpt = userRepository.findByUsername(authentication.getName());
                if(uOpt.isEmpty()){
                    redirect.addFlashAttribute("error", "Usuario no encontrado");
                    return "redirect:/quotes/" + id;
                }
                var qOpt = quoteRepository.findById(id);
                if(qOpt.isEmpty() || qOpt.get().getUser() == null || !qOpt.get().getUser().getId().equals(uOpt.get().getId())){
                    redirect.addFlashAttribute("error", "No autorizado para aprobar esta cotización");
                    return "redirect:/quotes";
                }
            }
            quoteService.approveQuote(id);
            redirect.addFlashAttribute("success", "Cotización aprobada y venta creada.");
            return "redirect:/quotes/" + id;
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/quotes/" + id;
        }
    }

    @PostMapping("/sales/{id}/delete")
    public String deleteSale(@PathVariable Long id) {
        saleService.deleteById(id);
        return "redirect:/sales";
    }
}