package com.henheang.stock_flow_commerce.service;

import com.henheang.stock_flow_commerce.model.store.Store;
import com.henheang.stock_flow_commerce.model.store.StoreRequest;

import java.text.ParseException;
import java.util.List;

public interface DistributorStoreService {
    Store createNewStore(StoreRequest storeRequest, Integer currentUserId) throws ParseException;

    Store getUserStore(Integer currentUserId) throws ParseException;

    Store editAllFieldUserStore(Integer currentUserId, StoreRequest storeRequest) throws ParseException;

    String deleteUserStore(Integer currentUserId);

    String disableStore(Integer currentUserId);

    String enableStore(Integer currentUserId);
}
