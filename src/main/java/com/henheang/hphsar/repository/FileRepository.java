package com.henheang.hphsar.repository;

import com.henheang.hphsar.model.file.StoredFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * FileRepository — persistence for tb_file (uploaded bytes stored in Postgres).
 * All SQL lives in resources/mapper/FileMapper.xml.
 */
@Mapper
public interface FileRepository {

    Integer insertFile(@Param("id") String id,
                        @Param("contentType") String contentType,
                        @Param("data") byte[] data);

    StoredFile findById(@Param("id") String id);
}
