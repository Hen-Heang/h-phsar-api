package com.henheang.hphsar.service;

import com.henheang.hphsar.model.store.Store;
import com.henheang.hphsar.model.store.StoreRequest;

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
