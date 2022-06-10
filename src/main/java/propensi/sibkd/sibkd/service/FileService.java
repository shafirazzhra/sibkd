package propensi.sibkd.sibkd.service;

import org.springframework.web.multipart.MultipartFile;
import propensi.sibkd.sibkd.model.File;

import java.io.IOException;

public interface FileService {
    File storeFile(MultipartFile file) throws IOException;

    File storeFileWithLink(MultipartFile file) throws IOException;

    File getFile(String fileId);

    File getFileByUrl(String url);

    void deleteFile(File file);

    void updateFile(File file);
}
