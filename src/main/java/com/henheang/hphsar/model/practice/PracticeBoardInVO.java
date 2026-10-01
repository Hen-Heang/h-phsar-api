package com.henheang.hphsar.model.practice;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * P1 practice: OLV-style input VO for the practice board (search, paging, and write fields).
 * <p>
 * Written without Lombok on purpose, because OLV does not use Lombok. In OLV this class would
 * extend {@code CmmVO}, which supplies the paging fields; here they are declared inline.
 *
 * @author Hen-Heang
 * @since 2026.09.28
 * @version 1.0
 */
public class PracticeBoardInVO {

    /** Board serial number (primary key). Filled by MyBatis generated keys after insert. */
    private Long bbsSn;

    /** Optional title search keyword. */
    private String searchKeyword;

    /** 1-based page number requested by the client. */
    private int pageIndex = 1;

    /** Rows per page. Capped so one request cannot load the whole table. */
    @Min(1)
    @Max(100)
    private int recordCountPerPage = 10;

    /** Zero-based row offset for {@code OFFSET}. Calculated by the service, never by the client. */
    private int firstIndex;

    /** Post title. */
    private String bbsTtlNm;

    /** Post content. */
    private String bbsCn;

    /** Published flag ('Y' or 'N'). */
    private String pstgYn;

    /** Registering user ID (audit column). */
    private String dataRegId;

    /** Last modifying user ID (audit column). */
    private String dataChgId;

    /**
     * Returns the board serial number.
     *
     * @return board serial number
     */
    public Long getBbsSn() {
        return bbsSn;
    }

    /**
     * Sets the board serial number.
     *
     * @param bbsSn board serial number
     */
    public void setBbsSn(Long bbsSn) {
        this.bbsSn = bbsSn;
    }

    /**
     * Returns the title search keyword.
     *
     * @return search keyword, or {@code null} when not searching
     */
    public String getSearchKeyword() {
        return searchKeyword;
    }

    /**
     * Sets the title search keyword.
     *
     * @param searchKeyword search keyword
     */
    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    /**
     * Returns the 1-based page number.
     *
     * @return page number
     */
    public int getPageIndex() {
        return pageIndex;
    }

    /**
     * Sets the 1-based page number.
     *
     * @param pageIndex page number
     */
    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    /**
     * Returns the number of rows per page.
     *
     * @return rows per page
     */
    public int getRecordCountPerPage() {
        return recordCountPerPage;
    }

    /**
     * Sets the number of rows per page.
     *
     * @param recordCountPerPage rows per page
     */
    public void setRecordCountPerPage(int recordCountPerPage) {
        this.recordCountPerPage = recordCountPerPage;
    }

    /**
     * Returns the zero-based row offset.
     *
     * @return row offset
     */
    public int getFirstIndex() {
        return firstIndex;
    }

    /**
     * Sets the zero-based row offset.
     *
     * @param firstIndex row offset
     */
    public void setFirstIndex(int firstIndex) {
        this.firstIndex = firstIndex;
    }

    /**
     * Returns the post title.
     *
     * @return title
     */
    public String getBbsTtlNm() {
        return bbsTtlNm;
    }

    /**
     * Sets the post title.
     *
     * @param bbsTtlNm title
     */
    public void setBbsTtlNm(String bbsTtlNm) {
        this.bbsTtlNm = bbsTtlNm;
    }

    /**
     * Returns the post content.
     *
     * @return content
     */
    public String getBbsCn() {
        return bbsCn;
    }

    /**
     * Sets the post content.
     *
     * @param bbsCn content
     */
    public void setBbsCn(String bbsCn) {
        this.bbsCn = bbsCn;
    }

    /**
     * Returns the published flag.
     *
     * @return 'Y' or 'N'
     */
    public String getPstgYn() {
        return pstgYn;
    }

    /**
     * Sets the published flag.
     *
     * @param pstgYn 'Y' or 'N'
     */
    public void setPstgYn(String pstgYn) {
        this.pstgYn = pstgYn;
    }

    /**
     * Returns the registering user ID.
     *
     * @return registering user ID
     */
    public String getDataRegId() {
        return dataRegId;
    }

    /**
     * Sets the registering user ID.
     *
     * @param dataRegId registering user ID
     */
    public void setDataRegId(String dataRegId) {
        this.dataRegId = dataRegId;
    }

    /**
     * Returns the last modifying user ID.
     *
     * @return last modifying user ID
     */
    public String getDataChgId() {
        return dataChgId;
    }

    /**
     * Sets the last modifying user ID.
     *
     * @param dataChgId last modifying user ID
     */
    public void setDataChgId(String dataChgId) {
        this.dataChgId = dataChgId;
    }
}
