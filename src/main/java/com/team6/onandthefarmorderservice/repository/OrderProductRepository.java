package com.team6.onandthefarmorderservice.repository;

import com.team6.onandthefarmorderservice.entity.OrderProduct;
import com.team6.onandthefarmorderservice.entity.Orders;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface OrderProductRepository extends CrudRepository<OrderProduct,Long> {
    List<OrderProduct> findByOrders(Orders orders);

    List<OrderProduct> findByOrdersAndSellerIdAndOrderProductStatus(Orders orders, Long sellerId, String status);

    List<OrderProduct> findBySellerIdAndOrderProductStatus(Long sellerId, String orderStatus);

    List<OrderProduct> findBySellerId(Long sellerId);

    List<OrderProduct> findBySellerIdAndOrderProductDateBetween(Long sellerId, String startDate, String endDate);

    List<OrderProduct> findBySellerIdAndOrderProductDateStartingWith(Long sellerId, String date);

    List<OrderProduct> findByProductId(Long productId);

    List<OrderProduct> findOrderProductsByOrders(Orders orders);
}
