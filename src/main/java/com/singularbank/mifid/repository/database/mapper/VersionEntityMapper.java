package com.singularbank.mifid.repository.database.mapper;

import com.singularbank.mifid.entity.Version;
import com.singularbank.mifid.repository.database.model.VersionEntity;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VersionEntityMapper {

  Version toDomain(VersionEntity entity);

  List<Version> toDomain(List<VersionEntity> entityList);
}
