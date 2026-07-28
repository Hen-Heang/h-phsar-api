package com.henheang.hphsar.model.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * StoredFile — a row in tb_file. Uploaded bytes (images, etc.) live in Postgres
 * itself rather than on local disk, since the deployment's local filesystem is
 * ephemeral and would lose every upload on restart/redeploy.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoredFile {
    private String id;
    private String contentType;
    private byte[] data;
    private Timestamp createdDate;
}
