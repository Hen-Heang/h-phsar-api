package com.henheang.hphsar.repository;

import com.henheang.hphsar.model.file.StoredFile;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * FileRepository — persistence for tb_file (uploaded bytes stored in Postgres).
 * Single table, no joins, so the SQL lives directly on the interface instead
 * of a separate XML mapper; map-underscore-to-camel-case handles the column
 * mapping for {@link StoredFile}.
 */
@Mapper
public interface FileRepository {

    @Insert("INSERT INTO tb_file (id, content_type, data) VALUES (#{id}, #{contentType}, #{data})")
    Integer insertFile(@Param("id") String id,
                        @Param("contentType") String contentType,
                        @Param("data") byte[] data);

    @Select("SELECT id, content_type, data, created_date FROM tb_file WHERE id = #{id}")
    StoredFile findById(@Param("id") String id);
}
