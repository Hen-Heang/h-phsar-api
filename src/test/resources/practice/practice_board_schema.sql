-- Test-only table for the P1 OLV-style board practice (PracticeBoardMapperIT).
-- Deliberately NOT added to script/schema.sql or DatabaseInitializer: this is a
-- learning sandbox and must not change the real application schema.
-- Column names copy OLV's co_bbs_m naming (bbs_*, *_yn, data_reg_*/data_chg_* audit columns).
CREATE TABLE IF NOT EXISTS tb_practice_board (
    bbs_sn       BIGSERIAL    PRIMARY KEY,
    bbs_ttl_nm   VARCHAR(200) NOT NULL,
    bbs_cn       TEXT,
    bbs_inq_cnt  INTEGER      NOT NULL DEFAULT 0,
    pstg_yn      CHAR(1)      NOT NULL DEFAULT 'Y',
    del_yn       CHAR(1)      NOT NULL DEFAULT 'N',
    data_reg_id  VARCHAR(100) NOT NULL,
    data_reg_dt  TIMESTAMP    NOT NULL DEFAULT NOW(),
    data_chg_id  VARCHAR(100),
    data_chg_dt  TIMESTAMP
);
