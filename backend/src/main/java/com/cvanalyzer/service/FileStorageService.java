package com.cvanalyzer.service;

import com.cvanalyzer.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    public String storeFile(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String originalFileName = file.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String storedFileName = UUID.randomUUID() + extension;
            Path targetLocation = uploadPath.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return storedFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }

    /** SHA-256 hex checksum of arbitrary bytes (used for resume integrity / dedup). */
    public String computeChecksum(byte[] bytes) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /** Persist raw bytes under a generated name and return that stored name. */
    public String storeBytes(byte[] bytes, String extension) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            String storedFileName = UUID.randomUUID() + (extension == null ? "" : extension);
            Path target = uploadPath.resolve(storedFileName);
            Files.write(target, bytes);
            return storedFileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not store generated file", e);
        }
    }

    public String getFilePath(String storedFileName) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        return uploadPath.resolve(storedFileName).toString();
    }

    public byte[] loadFileAsBytes(String filePath) {
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            throw new RuntimeException("Could not read file: " + filePath, e);
        }
    }

    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("Could not delete file: {}", filePath, e);
        }
    }

    public String extractText(MultipartFile file) {
        String contentType = file.getContentType();
        String originalName = file.getOriginalFilename();

        if (file.isEmpty() || file.getSize() == 0) {
            throw new BadRequestException("Uploaded file is empty");
        }

        try {
            if (isPdf(contentType, originalName)) {
                return extractTextFromPdf(file.getInputStream());
            } else if (isDocx(contentType, originalName)) {
                return extractTextFromDocx(file.getInputStream());
            } else if (isText(contentType, originalName)) {
                return new String(file.getBytes());
            } else {
                throw new BadRequestException("Unsupported file type: " + contentType);
            }
        } catch (IOException e) {
            log.warn("Failed to extract text from {}: {}", originalName, e.getMessage());
            throw new BadRequestException(
                "Could not read this file. It may be corrupt, password-protected, or a scanned image without selectable text."
            );
        }
    }

    public String extractTextFromPath(String filePath, String fileType) {
        try {
            Path path = Paths.get(filePath);
            if (isPdf(fileType, filePath)) {
                try (InputStream is = Files.newInputStream(path)) {
                    return extractTextFromPdf(is);
                }
            } else if (isDocx(fileType, filePath)) {
                try (InputStream is = Files.newInputStream(path)) {
                    return extractTextFromDocx(is);
                }
            } else {
                return Files.readString(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract text from file: " + filePath, e);
        }
    }

    private String extractTextFromPdf(InputStream inputStream) throws IOException {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractTextFromDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private boolean isPdf(String contentType, String fileName) {
        return "application/pdf".equalsIgnoreCase(contentType)
                || (fileName != null && fileName.toLowerCase().endsWith(".pdf"));
    }

    private boolean isDocx(String contentType, String fileName) {
        return "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equalsIgnoreCase(contentType)
                || "application/msword".equalsIgnoreCase(contentType)
                || (fileName != null && (fileName.toLowerCase().endsWith(".docx") || fileName.toLowerCase().endsWith(".doc")));
    }

    private boolean isText(String contentType, String fileName) {
        return "text/plain".equalsIgnoreCase(contentType)
                || (fileName != null && fileName.toLowerCase().endsWith(".txt"));
    }
}
