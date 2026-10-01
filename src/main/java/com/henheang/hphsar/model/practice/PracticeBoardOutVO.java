package com.henheang.hphsar.model.practice;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * P1 practice: OLV-style query result VO for one practice board row.
 * <p>
 * OLV result VOs implement {@link Serializable} and have hand-written getters and setters (no Lombok).
 *
 * @author Hen-Heang
 * @since 2026.09.28
 * @version 1.0
 */
public class PracticeBoardOutVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Board serial number. */
    private Long bbsSn;

    /** Post title. */
    private String bbsTtlNm;

    /** Post content. */
    private String bbsCn;

    /** View count. */
    private int bbsInqCnt;

    /** Published flag ('Y' or 'N'). */
    private String pstgYn;

    /** Registering user ID. */
    private String dataRegId;

    /** Registration time. */
    private LocalDateTime dataRegDt;

    /** Last modifying user ID. */
    private String dataChgId;

    /** Last modification time. */
    private LocalDateTime dataChgDt;

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
     * Returns the view count.
     *
     * @return view count
     */
    public int getBbsInqCnt() {
        return bbsInqCnt;
    }

    /**
     * Sets the view count.
     *
     * @param bbsInqCnt view count
     */
    public void setBbsInqCnt(int bbsInqCnt) {
        this.bbsInqCnt = bbsInqCnt;
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
     * Returns the registration time.
     *
     * @return registration time
     */
    public LocalDateTime getDataRegDt() {
        return dataRegDt;
    }

    /**
     * Sets the registration time.
     *
     * @param dataRegDt registration time
     */
    public void setDataRegDt(LocalDateTime dataRegDt) {
        this.dataRegDt = dataRegDt;
    }

    /**
     * Returns the last modifying user ID.
     *
     * @return last modifying user ID, or {@code null} if never modified
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

    /**
     * Returns the last modification time.
     *
     * @return last modification time, or {@code null} if never modified
     */
    public LocalDateTime getDataChgDt() {
        return dataChgDt;
    }

    /**
     * Sets the last modification time.
     *
     * @param dataChgDt last modification time
     */
    public void setDataChgDt(LocalDateTime dataChgDt) {
        this.dataChgDt = dataChgDt;
    }
}
