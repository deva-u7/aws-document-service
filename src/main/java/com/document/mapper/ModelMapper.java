package com.document.mapper;

import com.document.dto.DocumentDTO;
import com.document.entity.Document;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ModelMapper {
    DocumentDTO toDTO(Document document);
    Document toEntity(DocumentDTO documentDTO);
}
