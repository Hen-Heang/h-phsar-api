package com.henheang.stock_flow_commerce.service.implement;

import com.henheang.stock_flow_commerce.exception.BadRequestException;
import com.henheang.stock_flow_commerce.exception.ConflictException;
import com.henheang.stock_flow_commerce.exception.InternalServerErrorException;
import com.henheang.stock_flow_commerce.exception.NotFoundException;
import com.henheang.stock_flow_commerce.model.appUser.AppUser;
import com.henheang.stock_flow_commerce.model.history.ImportHistory;
import com.henheang.stock_flow_commerce.model.history.OrderDetailHistory;
import com.henheang.stock_flow_commerce.repository.HistoryRepository;
import com.henheang.stock_flow_commerce.repository.OrderRetailerRepository;
import com.henheang.stock_flow_commerce.repository.RetailerProfileRepository;
import com.henheang.stock_flow_commerce.repository.StoreRepository;
import com.henheang.stock_flow_commerce.service.HistoryService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class HistoryServiceImplV1 implements HistoryService {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final HistoryRepository historyRepository;
    private final OrderRetailerRepository orderRetailerRepository;
    private final StoreRepository storeRepository;
    private final RetailerProfileRepository retailerProfileRepository;

    public HistoryServiceImplV1(HistoryRepository historyRepository, OrderRetailerRepository orderRetailerRepository, StoreRepository storeRepository, RetailerProfileRepository retailerProfileRepository) {
        this.historyRepository = historyRepository;
        this.orderRetailerRepository = orderRetailerRepository;
        this.storeRepository = storeRepository;
        this.retailerProfileRepository = retailerProfileRepository;
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
        List<ImportHistory> histories = new ArrayList<>();
        if (sort.equalsIgnoreCase("asc")) {
            histories = historyRepository.getProductImportHistoryASC(pageNumber, pageSize, storeId);
        } else {
            histories = historyRepository.getProductImportHistoryDESC(pageNumber, pageSize, storeId);
        }
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
        List<OrderDetailHistory> orderDetails;
        if (sort.equalsIgnoreCase("asc")) {
            orderDetails = historyRepository.getOrderHistoryASC(pageNumber, pageSize, storeId);
        } else {
            orderDetails = historyRepository.getOrderHistoryDESC(pageNumber, pageSize, storeId);
        }
        if (totalOrderHistory < pageSize * pageNumber && orderDetails.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalOrderHistory);
        }
        for (OrderDetailHistory orderDetailHistory : orderDetails) {
            orderDetailHistory.getOrder().setDate(formatter.format(formatter.parse(orderDetailHistory.getOrder().getDate())));
        }
        return orderDetails;
    }

    @Override
    public List<OrderDetailHistory> getRetailerOrderHistory(String sort, Integer pageNumber, Integer pageSize) throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        if (!retailerProfileRepository.checkIfRetailerProfileIsAlreadyCreated(currentUserId)) {
            throw new NotFoundException("User have not created profile yet. Please create user profile to use this feature.");
        }
        Integer totalOrderHistory = historyRepository.findTotalRetailerOrder(currentUserId);
        List<OrderDetailHistory> orderDetails;
        if (sort.equalsIgnoreCase("asc")) {
            orderDetails = historyRepository.getRetailerOrderHistoryASC(pageNumber, pageSize, currentUserId);
        } else {
            orderDetails = historyRepository.getRetailerOrderHistoryDESC(pageNumber, pageSize, currentUserId);
        }
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
        if (!retailerProfileRepository.checkIfRetailerProfileIsAlreadyCreated(currentUserId)) {
            throw new NotFoundException("User have not created profile yet. Please create user profile to use this feature.");
        }
        Integer totalRetailerDraftPage = historyRepository.findTotalRetailerDraft(currentUserId);
        List<OrderDetailHistory> orderDetails;
        if (sort.equalsIgnoreCase("asc")) {
            orderDetails = historyRepository.getRetailerDraftASC(pageNumber, pageSize, currentUserId);
        } else {
            orderDetails = historyRepository.getRetailerDraftDESC(pageNumber, pageSize, currentUserId);
        }
        if (totalRetailerDraftPage < pageSize * pageNumber && orderDetails.isEmpty()) {
            throw new NotFoundException("Out of range. Total page is " + totalRetailerDraftPage);
        }
        return orderDetails;
    }

    @Override
    public String deleteDraftById(Integer id) {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        if (!historyRepository.checkDraftById(id)) {
            throw new NotFoundException("Draft not found.");
        }
        Integer draftId = historyRepository.deleteDraftById(id);
        if (!Objects.equals(id, draftId)){
            throw new InternalServerErrorException("Fail to delete draft.");
        }
        return "Successfully deleted draft " + id;
    }

    @Override
    public OrderDetailHistory updateDraftById(Integer id) throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        if (!historyRepository.checkDraftById(id)) {
            throw new NotFoundException("Draft not found.");
        }
        Integer storeId = storeRepository.getStoreIdByDraftId(id);
        // check for cart or pending request
        if (orderRetailerRepository.checkForCartOrPending(storeId, currentUserId)) {
            throw new ConflictException("You currently have pending order or cart. Can only order once at a time. Please kindly wait for this order to be accepted.");
        }
        Integer draftId = historyRepository.updateDraftById(id);
        if (!Objects.equals(draftId, id)){
            throw new InternalServerErrorException("Fail to update draft.");
        }
        OrderDetailHistory orderDetailHistory = historyRepository.getDraftHistory(draftId, currentUserId);
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
    public Integer findRetailerTotalOrderPage(Integer pageSize) {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
//        if (storeRepository.checkStoreIfCreated(currentUserId) == 0) {
//            throw new NotFoundException("User have not created store. Please create store to proceed with category.");
//        }
        Integer totalRetailerOrder = historyRepository.findTotalRetailerOrder(currentUserId);
        int totalPage;
        if (totalRetailerOrder % pageSize == 0) {
            totalPage = totalRetailerOrder / pageSize;
        } else {
            totalPage = (Integer) (totalRetailerOrder / pageSize) + 1;
        }
        return totalPage;
    }

    @Override
    public Integer findRetailerTotalDraftPage(Integer pageSize) {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer totalRetailerDraftPage = historyRepository.findTotalRetailerDraft(currentUserId);
        int totalPage;
        if (totalRetailerDraftPage % pageSize == 0) {
            totalPage = totalRetailerDraftPage / pageSize;
        } else {
            totalPage = (Integer) (totalRetailerDraftPage / pageSize) + 1;
        }
        return totalPage;
    }
}
