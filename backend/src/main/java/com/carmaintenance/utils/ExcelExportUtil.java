package com.carmaintenance.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * Excel导出工具类
 * 使用Apache POI实现Excel文件导出
 */
public class ExcelExportUtil {

    /**
     * 导出Excel文件到HTTP响应
     * 
     * @param fileName 文件名（不含扩展名）
     * @param headers 表头列表
     * @param dataList 数据列表，每个Map代表一行数据
     * @param response HTTP响应对象
     * @throws IOException IO异常
     */
    public static void exportToExcel(String fileName, 
                                     List<String> headers, 
                                     List<Map<String, Object>> dataList, 
                                     HttpServletResponse response) throws IOException {
        
        // 创建工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("数据");

        // 创建表头样式
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        // 创建数据样式
        CellStyle dataStyle = createDataStyle(workbook);

        // 创建表头行
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
            
            // 自动调整列宽
            sheet.setColumnWidth(i, 15 * 256); // 15个字符宽度
        }

        // 填充数据
        int rowIndex = 1;
        for (Map<String, Object> dataMap : dataList) {
            Row row = sheet.createRow(rowIndex++);
            int cellIndex = 0;
            
            for (String header : headers) {
                Cell cell = row.createCell(cellIndex++);
                Object value = dataMap.get(header);
                
                if (value != null) {
                    if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else {
                        cell.setCellValue(value.toString());
                    }
                }
                cell.setCellStyle(dataStyle);
            }
        }

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName + ".xlsx");

        // 写入响应流
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    /**
     * 创建表头样式
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 设置背景色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // 设置边框
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        
        // 设置字体
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        
        // 设置居中
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }

    /**
     * 创建数据样式
     */
    private static CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 设置边框
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        
        // 设置对齐
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }
}






