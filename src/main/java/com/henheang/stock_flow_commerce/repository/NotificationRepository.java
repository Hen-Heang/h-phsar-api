package com.henheang.stock_flow_commerce.repository;

import com.henheang.stock_flow_commerce.model.notification.NotificationRetailer;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NotificationRepository {
    @Select("""
            INSERT INTO tb_retailer_notification (id, retailer_account_id, notification_type_id, order_id, summery, title,
                                                  description, is_read, created_date)
            VALUES (DEFAULT, #{retailerId}, #{notificationType}, #{orderId}, #{summery}, #{title}, #{description}, #{isRead}, NOW())
            RETURNING id;
            """)
    Integer createRetailerNotification(Integer retailerId, int notificationType, Integer orderId, String summery, String title, String description, boolean isRead);

    @Select("""
            INSERT INTO tb_distributor_notification (id, distributor_account_id, notification_type_id, order_id, summery, title,
                                                  description, is_read, created_date)
            VALUES (DEFAULT, #{distributorId}, #{notificationType}, #{orderId}, #{summery}, #{title}, #{description}, #{isRead}, NOW())
            RETURNING id;
            """)
    Integer createDistributorNotification(Integer distributorId, int notificationType, Integer orderId, String summery, String title, String description, boolean isRead);

    @Delete("""
            DELETE FROM tb_retailer_notification
            WHERE id = #{check};
            """)
    void deleteNotification(Integer check);

    @Select("""
            SELECT exists(SELECT * FROM tb_retailer_notification WHERE retailer_account_id = #{currentUserId});
            """)
    boolean checkForRetailerNotification(Integer currentUserId);

    @Select("""
            SELECT exists(SELECT * FROM tb_distributor_notification WHERE distributor_account_id = #{currentUserId});
            """)
    boolean checkForDistributorNotification(Integer currentUserId);

//    @Select("""
//            SELECT x.id,
//                   notification_type_id                                                                           AS notificationType,
//                   y.id                                                                                           AS orderId,
//                   (SELECT name FROM tb_store WHERE id = (SELECT store_id FROM tb_order WHERE id = y.id))         AS store,
//                   (SELECT banner_image FROM tb_store WHERE id = (SELECT store_id FROM tb_order WHERE id = y.id)) AS image,
//                   title,
//                   is_read                                                                                        AS seen,
//                   x.created_date                                                                                 AS createdDate
//            FROM tb_retailer_notification x
//                     JOIN tb_store y ON y.id = x.order_id
//            WHERE retailer_account_id = #{currentUserId}
//            ORDER BY x.created_date DESC;
//            """)
//    List<NotificationRetailer> getRetailerUserAllNotification(Integer currentUserId);

    @Select("""
            SELECT id,
                   (SELECT notification_type FROM tb_notification_type WHERE id = notification_type_id) AS notificationType,
                   order_id AS orderId,
                   (SELECT name FROM tb_store WHERE id = (SELECT store_id FROM tb_order WHERE id = tb_retailer_notification.order_id)) AS store,
                   (SELECT banner_image FROM tb_store WHERE id = (SELECT store_id FROM tb_order WHERE id = tb_retailer_notification.order_id)) AS image,
                   title,
                   description,
                   is_read AS seen,
                   created_date AS createdDate
            FROM tb_retailer_notification
            WHERE retailer_account_id = #{currentUserId}
            ORDER BY created_date DESC;
            """)
    List<NotificationRetailer> getRetailerUserAllNotification(Integer currentUserId);

//    @Select("""
//            SELECT x.id,
//                   notification_type_id                                                                           AS notificationType,
//                   y.id                                                                                           AS orderId,
//                   (SELECT name FROM tb_store WHERE id = (SELECT store_id FROM tb_order WHERE id = y.id))         AS store,
//                   (SELECT banner_image FROM tb_store WHERE id = (SELECT store_id FROM tb_order WHERE id = y.id)) AS image,
//                   title,
//                   is_read                                                                                        AS seen,
//                   x.created_date                                                                                 AS createdDate
//            FROM tb_distributor_notification x
//                JOIN tb_store y ON y.id = x.order_id
//            WHERE x.distributor_account_id = #{currentUserId}
//            ORDER BY x.created_date DESC;
//            """)
//    List<NotificationRetailer> getDistributorUserAllNotification(Integer currentUserId);

    @Select("""
            SELECT id,
                   (SELECT notification_type FROM tb_notification_type WHERE id = notification_type_id) AS notificationType,
                   order_id AS orderId,
                   (SELECT name FROM tb_store WHERE id = (SELECT store_id FROM tb_order WHERE id = tb_distributor_notification.order_id)) AS store,
                   (SELECT banner_image FROM tb_store WHERE id = (SELECT store_id FROM tb_order WHERE id = tb_distributor_notification.order_id)) AS image,
                   title,
                   description,
                   is_read AS seen,
                   created_date AS createdDate
            FROM tb_distributor_notification
            WHERE distributor_account_id = #{currentUserId}
            ORDER BY created_date DESC;
            """)
    List<NotificationRetailer> getDistributorUserAllNotification(Integer currentUserId);

    @Select("""
            SELECT exists(SELECT * FROM tb_retailer_notification WHERE retailer_account_id = #{currentUserId} AND id = #{id});
            """)
    boolean checkForRetailerNotificationById(Integer id, Integer currentUserId);

    @Select("""
            SELECT exists(SELECT * FROM tb_distributor_notification WHERE distributor_account_id = #{currentUserId} AND id = #{id});
            """)
    boolean checkForDistributorNotificationById(Integer id, Integer currentUserId);

    @Select("""
            UPDATE tb_retailer_notification
            SET is_read = true
            WHERE retailer_account_id = #{currentUserId}
            AND id = #{id}
            RETURNING 1;
            """)
    String markAsReadRetailer(Integer id, Integer currentUserId);

    @Select("""
            UPDATE tb_distributor_notification
            SET is_read = true
            WHERE distributor_account_id = #{currentUserId}
            AND id = #{id}
            RETURNING 1;
            """)
    String markAsReadDistributor(Integer id, Integer currentUserId);
    @Select("""
            SELECT exists(SELECT * FROM tb_retailer_notification WHERE retailer_account_id = #{currentUserId} AND is_read = false);
            """)
    boolean checkRetailerUnReadNotification(Integer currentUserId);

    @Select("""
            SELECT exists(SELECT * FROM tb_distributor_notification WHERE distributor_account_id = #{currentUserId} AND is_read = false);
            """)
    boolean checkDistributorUnReadNotification(Integer currentUserId);

    @Select("""
            UPDATE tb_retailer_notification
            SET is_read = true
            WHERE retailer_account_id = #{currentUserId}
            RETURNING 1;
            """)
    String markAllNotificationAsReadRetailer(Integer currentUserId);
    @Select("""
            UPDATE tb_distributor_notification
            SET is_read = true
            WHERE distributor_account_id = #{currentUserId}
            RETURNING 1;
            """)
    String markAllNotificationAsReadDistributor(Integer currentUserId);

}
