package com.henheang.stock_flow_commerce.repository;

import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Select;


@Mapper
public interface DistributorHomepageRepository {
    @Select("""
            select id from tb_store where distributor_account_id=#{currentUserId};
            """)
    Integer getStoreId(Integer currentUserId);


    @Select("""
            select count(*) as newOrder from tb_order where store_id= #{storeId} and status_id=1;
            """)

    Integer getNewOrder(Integer storeId);

        @Select("""
            select count(*) as preparing from tb_order where store_id= #{storeId} and status_id=2;
            """)
    Integer getPreparing(Integer storeId);

    @Select("""
            select count(*) as dispatch from tb_order where store_id= #{storeId} and status_id=3;
            """)
    Integer getDispatch(Integer storeId);

    @Select("""
            select count(*) as confirming from tb_order where store_id= #{storeId} and status_id=4;
            """)
    Integer getConfirming(Integer storeId);

    @Select("""
            select count(*) as completed from tb_order where store_id= #{storeId} and status_id=5;
            """)
    Integer getCompleted(Integer storeId);



    @Select("""
            select sum(qty) from tb_product_import join
                        tb_product_import_detail tpid on tb_product_import.id = tpid.product_import_id
                        where store_id = #{storeId} and date_trunc('month', imported_date) BETWEEN '${startDate}-01' AND '${endDate}-01';
            """)
    Integer getTotalProductImport(Integer storeId, String startDate, String endDate );


    @Select("""
            select count(*) from tb_order inner join
                tb_order_detail tod on tb_order.id = tod.order_id
                            where store_id = #{storeId} and date_trunc('month', created_date) between '${startDate}-01' and '${endDate}-1';
            """)
    Integer getTotalOrderByMonth(Integer storeId, String startDate, String endDate);

    @Select("""
            select sum(qty) from tb_order_detail inner join tb_order t on t.id = tb_order_detail.order_id
            where store_id=#{storeId} and date_trunc('month', created_date) between '${startDate}-01' and '${endDate}-1' ;
            """)
    Integer getTotalProductSold(Integer storeId, String startDate, String endDate);




    @Select("""
              select coalesce(count(tb_order.id),0) as totalMonthlyOrder
                                           FROM tb_order
                                                   JOIN tb_order_detail tod ON tb_order.id = tod.order_id
                                          WHERE  extract(month from created_date) = #{startMonth} and extract(year from created_date)=#{startYear}
                                            and status_id = 5
                                            and store_id = #{storeId}
                                          GROUP BY date_trunc('month', created_date);
            """)
    Integer getTotalOrderEachMonth(Integer storeId,Integer startMonth, Integer startYear);


                            //year
    @Select("""
            select count(*) as totalOrder
                  from tb_order
                           inner join
                       tb_order_detail tod on tb_order.id = tod.order_id
                  where store_id = #{storeId}
                    and date_trunc('year', created_date) between '${startYear}-01-01' and '${endYear}-01-01';
            """)
    Integer getTotalOrderByYear(Integer storeId, Integer startYear, Integer endYear);


    @Select("""
            select sum(qty) as totalProductImport
            from tb_product_import
                     join
                 tb_product_import_detail tpid on tb_product_import.id = tpid.product_import_id
            where store_id = #{storeId}
              and date_trunc('year', imported_date)  between  '${startYear}-01-01' and '${endYear}-01-01';
            """)
    Integer getTotalProductImportByYear(Integer storeId, Integer startYear, Integer endYear);

    @Select("""
            select sum(qty) as totalProductSold
            from tb_order_detail
                     inner join tb_order t on t.id = tb_order_detail.order_id
            where store_id = #{storeId}
              and date_trunc('year', created_date) between  '${startYear}-01-01' and '${endYear}-01-01';
            """)
    Integer getTotalProductSoldByYear(Integer storeId, Integer startYear, Integer endYear);


//    @Select("""
//            select distinct COALESCE(TO_CHAR(DATE_TRUNC('year', created_date), 'YYYY'), '0') AS month
//            from tb_order
//                     inner join tb_order_detail tod on tb_order.id = tod.order_id
//            where store_id = #{storeId}
//              and date_trunc('year', created_date) between '${startDate}-01' and '${endDate}-1';
//            """)
//    List<String> getTotalYear(Integer storeId, String startDate, String endDate);


    @Select("""
            SELECT count(*) as totalMonthlyOrder
            FROM tb_order
                     JOIN tb_order_detail tod ON tb_order.id = tod.order_id
            WHERE extract(year from created_date) = #{startYear}
              and status_id = 5
              and store_id = #{storeId}
            GROUP BY date_trunc('year', created_date);
            """)
    Integer getTotalOrderEachYear(Integer storeId, Integer startYear);
}

