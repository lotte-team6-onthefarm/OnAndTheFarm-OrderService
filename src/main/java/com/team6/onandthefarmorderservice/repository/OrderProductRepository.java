package com.team6.onandthefarmorderservice.repository;


import com.team6.onandthefarmorderservice.entity.OrderProduct;
import com.team6.onandthefarmorderservice.entity.Orders;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderProductRepository extends CrudRepository<OrderProduct,Long> {
    List<OrderProduct> findByOrders(Orders orders);

    @Query("select p from OrderProduct p where p.orders.ordersId=:ordersId and p.orderProductStatus='deliveryCompleted'")
    List<OrderProduct> findByOrdersAndStatus(@Param("ordersId") Long ordersId);

    List<OrderProduct> findByOrdersAndOrderProductStatus(Orders orders, String status);

    List<OrderProduct> findByOrdersAndSellerIdAndOrderProductStatus(Orders orders, Long sellerId, String status);

    List<OrderProduct> findBySellerIdAndOrderProductStatusAndOrderProductDateBetween(Long sellerId, String orderStatus,String startDate,String endDate);

    List<OrderProduct> findBySellerId(Long sellerId);

    List<OrderProduct> findBySellerIdAndOrderProductDateBetween(Long sellerId, String startDate, String endDate);

    List<OrderProduct> findBySellerIdAndOrderProductDateStartingWith(Long sellerId, String date);

    List<OrderProduct> findByProductId(Long productId);

    List<OrderProduct> findOrderProductsByOrders(Orders orders);

    @Query("select o from OrderProduct o where o.sellerId=:sellerId and o.orderProductStatus='refundRequest'")
    List<OrderProduct> findRequestRefundOrderProduct(@Param("sellerId") Long sellerId);

    @Query("select o from OrderProduct o where o.sellerId=:sellerId and o.orderProductStatus='canceled'")
    List<OrderProduct> findCancelOrdersOrderProduct(@Param("sellerId") Long sellerId);

    @Query("select o from OrderProduct o where o.sellerId=:sellerId and o.orderProductStatus='activated'")
    List<OrderProduct> findBeforeDeliveryOrders(@Param("sellerId") Long sellerId);

    @Query("select o from OrderProduct o where o.sellerId=:sellerId and o.orderProductStatus='deliveryProgress'")
    List<OrderProduct> findDeliveringOrders(@Param("sellerId") Long sellerId);

    @Query("select o from OrderProduct o where o.sellerId=:sellerId and o.orderProductStatus='deliveryCompleted'")
    List<OrderProduct> findDeliverCompleteOrders(@Param("sellerId") Long sellerId);

    Integer countBySellerIdAndOrderProductStatus(Long sellerId,String status);
}
