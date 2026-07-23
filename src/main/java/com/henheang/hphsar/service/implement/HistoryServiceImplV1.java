package com.henheang.hphsar.service.implement;

import com.henheang.hphsar.exception.BadRequestException;
import com.henheang.hphsar.exception.ConflictException;
import com.henheang.hphsar.exception.InternalServerErrorException;
import com.henheang.hphsar.exception.NotFoundException;
import com.henheang.hphsar.model.appUser.AppUser;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@Service
public class HistoryServiceImplV1 implements HistoryService {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final HistoryRepository historyRepository;
    private final BuyerOrderRepository buyerOrderRepository;
    private final StoreRepository storeRepository;
    private final BuyerProfileRepository buyerProfileRepository;
    private final OrderStatusService orderStatusService;

    public HistoryServiceImplV1(HistoryRepository historyRepository, BuyerOrderRepository buyerOrderRepository, StoreRepository storeRepository, BuyerProfileRepository buyerProfileRepository, OrderStatusService orderStatusService) {
        this.historyRepository = historyRepository;
        this.buyerOrderRepository = buyerOrderRepository;
        this.storeRepository = storeRepository;
        this.buyerProfileRepository = buyerProfileRepository;
        this.orderStatusService = orderStatusService;
    }

    @Override
    public List<ImportHistory> getProductImportHistory(String sort, Integer pageNumber, Integer pageSize) throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer storeId = storeRepository.getStoreIdByUserId(appUser.getId());
        // check sort spelling
        if (!(sort.toLowerCase().equals("asc") || sort.toLowerCase().equals("desc") || sort.isEmpty())) {
            throw new BadRequestException("Field 'sort' is invalid. Please input either 'ASC' or 'DESC'. Case sensitive not required.");
        }
        // validate page number and size
        if (pageNumber <= 0 || pageSize <= 0) {
            throw new NotFoundException("Page number and size should be higher than 0.");
        }
        Integer totalPage = findTotalImportPage(pageSize);
        List<ImportHistory> histories = historyRepository.getImportHistory(sort, pageNumber, pageSize, storeId);
        if (totalPage < pageSize * pageNumber && histories.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalPage);
        }
        for (ImportHistory history : histories) {
            history.setDate(formatter.format(formatter.parse(history.getDate())));
        }
        return histories;
    }

    @Override
    public List<OrderDetailHistory> getOrderHistory(String sort, Integer pageNumber, Integer pageSize) throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        if (storeRepository.checkStoreIfCreated(currentUserId) == 0) {
            throw new NotFoundException("User have not created store. Please create store to proceed with category.");
        }
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        Integer totalOrderHistory = historyRepository.findTotalOrderHistory(storeId);
        List<OrderDetailHistory> orderDetails = historyRepository.getOrderHistory(sort, pageNumber, pageSize, storeId);
        if (totalOrderHistory < pageSize * pageNumber && orderDetails.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalOrderHistory);
        }
        for (OrderDetailHistory orderDetailHistory : orderDetails) {
            orderDetailHistory.getOrder().setDate(formatter.format(formatter.parse(orderDetailHistory.getOrder().getDate())));
        }
        return orderDetails;
    }

    @Override
    public List<OrderDetailHistory> getBuyerOrderHistory(String sort, Integer pageNumber, Integer pageSize) throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        if (!buyerProfileRepository.checkIfBuyerProfileIsAlreadyCreated(currentUserId)) {
            throw new NotFoundException("User have not created profile yet. Please create user profile to use this feature.");
        }
        Integer totalOrderHistory = historyRepository.findTotalBuyerOrder(currentUserId);
        List<OrderDetailHistory> orderDetails = historyRepository.getBuyerOrderHistory(sort, pageNumber, pageSize, currentUserId);
        if (totalOrderHistory < pageSize * pageNumber && orderDetails.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalOrderHistory);
        }
        for (OrderDetailHistory orderDetailHistory : orderDetails) {
            orderDetailHistory.getOrder().setDate(formatter.format(formatter.parse(orderDetailHistory.getOrder().getDate())));
        }
        return orderDetails;
    }

    @Override
    public List<OrderDetailHistory> getDraftHistory(String sort, Integer pageNumber, Integer pageSize) {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        if (!buyerProfileRepository.checkIfBuyerProfileIsAlreadyCreated(currentUserId)) {
            throw new NotFoundException("User have not created profile yet. Please create user profile to use this feature.");
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
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        // Ownership-scoped existence check: a draft that exists but belongs to
        // another buyer must look identical to a draft that doesn't exist at all.
        if (!historyRepository.existsDraftByIdAndBuyerId(id, currentUserId)) {
            throw new NotFoundException("Draft not found.");
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
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        // Ownership-scoped existence check: a draft that exists but belongs to
        // another buyer must look identical to a draft that doesn't exist at all.
        if (!historyRepository.existsDraftByIdAndBuyerId(id, currentUserId)) {
            throw new NotFoundException("Draft not found.");
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
        orderDetailHistory.getOrder().setDate(formatter.format(formatter.parse(orderDetailHistory.getOrder().getDate())));
        return orderDetailHistory;
    }

    @Override
    public Integer findTotalOrderPage(Integer pageSize) {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        if (storeRepository.checkStoreIfCreated(currentUserId) == 0) {
            throw new NotFoundException("User have not created store. Please create store to proceed with category.");
        }
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        Integer totalImportDetail = historyRepository.findTotalOrderHistory(storeId);
        int totalPage;
        if (totalImportDetail % pageSize == 0) {
            totalPage = totalImportDetail / pageSize;
        } else {
            totalPage = (Integer) (totalImportDetail / pageSize) + 1;
        }
        return totalPage;
    }

    @Override
    public Integer findTotalImportPage(Integer pageSize) {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        if (storeRepository.checkStoreIfCreated(currentUserId) == 0) {
            throw new NotFoundException("User have not created store. Please create store to proceed with category.");
        }
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        Integer totalImportDetail = historyRepository.findTotalImportDetail(storeId);
        int totalPage;
        if (totalImportDetail % pageSize == 0) {
            totalPage = totalImportDetail / pageSize;
        } else {
            totalPage = (Integer) (totalImportDetail / pageSize) + 1;
        }
        return totalPage;
    }

    @Override
    public Integer findBuyerTotalOrderPage(Integer pageSize) {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
//        if (storeRepository.checkStoreIfCreated(currentUserId) == 0) {
//            throw new NotFoundException("User have not created store. Please create store to proceed with category.");
//        }
        Integer totalBuyerOrder = historyRepository.findTotalBuyerOrder(currentUserId);
        int totalPage;
        if (totalBuyerOrder % pageSize == 0) {
            totalPage = totalBuyerOrder / pageSize;
        } else {
            totalPage = (Integer) (totalBuyerOrder / pageSize) + 1;
        }
        return totalPage;
    }

    @Override
    public Integer findBuyerTotalDraftPage(Integer pageSize) {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer totalBuyerDraftPage = historyRepository.findTotalBuyerDraft(currentUserId);
        int totalPage;
        if (totalBuyerDraftPage % pageSize == 0) {
            totalPage = totalBuyerDraftPage / pageSize;
        } else {
            totalPage = (Integer) (totalBuyerDraftPage / pageSize) + 1;
        }
        return totalPage;
    }
}
