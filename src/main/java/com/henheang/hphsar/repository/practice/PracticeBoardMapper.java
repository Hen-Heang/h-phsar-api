package com.henheang.hphsar.repository.practice;

import com.henheang.hphsar.model.practice.PracticeBoardInVO;
import com.henheang.hphsar.model.practice.PracticeBoardOutVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * P1 practice: OLV-style MyBatis mapper for {@code tb_practice_board}.
 * <p>
 * The SQL lives in {@code resources/mapper/PracticeBoardMapper.xml} (namespace = this interface's FQCN).
 * Unlike OLV, whose write methods return {@code void}, every mutation here returns the affected-row
 * count so the service can verify it (h-phsar rule).
 *
 * @author Hen-Heang
 * @since 2026.09.28
 * @version 1.0
 */
@Mapper
public interface PracticeBoardMapper {

    /**
     * Selects one page of active (not deleted) posts, newest first, optionally filtered by title keyword.
     *
     * @param inVO search keyword and paging ({@code recordCountPerPage}, {@code firstIndex})
     * @return rows of the requested page
     */
    List<PracticeBoardOutVO> selectList(PracticeBoardInVO inVO);

    /**
     * Counts active posts matching the same filter as {@link #selectList(PracticeBoardInVO)}.
     *
     * @param inVO search keyword
     * @return total matching row count
     */
    int selectListTotCnt(PracticeBoardInVO inVO);

    /**
     * Selects one active post by serial number.
     *
     * @param inVO {@code bbsSn}
     * @return the post, or {@code null} when missing or soft-deleted
     */
    PracticeBoardOutVO selectDetail(PracticeBoardInVO inVO);

    /**
     * Increases the view count of one active post by 1.
     *
     * @param inVO {@code bbsSn}
     * @return affected row count (1, or 0 when missing or soft-deleted)
     */
    int updateInqCnt(PracticeBoardInVO inVO);

    /**
     * Inserts a post and writes the generated key back into {@code inVO.bbsSn}.
     *
     * @param inVO title, content, published flag, registering user ID
     * @return affected row count
     */
    int insert(PracticeBoardInVO inVO);

    /**
     * Updates the title, content, and published flag of one active post and sets the change audit columns.
     *
     * @param inVO {@code bbsSn}, new values, modifying user ID
     * @return affected row count (1, or 0 when missing or soft-deleted)
     */
    int update(PracticeBoardInVO inVO);

    /**
     * Soft-deletes one active post ({@code del_yn = 'Y'}) and sets the change audit columns.
     *
     * @param inVO {@code bbsSn}, modifying user ID
     * @return affected row count (1, or 0 when missing or already deleted)
     */
    int delete(PracticeBoardInVO inVO);
}
