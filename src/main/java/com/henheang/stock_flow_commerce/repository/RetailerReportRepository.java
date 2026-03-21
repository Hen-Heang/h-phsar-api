package com.henheang.stock_flow_commerce.repository;

import com.henheang.stock_flow_commerce.model.retailer.report.CategoryNameAndTotalOfQty;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.sql.Timestamp;
import java.util.List;

@Mapper
public interface RetailerReportRepository {

    @Select("""
            SELECT count(*) as totalOrder FROM tb_order
            Where retailer_account_id = #{currentUserId} AND (EXTRACT(YEAR FROM created_date) = #{year} AND EXTRACT(MONTH FROM created_date)=#{month})
            """)
    Integer getTotalMonthlyOrderByCurrentMonth(Integer currentUserId, Integer year, Integer month);

    @Select("""
            SELECT count(*) FROM tb_order
            WHERE retailer_account_id=#{currentUserId} AND status_id=#{statusId} AND (EXTRACT(YEAR FROM created_date) = #{year} AND EXTRACT(MONTH FROM created_date)=#{month})
            """)
    Integer getTotalRejectedAndAccepted(Integer currentUserId, Integer statusId, Integer year, Integer month);

    @Select("""
            SELECT count(*)
            FROM tb_order
            WHERE retailer_account_id=#{currentUserId} AND (EXTRACT(YEAR FROM created_date) = #{year} AND EXTRACT(MONTH FROM created_date)=#{month})
            """)
    Integer getTotalOrderFromDifferentYear(Integer currentUserId,Integer year, Integer month);

    @Select("""
            SELECT count(*)
            FROM tb_order
            WHERE retailer_account_id=#{currentUserId} AND status_id =#{statusId} AND (EXTRACT(YEAR FROM created_date) = #{year} AND EXTRACT(MONTH FROM created_date)=#{month})
            """)
    Integer getTotalAcceptedAndRejectedFromDifferentYear(Integer currentUserId, Integer statusId,Integer year, Integer month );

    @Select("""
            SELECT sum(qty) as totalQuantityOrder FROM tb_order_detail TOD INNER JOIN tb_order T ON T.id = TOD.order_id
            Where retailer_account_id = #{currentUserId} AND status_id =#{statusId} AND (EXTRACT(YEAR FROM created_date) = #{year} AND EXTRACT(MONTH FROM created_date)=#{month})
            """)
    Integer getTotalQuantityOrder(Integer currentUserId, Integer statusId, Integer year, Integer month);


    @Select("""
            SELECT DISTINCT TC.name
            FROM tb_order_detail
            INNER JOIN tb_order T ON T.id = tb_order_detail.order_id
            INNER JOIN tb_store_product_detail SPD ON SPD.id = tb_order_detail.store_product_id
            INNER JOIN tb_category TC ON TC.id = SPD.category_id
            Where retailer_account_id = #{currentUserId}  AND status_id=#{statusId} AND (EXTRACT(YEAR FROM T.created_date) = #{year} AND EXTRACT(MONTH FROM T.created_date)=#{month})
            ORDER BY TC.name DESC
            """)
    List<String> getCategoryNameOrder(Integer currentUserId, Integer statusId, Integer year, Integer month);


    @Select("""
            SELECT DISTINCT store_id
            FROM tb_order
            WHERE retailer_account_id=#{currentUserId} AND status_id = #{statusId} AND (EXTRACT(YEAR FROM created_date) = #{year} AND EXTRACT(MONTH FROM created_date)=#{month});
                
                        """)
    List<Integer> getPurchasedShopOrdered(Integer currentUserId, Integer statusId, Integer year, Integer month);


    @Select("""
            SELECT sum(qty*unit_price)
            FROM  tb_order_detail
            INNER JOIN tb_order T ON T.id = tb_order_detail.order_id
            WHERE T.retailer_account_id=#{currentUserId} AND (EXTRACT(MONTH FROM created_date) = #{month} AND EXTRACT(YEAR FROM created_date)=#{year});
                      
            """)
    Integer getTotalExpense(Integer currentUserId, Integer statusId, Integer year, Integer month);


    @Select("""
            SELECT count(*)
            from tb_rating_detail
            where retailer_account_id=#{currentUserId} AND (EXTRACT(YEAR FROM created_date) = #{year} AND EXTRACT(MONTH FROM created_date)=#{month})
            """)
    Integer getTotalRatingStore(Integer currentUserId, Integer year, Integer month);


    @Select("""
            SELECT coalesce(sum(qty*unit_price))
            FROM  tb_order_detail
            INNER JOIN tb_order T ON T.id = tb_order_detail.order_id
            WHERE T.retailer_account_id=#{currentUserId} AND T.status_id=#{statusId}  AND (EXTRACT(YEAR FROM T.created_date) = #{year})
            """)
    Double getTotalYearlyExpense(Integer currentUserId, Integer statusId, Integer year);


    @Select("""
            SELECT coalesce(sum(qty))
            from tb_order_detail tod
            INNER JOIN tb_order t on t.id = tod.order_id
            Where retailer_account_id=#{currentUserId} AND status_id=#{statusId}
            AND (EXTRACT(YEAR FROM t.created_date)=#{year} AND EXTRACT(MONTH FROM t.created_date)= #{month});
            """)
    Integer getTotalQuantityInDifferenceYear(Integer currentUserId, Integer statusId, Integer year, Integer month);

    @Select("""
            SELECT DISTINCT TC.name
            FROM tb_order_detail
                     INNER JOIN tb_order T ON T.id = tb_order_detail.order_id
                     INNER JOIN tb_store_product_detail SPD ON SPD.id = tb_order_detail.store_product_id
                     INNER JOIN tb_category TC ON TC.id = SPD.category_id
            Where retailer_account_id = #{currentUserId}  AND status_id=#{statusId}
            AND EXTRACT(YEAR FROM TC.created_date) = #{year} AND EXTRACT(MONTH FROM TC.created_date) = #{month}
            ORDER BY TC.name DESC
            """)
    List<String> getCategoryNameOrderIndDifferentYear(Integer currentUserId, Integer statusId, Integer year, Integer month);


    @Select("""
            SELECT DISTINCT store_id
            FROM tb_order
            WHERE retailer_account_id=#{currentUserId} AND status_id = #{statusId} AND (EXTRACT(YEAR FROM created_date) = #{year} AND EXTRACT(MONTH FROM created_date)=#{month});
            """)
    List<Integer> getTotalPurchasedShopDifferent(Integer currentUserId, Integer statusId, Integer year, Integer month);


    @Select("""
            SELECT coalesce(sum(qty*unit_price))
            FROM  tb_order_detail
            INNER JOIN tb_order T ON T.id = tb_order_detail.order_id
            WHERE T.retailer_account_id=#{currentUserId} AND T.status_id= #{statusId} AND (EXTRACT(YEAR FROM T.created_date) = #{year} AND EXTRACT(MONTH FROM T.created_date)=#{month});
            """)
    Integer getTotalExpenseInDifferentYear(Integer currentUserId, Integer statusId, Integer year, Integer month);

    @Select("""
            SELECT count(*)
            from tb_rating_detail
            where retailer_account_id=#{currentUserId} AND (EXTRACT(YEAR FROM created_date) = #{year} AND EXTRACT(MONTH FROM created_date)=#{month});
            """)
    Integer getRatingInDifferentYear(Integer currentUserId, Integer statusId, Integer year, Integer month);

    @Select("""
            SELECT sum(qty*unit_price)
            FROM  tb_order_detail
            INNER JOIN tb_order T ON T.id = tb_order_detail.order_id
            WHERE T.retailer_account_id=#{currentUserId} AND T.status_id=#{statusId}
            AND (EXTRACT(YEAR FROM T.created_date) = #{year});
            """)
    Double getTotalYearlyInDifferentYear(Integer currentUserId, Integer statusId, Integer year);


    @Select("""
            SELECT sum(tb_order_detail.qty)
            FROM tb_order_detail
                     INNER JOIN tb_order T ON T.id = tb_order_detail.order_id
                     INNER JOIN tb_store_product_detail SPD ON SPD.id = tb_order_detail.store_product_id
                     INNER JOIN tb_category TC ON TC.id = SPD.category_id
            Where retailer_account_id = #{currentUserId}
              AND status_id = #{statusId}
              AND (EXTRACT(YEAR FROM T.created_date) = #{year} AND EXTRACT(MONTH FROM T.created_date) = #{month})
            group by TC.name
            order by TC.name DESC ;
            """)
    List<Integer> getTotalQtyInEachCategory(Integer currentUserId, Integer statusId, Integer year, Integer month);


    @Select("""
            SELECT sum(tb_order_detail.qty) as totalItem ,TC.name as categoryName
            FROM tb_order_detail
            INNER JOIN tb_order T ON T.id = tb_order_detail.order_id
            INNER JOIN tb_store_product_detail SPD ON SPD.id = tb_order_detail.store_product_id
            INNER JOIN tb_category TC ON TC.id = SPD.category_id
            Where retailer_account_id = #{currentUserId}
            AND status_id = #{statusId}
            AND (EXTRACT(YEAR FROM T.created_date) = #{year} AND EXTRACT(MONTH FROM T.created_date) = #{month})
            group by TC.name
            order by TC.name DESC ;
            """)
    List<CategoryNameAndTotalOfQty> getCategoryNameAndTotalItem(Integer currentUserId, Integer statusId, Integer year, Integer month);
}

