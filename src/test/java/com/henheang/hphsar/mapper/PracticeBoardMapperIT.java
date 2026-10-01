package com.henheang.hphsar.mapper;

import com.henheang.hphsar.AbstractIntegrationTest;
import com.henheang.hphsar.model.practice.PracticeBoardInVO;
import com.henheang.hphsar.model.practice.PracticeBoardOutVO;
import com.henheang.hphsar.repository.practice.PracticeBoardMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * P1 practice: real-PostgreSQL coverage for PracticeBoardMapper.xml.
 * <p>
 * The table comes from a test-only script, so the application schema is untouched. The container is
 * shared across all *IT classes (no per-test rollback), so every test tags its rows with a unique
 * keyword and never assumes it sees the only rows in the table.
 * These tests are RED until the TODO statements in the mapper XML are written.
 */
@Sql(scripts = "classpath:practice/practice_board_schema.sql")
class PracticeBoardMapperIT extends AbstractIntegrationTest {

    @Autowired
    private PracticeBoardMapper practiceBoardMapper;

    private String uniqueKeyword() {
        return "p1-" + System.nanoTime();
    }

    private PracticeBoardInVO newPost(String title) {
        PracticeBoardInVO inVO = new PracticeBoardInVO();
        inVO.setBbsTtlNm(title);
        inVO.setBbsCn("content of " + title);
        inVO.setDataRegId("tester");
        return inVO;
    }

    private long insertPost(String title) {
        PracticeBoardInVO inVO = newPost(title);
        practiceBoardMapper.insert(inVO);
        return inVO.getBbsSn();
    }

    private PracticeBoardInVO bySn(long bbsSn) {
        PracticeBoardInVO inVO = new PracticeBoardInVO();
        inVO.setBbsSn(bbsSn);
        inVO.setDataChgId("editor");
        return inVO;
    }

    private PracticeBoardInVO search(String keyword, int recordCountPerPage, int firstIndex) {
        PracticeBoardInVO inVO = new PracticeBoardInVO();
        inVO.setSearchKeyword(keyword);
        inVO.setRecordCountPerPage(recordCountPerPage);
        inVO.setFirstIndex(firstIndex);
        return inVO;
    }

    @Test
    void insert_thenSelectDetail_returnsRowWithDefaultsAndAudit() {
        PracticeBoardInVO inVO = newPost(uniqueKeyword());

        int affected = practiceBoardMapper.insert(inVO);
        PracticeBoardOutVO found = practiceBoardMapper.selectDetail(bySn(inVO.getBbsSn()));

        assertThat(affected).isEqualTo(1);
        assertThat(inVO.getBbsSn()).isNotNull();
        assertThat(found.getBbsTtlNm()).isEqualTo(inVO.getBbsTtlNm());
        assertThat(found.getBbsInqCnt()).isZero();
        assertThat(found.getPstgYn()).isEqualTo("Y");
        assertThat(found.getDataRegId()).isEqualTo("tester");
        assertThat(found.getDataRegDt()).isNotNull();
        assertThat(found.getDataChgDt()).isNull();
    }

    @Test
    void selectList_returnsRequestedPageNewestFirst() {
        String keyword = uniqueKeyword();
        long first = insertPost(keyword + "-a");
        long second = insertPost(keyword + "-b");
        long third = insertPost(keyword + "-c");

        List<PracticeBoardOutVO> page1 = practiceBoardMapper.selectList(search(keyword, 2, 0));
        List<PracticeBoardOutVO> page2 = practiceBoardMapper.selectList(search(keyword, 2, 2));

        assertThat(page1).extracting(PracticeBoardOutVO::getBbsSn).containsExactly(third, second);
        assertThat(page2).extracting(PracticeBoardOutVO::getBbsSn).containsExactly(first);
    }

    @Test
    void selectListTotCnt_countsOnlyMatchingActivePosts() {
        String keyword = uniqueKeyword();
        insertPost(keyword + "-a");
        long deleted = insertPost(keyword + "-b");
        insertPost(uniqueKeyword());
        practiceBoardMapper.delete(bySn(deleted));

        assertThat(practiceBoardMapper.selectListTotCnt(search(keyword, 10, 0))).isEqualTo(1);
    }

    @Test
    void selectList_keywordWithSqlCharacters_isTreatedAsPlainText() {
        String keyword = uniqueKeyword();
        insertPost(keyword);

        List<PracticeBoardOutVO> result = practiceBoardMapper.selectList(search("' OR '1'='1", 10, 0));

        assertThat(result).isEmpty();
    }

    @Test
    void updateInqCnt_increasesViewCountByOne() {
        long bbsSn = insertPost(uniqueKeyword());

        practiceBoardMapper.updateInqCnt(bySn(bbsSn));
        practiceBoardMapper.updateInqCnt(bySn(bbsSn));

        assertThat(practiceBoardMapper.selectDetail(bySn(bbsSn)).getBbsInqCnt()).isEqualTo(2);
    }

    @Test
    void update_changesValuesAndSetsChangeAudit() {
        long bbsSn = insertPost(uniqueKeyword());
        PracticeBoardInVO change = bySn(bbsSn);
        change.setBbsTtlNm("changed title");
        change.setBbsCn("changed content");
        change.setPstgYn("N");

        int affected = practiceBoardMapper.update(change);
        PracticeBoardOutVO found = practiceBoardMapper.selectDetail(bySn(bbsSn));

        assertThat(affected).isEqualTo(1);
        assertThat(found.getBbsTtlNm()).isEqualTo("changed title");
        assertThat(found.getPstgYn()).isEqualTo("N");
        assertThat(found.getDataChgId()).isEqualTo("editor");
        assertThat(found.getDataChgDt()).isNotNull();
    }

    @Test
    void delete_softDeletesOnce_andHidesPostFromDetail() {
        long bbsSn = insertPost(uniqueKeyword());

        int firstDelete = practiceBoardMapper.delete(bySn(bbsSn));
        int secondDelete = practiceBoardMapper.delete(bySn(bbsSn));

        assertThat(firstDelete).isEqualTo(1);
        assertThat(secondDelete).as("deleting an already-deleted post must affect 0 rows").isZero();
        assertThat(practiceBoardMapper.selectDetail(bySn(bbsSn))).isNull();
        assertThat(practiceBoardMapper.update(bySn(bbsSn))).as("a deleted post must not be updatable").isZero();
    }
}
