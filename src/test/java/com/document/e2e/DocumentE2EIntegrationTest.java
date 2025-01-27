package com.document.e2e;

import com.document.controller.DocumentController;
import com.document.dto.DocumentDTO;
import com.document.entity.Document;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import static com.document.e2e.utils.DocumentUtils.getExpectedResponse;
import static com.document.e2e.utils.DocumentUtils.getMockMultipartFile;
import static com.document.e2e.utils.DocumentUtils.getRequest;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


public class DocumentE2EIntegrationTest extends IntegrationBaseTest {

    @Autowired
    private DocumentController documentController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void beforeEach() {
        documentRepository.deleteAll();
        mockMvc = MockMvcBuilders.standaloneSetup(documentController).build();
    }

    @Test
    @DisplayName("Should be able to upload file to the test s3 bucket")
    void saveDocument() throws Exception {
        DocumentDTO request = getRequest();
        String fileName = "File.txt";
        MockMultipartFile multipartFile = getMockMultipartFile(fileName);

        String responseString = mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/api/v1/document")
                        .file(multipartFile)
                        .param("userName", request.getUserName())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        )
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString();
        DocumentDTO actualResponse = objectMapper.readValue(responseString, DocumentDTO.class);
        Assertions.assertEquals(getExpectedResponse(fileName,bucketName), actualResponse);

        Document document = documentRepository.findById(actualResponse.getId()).orElseThrow();
        // Get uploaded file from the bucket
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(document.getS3Key())
                .build();

        ResponseInputStream<GetObjectResponse> response = amazonS3.getObject(getObjectRequest);

        //Assert that the file content matches
        Assertions.assertArrayEquals(multipartFile.getBytes(), response.readAllBytes(), "The file content does not match.");
    }

    @Test
    @DisplayName("Should be able to fetch file from test bucket")
    void fetchDocument() throws Exception {
        DocumentDTO request = getRequest();
        String fileName = "File.txt";
        MockMultipartFile multipartFile = getMockMultipartFile(fileName);

        // Upload File and Insert Record in Database
        String responseString = mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/api/v1/document")
                        .file(multipartFile)
                        .param("userName", request.getUserName())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        DocumentDTO actualResponse = objectMapper.readValue(responseString, DocumentDTO.class);

        byte[] response = mockMvc.perform(get("/api/v1/document/{documentId}", actualResponse.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        //Assert that the file content matches
        Assertions.assertArrayEquals(multipartFile.getBytes(), response, "The file content does not match.");
    }

}

