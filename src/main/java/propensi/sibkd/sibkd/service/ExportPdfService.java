package propensi.sibkd.sibkd.service;

import java.io.ByteArrayInputStream;
import java.util.Map;

public interface ExportPdfService {
    ByteArrayInputStream exportLaporanPdf(String templateName, Map<String, Object> data);
}
