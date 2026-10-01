package com.henheang.hphsar.service;

import com.henheang.hphsar.model.practice.PracticeBoardInVO;
import com.henheang.hphsar.model.practice.PracticeBoardOutVO;

import java.util.List;

/**
 * P1 practice: OLV-style board service (list, detail, register, update, soft delete).
 *
 * @author Hen-Heang
 * @since 2026.09.28
 * @version 1.0
 */
public interface PracticeBoardService {

    /**
     * Returns one page of active posts. Calculates {@code firstIndex} from {@code pageIndex}.
     *
     * @param inVO search keyword and paging
     * @return rows of the requested page
     */
    List<PracticeBoardOutVO> selectList(PracticeBoardInVO inVO);

    /**
     * Counts active posts matching the search keyword.
     *
     * @param inVO search keyword
     * @return total matching row count
     */
    int selectListTotCnt(PracticeBoardInVO inVO);

    /**
     * Returns one active post and increases its view count.
     *
     * @param bbsSn board serial number
     * @return the post
     * @throws com.henheang.hphsar.exception.NotFoundException when the post is missing or deleted
     */
    PracticeBoardOutVO selectDetail(long bbsSn);

    /**
     * Registers a post.
     *
     * @param inVO title, content, published flag, registering user ID
     * @return generated board serial number
     * @throws com.henheang.hphsar.exception.InternalServerErrorException when the insert does not affect exactly 1 row
     */
    long insert(PracticeBoardInVO inVO);

    /**
     * Updates one active post.
     *
     * @param inVO {@code bbsSn}, new values, modifying user ID
     * @throws com.henheang.hphsar.exception.NotFoundException when the post is missing or deleted
     */
    void update(PracticeBoardInVO inVO);

    /**
     * Soft-deletes one active post.
     *
     * @param bbsSn board serial number
     * @param dataChgId modifying user ID
     * @throws com.henheang.hphsar.exception.NotFoundException when the post is missing or already deleted
     */
    void delete(long bbsSn, String dataChgId);
}
