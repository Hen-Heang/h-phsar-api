package com.henheang.hphsar.service.implement;

import com.henheang.hphsar.exception.InternalServerErrorException;
import com.henheang.hphsar.exception.NotFoundException;
import com.henheang.hphsar.model.practice.PracticeBoardInVO;
import com.henheang.hphsar.model.practice.PracticeBoardOutVO;
import com.henheang.hphsar.repository.practice.PracticeBoardMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * P1 practice: business rules of {@link PracticeBoardServiceImpl} with a mocked mapper.
 * These tests are RED until the TODOs in the service are implemented.
 */
@ExtendWith(MockitoExtension.class)
class PracticeBoardServiceImplTest {

    @Mock
    private PracticeBoardMapper practiceBoardMapper;

    @InjectMocks
    private PracticeBoardServiceImpl practiceBoardService;

    @Test
    void selectList_calculatesFirstIndexFromPageIndex() {
        PracticeBoardInVO inVO = new PracticeBoardInVO();
        inVO.setPageIndex(3);
        inVO.setRecordCountPerPage(10);
        when(practiceBoardMapper.selectList(any())).thenReturn(List.of());

        practiceBoardService.selectList(inVO);

        ArgumentCaptor<PracticeBoardInVO> captor = ArgumentCaptor.forClass(PracticeBoardInVO.class);
        verify(practiceBoardMapper).selectList(captor.capture());
        assertThat(captor.getValue().getFirstIndex()).isEqualTo(20);
    }

    @Test
    void selectList_pageIndexBelowOne_isTreatedAsFirstPage() {
        PracticeBoardInVO inVO = new PracticeBoardInVO();
        inVO.setPageIndex(0);
        inVO.setRecordCountPerPage(10);
        when(practiceBoardMapper.selectList(any())).thenReturn(List.of());

        practiceBoardService.selectList(inVO);

        ArgumentCaptor<PracticeBoardInVO> captor = ArgumentCaptor.forClass(PracticeBoardInVO.class);
        verify(practiceBoardMapper).selectList(captor.capture());
        assertThat(captor.getValue().getFirstIndex()).isZero();
    }

    @Test
    void selectListTotCnt_returnsMapperCount() {
        when(practiceBoardMapper.selectListTotCnt(any())).thenReturn(7);

        assertThat(practiceBoardService.selectListTotCnt(new PracticeBoardInVO())).isEqualTo(7);
    }

    @Test
    void selectDetail_existingPost_increasesViewCountAndReturnsPost() {
        PracticeBoardOutVO post = new PracticeBoardOutVO();
        post.setBbsSn(5L);
        when(practiceBoardMapper.selectDetail(any())).thenReturn(post);
        when(practiceBoardMapper.updateInqCnt(any())).thenReturn(1);

        PracticeBoardOutVO result = practiceBoardService.selectDetail(5L);

        assertThat(result.getBbsSn()).isEqualTo(5L);
        verify(practiceBoardMapper).updateInqCnt(any());
    }

    @Test
    void selectDetail_missingPost_throwsNotFoundAndDoesNotTouchViewCount() {
        when(practiceBoardMapper.selectDetail(any())).thenReturn(null);

        assertThatThrownBy(() -> practiceBoardService.selectDetail(404L))
                .isInstanceOf(NotFoundException.class);
        verify(practiceBoardMapper, never()).updateInqCnt(any());
    }

    @Test
    void insert_returnsGeneratedSerialNumber() {
        PracticeBoardInVO inVO = new PracticeBoardInVO();
        inVO.setBbsTtlNm("title");
        inVO.setDataRegId("tester");
        doAnswer(invocation -> {
            PracticeBoardInVO arg = invocation.getArgument(0);
            arg.setBbsSn(42L);
            return 1;
        }).when(practiceBoardMapper).insert(any());

        assertThat(practiceBoardService.insert(inVO)).isEqualTo(42L);
    }

    @Test
    void insert_noRowAffected_throwsInternalServerError() {
        PracticeBoardInVO inVO = new PracticeBoardInVO();
        inVO.setBbsTtlNm("title");
        inVO.setDataRegId("tester");
        when(practiceBoardMapper.insert(any())).thenReturn(0);

        assertThatThrownBy(() -> practiceBoardService.insert(inVO))
                .isInstanceOf(InternalServerErrorException.class);
    }

    @Test
    void update_noRowAffected_throwsNotFound() {
        PracticeBoardInVO inVO = new PracticeBoardInVO();
        inVO.setBbsSn(404L);
        when(practiceBoardMapper.update(any())).thenReturn(0);

        assertThatThrownBy(() -> practiceBoardService.update(inVO))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_passesSerialNumberAndModifierToMapper() {
        when(practiceBoardMapper.delete(any())).thenReturn(1);

        practiceBoardService.delete(9L, "tester");

        ArgumentCaptor<PracticeBoardInVO> captor = ArgumentCaptor.forClass(PracticeBoardInVO.class);
        verify(practiceBoardMapper).delete(captor.capture());
        assertThat(captor.getValue().getBbsSn()).isEqualTo(9L);
        assertThat(captor.getValue().getDataChgId()).isEqualTo("tester");
    }

    @Test
    void delete_noRowAffected_throwsNotFound() {
        when(practiceBoardMapper.delete(any())).thenReturn(0);

        assertThatThrownBy(() -> practiceBoardService.delete(404L, "tester"))
                .isInstanceOf(NotFoundException.class);
    }
}
