package com.henheang.hphsar.service.implement;
import com.henheang.hphsar.common.ExceptionMessages;

import com.henheang.hphsar.exception.BadRequestException;
import com.henheang.hphsar.exception.ConflictException;
import com.henheang.hphsar.exception.InternalServerErrorException;
import com.henheang.hphsar.exception.NotFoundException;
import com.henheang.hphsar.model.appUser.Role;
import com.henheang.hphsar.model.history.ImportHistory;
import com.henheang.hphsar.model.history.OrderDetailHistory;
import com.henheang.hphsar.model.order.OrderStatus;
import com.henheang.hphsar.repository.HistoryRepository;
import com.henheang.hphsar.repository.BuyerOrderRepository;
import com.henheang.hphsar.repository.BuyerProfileRepository;
import com.henheang.hphsar.repository.StoreRepository;
import com.henheang.hphsar.service.HistoryService;
import com.henheang.hphsar.service.OrderStatusService;
import com.henheang.hphsar.service.support.CurrentUserProvider;
import com.henheang.hphsar.utils.PaginationUtils;
import com.henheang.hphsar.utils.SortDirectionUtils;
import com.henheang.hphsar.utils.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryServiceImplV1 implements HistoryService {
    private final HistoryRepository historyRepository;
    private final BuyerOrderRepository buyerOrderRepository;
    private final StoreRepository storeRepository;
    private final BuyerProfileRepository buyerProfileRepository;
    private final OrderStatusService orderStatusService;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public List<ImportHistory> getProductImportHistory(String sort, Integer pageNumber, Integer pageSize) throws ParseException {
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserProvider.getCurrentUserId());
        // check sort spelling
        SortDirectionUtils.validate(sort);
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new BadRequestException(ExceptionMessages.PAGE_SIZE_MUST_BE_POSITIVE);
        }
        Integer totalPage = findTotalImportPage(pageSize);
        List<ImportHistory> histories = historyRepository.getImportHistory(sort, pageNumber, pageSize, storeId);
        if (totalPage < pageSize * pageNumber && histories.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalPage);
        }
        for (ImportHistory history : histories) {
            history.setDate(DateTimeUtil.format(DateTimeUtil.parse(history.getDate())));
        }
        return histories;
    }

    @Override
    public List<OrderDetailHistory> getOrderHistory(String sort, Integer pageNumber, Integer pageSize) throws ParseException {
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        if (storeRepository.checkStoreIfCreated(currentUserId) == 0) {
            throw new NotFoundException(ExceptionMessages.USER_HAVE_NOT_CREATED_STORE);
        }
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        Integer totalOrderHistory = historyRepository.findTotalOrderHistory(storeId);
        List<OrderDetailHistory> orderDetails = historyRepository.getOrderHistory(sort, pageNumber, pageSize, storeId);
        if (totalOrderHistory < pageSize * pageNumber && orderDetails.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalOrderHistory);
        }
        for (OrderDetailHistory orderDetailHistory : orderDetails) {
            orderDetailHistory.getOrder().setDate(DateTimeUtil.format(DateTimeUtil.parse(orderDetailHistory.getOrder().getDate())));
        }
        return orderDetails;
    }

    @Override
    public List<OrderDetailHistory> getBuyerOrderHistory(String sort, Integer pageNumber, Integer pageSize) throws ParseException {
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        if (!buyerProfileRepository.checkIfBuyerProfileIsAlreadyCreated(currentUserId)) {
            throw new NotFoundException(ExceptionMessages.USER_HAVE_NOT_CREATED_PROFILE_YET_PLEASE_CREATE);
        }
        Integer totalOrderHistory = historyRepository.findTotalBuyerOrder(currentUserId);
        List<OrderDetailHistory> orderDetails = historyRepository.getBuyerOrderHistory(sort, pageNumber, pageSize, currentUserId);
        if (totalOrderHistory < pageSize * pageNumber && orderDetails.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalOrderHistory);
        }
        for (OrderDetailHistory orderDetailHistory : orderDetails) {
            orderDetailHistory.getOrder().setDate(DateTimeUtil.format(DateTimeUtil.parse(orderDetailHistory.getOrder().getDate())));
        }
        return orderDetails;
    }

    @Override
    public List<OrderDetailHistory> getDraftHistory(String sort, Integer pageNumber, Integer pageSize) {
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        if (!buyerProfileRepository.checkIfBuyerProfileIsAlreadyCreated(currentUserId)) {
            throw new NotFoundException(ExceptionMessages.USER_HAVE_NOT_CREATED_PROFILE_YET_PLEASE_CREATE);
        }
        Integer totalBuyerDraftPage = historyRepository.findTotalBuyerDraft(currentUserId);
        List<OrderDetailHistory> orderDetails = historyRepository.getBuyerDraft(sort, pageNumber, pageSize, currentUserId);
        if (totalBuyerDraftPage < pageSize * pageNumber && orderDetails.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalBuyerDraftPage);
        }
        return orderDetails;
    }

    @Override
    public String deleteDraftById(Integer id) {
        // Buyer id always comes from the authenticated principal, never from
        // the request — the caller cannot supply/override whose draft this is.
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        // Ownership-scoped existence check: a draft that exists but belongs to
        // another buyer must look identical to a draft that doesn't exist at all.
        if (!historyRepository.existsDraftByIdAndBuyerId(id, currentUserId)) {
            throw new NotFoundException(ExceptionMessages.DRAFT_NOT_FOUND);
        }
        int affected = historyRepository.deleteDraftByIdAndBuyerId(id, currentUserId);
        if (affected != 1) {
            throw new InternalServerErrorException("Fail to delete draft.");
        }
        return "Successfully deleted draft " + id;
    }

    @Override
    @Transactional
    public OrderDetailHistory updateDraftById(Integer id) throws ParseException {
        // Buyer id always comes from the authenticated principal, never from
        // the request — the caller cannot supply/override whose draft this is.
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        // Ownership-scoped existence check: a draft that exists but belongs to
        // another buyer must look identical to a draft that doesn't exist at all.
        if (!historyRepository.existsDraftByIdAndBuyerId(id, currentUserId)) {
            throw new NotFoundException(ExceptionMessages.DRAFT_NOT_FOUND);
        }
        Integer storeId = storeRepository.getStoreIdByDraftId(id);
        // Check for another cart/draft already active in this store — excluding
        // this draft itself (Step 3C fix: without excluding it, a draft always
        // matched against its own row here, making draft submission permanently
        // unreachable; see BuyerOrderMapper#checkForCartOrPending).
        if (buyerOrderRepository.checkForCartOrPending(storeId, currentUserId, id)) {
            throw new ConflictException("You currently have pending order or cart. Can only order once at a time. Please kindly wait for this order to be accepted.");
        }
        orderStatusService.transitionOrder(id, OrderStatus.DRAFT, OrderStatus.PENDING, currentUserId, Role.BUYER, "Buyer submitted draft");
        OrderDetailHistory orderDetailHistory = historyRepository.getDraftHistory(id, currentUserId);
        if (orderDetailHistory == null){
            throw new InternalServerErrorException("Fail to fetch data.");
        }
        orderDetailHistory.getOrder().setDate(DateTimeUtil.format(DateTimeUtil.parse(orderDetailHistory.getOrder().getDate())));
        return orderDetailHistory;
    }

    @Override
    public Integer findTotalOrderPage(Integer pageSize) {
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        if (storeRepository.checkStoreIfCreated(currentUserId) == 0) {
            throw new NotFoundException(ExceptionMessages.USER_HAVE_NOT_CREATED_STORE);
        }
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        Integer totalImportDetail = historyRepository.findTotalOrderHistory(storeId);
        return PaginationUtils.totalPages(totalImportDetail, pageSize);
    }

    @Override
    public Integer findTotalImportPage(Integer pageSize) {
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        if (storeRepository.checkStoreIfCreated(currentUserId) == 0) {
            throw new NotFoundException(ExceptionMessages.USER_HAVE_NOT_CREATED_STORE);
        }
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        Integer totalImportDetail = historyRepository.findTotalImportDetail(storeId);
        return PaginationUtils.totalPages(totalImportDetail, pageSize);
    }

    @Override
    public Integer findBuyerTotalOrderElements() {
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        return historyRepository.findTotalBuyerOrder(currentUserId);
    }

    @Override
    public Integer findBuyerTotalDraftElements() {
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        return historyRepository.findTotalBuyerDraft(currentUserId);
    }
}
