package com.henheang.hphsar.service.implement;

import com.henheang.hphsar.exception.InternalServerErrorException;
import com.henheang.hphsar.exception.NotFoundException;
import com.henheang.hphsar.model.practice.PracticeBoardInVO;
import com.henheang.hphsar.model.practice.PracticeBoardOutVO;
import com.henheang.hphsar.repository.practice.PracticeBoardMapper;
import com.henheang.hphsar.service.PracticeBoardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * P1 practice: implementation of {@link PracticeBoardService}.
 * <p>
 * Uses a handwritten constructor instead of Lombok. OLV uses field {@code @Autowired}, but h-phsar
 * requires constructor injection, and this form works in both.
 * <p>
 * Transactions: OLV wraps every {@code *Impl} method through AOP by method name, so it rarely writes
 * {@code @Transactional}. h-phsar has no such AOP, so a method that writes more than once must declare
 * {@code @Transactional} itself.
 *
 * @author Hen-Heang
 * @since 2026.09.28
 * @version 1.0
 */
@Service
public class PracticeBoardServiceImpl implements PracticeBoardService {

    /** Practice-only message; kept out of the shared {@code ExceptionMessages}. */
    private static final String POST_NOT_FOUND = "Post not found.";

    private final PracticeBoardMapper practiceBoardMapper;

    /**
     * Creates the service.
     *
     * @param practiceBoardMapper board mapper
     */
    public PracticeBoardServiceImpl(PracticeBoardMapper practiceBoardMapper) {
        this.practiceBoardMapper = practiceBoardMapper;
    }

    /** {@inheritDoc} */
    @Override
    public List<PracticeBoardOutVO> selectList(PracticeBoardInVO inVO) {
        int pageIndex = Math.max(inVO.getPageIndex(), 1);
        inVO.setPageIndex(pageIndex);
        inVO.setFirstIndex((pageIndex - 1) * inVO.getRecordCountPerPage());
        return practiceBoardMapper.selectList(inVO);
    }

    /** {@inheritDoc} */
    @Override
    public int selectListTotCnt(PracticeBoardInVO inVO) {
        return practiceBoardMapper.selectListTotCnt(inVO);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Reads first so a missing post never touches the view count. The returned post carries the
     * count as it was before this view.
     */
    @Override
    @Transactional
    public PracticeBoardOutVO selectDetail(long bbsSn) {
        PracticeBoardInVO key = new PracticeBoardInVO();
        key.setBbsSn(bbsSn);

        PracticeBoardOutVO post = practiceBoardMapper.selectDetail(key);
        if (post == null) {
            throw new NotFoundException(POST_NOT_FOUND);
        }
        // 0 rows here means the post was deleted between the read and this update.
        if (practiceBoardMapper.updateInqCnt(key) != 1) {
            throw new NotFoundException(POST_NOT_FOUND);
        }
        return post;
    }

    /** {@inheritDoc} */
    @Override
    public long insert(PracticeBoardInVO inVO) {
        int affected = practiceBoardMapper.insert(inVO);
        if (affected != 1) {
            throw new InternalServerErrorException("Fail to register post.");
        }
        return inVO.getBbsSn();
    }

    /** {@inheritDoc} */
    @Override
    public void update(PracticeBoardInVO inVO) {
        if (practiceBoardMapper.update(inVO) != 1) {
            throw new NotFoundException(POST_NOT_FOUND);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void delete(long bbsSn, String dataChgId) {
        PracticeBoardInVO key = new PracticeBoardInVO();
        key.setBbsSn(bbsSn);
        key.setDataChgId(dataChgId);

        if (practiceBoardMapper.delete(key) != 1) {
            throw new NotFoundException(POST_NOT_FOUND);
        }
    }
}
