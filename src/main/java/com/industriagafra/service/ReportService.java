package com.industriagafra.service;

import com.industriagafra.entity.Product;
import com.industriagafra.repository.ProductRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private ProductRepository productRepository;

    public ByteArrayInputStream exportProductsToExcel() throws IOException {
        List<Product> products = productRepository.findAll();

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Products");

            int rowIdx = 0;
            Row header = sheet.createRow(rowIdx++);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Description");
            header.createCell(3).setCellValue("Price");
            header.createCell(4).setCellValue("Stock");

            for (Product p : products) {
                Row row = sheet.createRow(rowIdx++);
                int col = 0;
                row.createCell(col++).setCellValue(p.getId());
                row.createCell(col++).setCellValue(p.getName());
                row.createCell(col++).setCellValue(p.getDescription() == null ? "" : p.getDescription());
                row.createCell(col++).setCellValue(p.getPrice() != null ? p.getPrice().doubleValue() : 0);
                row.createCell(col++).setCellValue(p.getStock());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
