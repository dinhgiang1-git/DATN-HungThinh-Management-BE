package dinhgiang.dev.hungthinh.services.implement;

import dinhgiang.dev.hungthinh.exceptions.UserMessageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Lớp dịch vụ quản lý file đính kèm (File Storage).
 * Lưu trữ file, tạo đường dẫn và xóa file.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Service

public class FileStorageService {

    private final Path fileStoragePath;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("doc", "docx", "pdf");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public FileStorageService(@Value("${file.upload-dir:uploads/contracts}") String uploadDir) {
        this.fileStoragePath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStoragePath);
        } catch (IOException ex) {
            throw new RuntimeException("Không thể tạo thư mục lưu trữ file: " + uploadDir, ex);
        }
    }

    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new UserMessageException("File không được để trống");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new UserMessageException("File vượt quá kích thước tối đa cho phép (10MB)");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new UserMessageException("Tên file không hợp lệ");
        }

        String extension = getFileExtension(originalFileName);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new UserMessageException("Chỉ cho phép upload file .doc, .docx, .pdf");
        }

        // Tạo tên file unique để tránh trùng
        String storedFileName = UUID.randomUUID() + "." + extension;

        try {
            Path targetLocation = this.fileStoragePath.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return storedFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Không thể lưu file: " + originalFileName, ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStoragePath.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new UserMessageException("File không tồn tại: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new UserMessageException("File không tồn tại: " + fileName);
        }
    }

    public void deleteFile(String fileName) {
        if (fileName == null || fileName.isBlank()) return;
        try {
            Path filePath = this.fileStoragePath.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            // Log warning but don't throw - file deletion failure shouldn't block the operation
        }
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0) {
            return "";
        }
        return fileName.substring(lastDot + 1);
    }
}
