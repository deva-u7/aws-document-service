package com.document.service;

import com.document.FileUploadStatus;
import com.document.dto.DocumentDTO;
import com.document.entity.Document;
import com.document.mapper.ModelMapper;
import com.document.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.util.Pair;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AWSS3FileService s3FileService;

    public DocumentDTO saveDocuments(DocumentDTO documentDTO, MultipartFile file )  {
        log.info("Starting process to add a new Document : {}", documentDTO);
        Document document = documentRepository.save(modelMapper.toEntity(documentDTO));
        try {
            if (file != null){
                log.info("File detected for upload: {}", file.getOriginalFilename());
                Pair<String, String> response = s3FileService.fileUpload(file);
                document.setUploadStatus(FileUploadStatus.COMPLETED);
                document.setFileURL(response.getFirst());
                document.setS3Key(response.getSecond());
                documentRepository.save(document);
            }

            log.info("Document  successfully uploaded with ID: {}", document.getId());
            return modelMapper.toDTO(document);
        } catch (Exception ex) {
            document.setUploadStatus(FileUploadStatus.FAILED);
            documentRepository.save(document);
            log.error("Error while save Document",ex);
            throw new RuntimeException("Exception while uploading the doc",ex);
        }
    }

    public Pair<HttpHeaders,byte[]> fetchDocument(Long documentId) throws IOException {
        log.info("Starting process to fetch for DocumentId : {}", documentId);
            Document document = documentRepository.findById(documentId).orElseThrow();
            if(document.getUploadStatus().equals(FileUploadStatus.COMPLETED)){
                ResponseInputStream<GetObjectResponse> s3Response = s3FileService.fetchDocumentByKey(document.getS3Key());

                // Extract metadata
                GetObjectResponse documentMetadata = s3Response.response();
                String contentType = documentMetadata.contentType();
                long contentLength = documentMetadata.contentLength();
                String fileName = documentMetadata.metadata().getOrDefault("file-name", "file");

                // Set headers
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(contentType));
                headers.setContentLength(contentLength);
                headers.setContentDisposition(ContentDisposition.builder("attachment").filename(fileName).build());

                // Read the file content as a byte array
                byte[] fileContent = s3Response.readAllBytes();
                log.info("Document  fetch  successfully for DocumentId : {}", documentId);
                return Pair.of(headers,fileContent);

            }else{
                // For now just throw exception here we can manage others scenarios here
                throw new RuntimeException("Document not Uploaded for documentId " + documentId);
            }
    }
}
