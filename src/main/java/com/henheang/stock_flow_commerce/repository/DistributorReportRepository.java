package com.henheang.stock_flow_commerce.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DistributorReportRepository {
    @Select("""
            SELECT COALESCE(sum(qty * price), 0) AS totalExpense FROM tb_product_import_detail a
                     JOIN tb_product_import b ON b.id = a.product_import_id
            WHERE b.store_id = #{storeId}
            AND imported_date BETWEEN '${startDate}' AND '${endDate}';
            """)
    Double getTotalExpense(String startDate, String endDate, Integer storeId);

    @Select("""
            SELECT COALESCE(sum(qty * unit_price), 0) FROM tb_order a
            JOIN tb_order_detail b ON a.id = b.order_id
            WHERE a.store_id = #{storeId}
            AND created_date BETWEEN '${startDate}' AND '${endDate}';
            """)
    Double getTotalProfit(String startDate, String endDate, Integer storeId);

    @Select("""
            SELECT count(a.id) FROM tb_order a
            JOIN tb_order_detail b ON a.id = b.order_id
            WHERE a.store_id = #{storeId}
            AND created_date BETWEEN '${startDate}' AND '${endDate}';
            """)
    Integer getOrder(String startDate, String endDate, Integer storeId);
    @Select("""
            SELECT date_trunc('month', a.created_date) FROM tb_order a
            JOIN tb_order_detail b ON a.id = b.order_id
            WHERE a.store_id = #{storeId}
            AND created_date BETWEEN '${startDate}' AND '${endDate}';
            """)
    List<String> getPeriod(String startDate, String endDate, Integer storeId);




    @Select("""
            SELECT COALESCE(sum(unit_price)) FROM tb_order_detail
            JOIN tb_order t ON t.id = tb_order_detail.order_id
            WHERE store_id = #{storeId}
            AND created_date BETWEEN '${startDate}' AND '${endDate}';
            """)
    Integer getOrderOfMonth(Integer storeId, String startDate, String endDate);
}


