package propensi.sibkd.sibkd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import propensi.sibkd.sibkd.model.File;
import propensi.sibkd.sibkd.repository.FileDb;

import javax.transaction.Transactional;
import java.io.IOException;

@Service
@Transactional
public class FileServiceImpl implements FileService{
    @Autowired
    FileDb fileDb;

    @Override
    public File storeFile(MultipartFile file) throws IOException {
        if (!file.getContentType().equals("application/octet-stream")) {
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            File dbFile = new File(fileName, file.getContentType(), file.getBytes());
    
            return fileDb.save(dbFile);
        } 
        return null;
    }

    public File storeFileWithLink(MultipartFile file) throws IOException {
        System.out.print(file.getContentType());
        if (!file.getContentType().equals("application/octet-stream")) {
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            File dbFile = new File(fileName, file.getContentType(), file.getBytes());
            dbFile = fileDb.save(dbFile);
    
            String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/viewFile/")
                    .path(dbFile.getId())
                    .toUriString();
    
            dbFile.setUrlView(url);
    
            return dbFile;
        }
        return null;
    }

    @Override
    public File getFile(String fileId) {
        return fileDb.findById(fileId).get();
    }

    @Override
    public void updateFile(File file) {
        fileDb.save(file);
    }

    @Override
    public File getFileByUrl(String url) {
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/viewFile/")
                .toUriString();
        String id = url.replace(fileDownloadUri, "");
        return fileDb.findById(id).get();
    }

    @Override
    public void deleteFile(File file) {
        fileDb.delete(file);
    }
}