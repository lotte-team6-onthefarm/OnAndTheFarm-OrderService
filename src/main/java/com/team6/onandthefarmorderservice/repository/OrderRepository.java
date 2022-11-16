package com.team6.onandthefarmorderservice.repository;


import com.team6.onandthefarmorderservice.entity.Orders;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends CrudRepository<Orders,Long> {
    List<Orders> findByOrdersStatusAndOrdersDateBetween(String orderStatus, String startDate, String endDate);

    List<Orders> findByOrdersSellerId(Long sellerId);

    Orders findByOrdersSerial(String orderSerial);

    List<Orders> findByOrdersSellerIdAndOrdersStatusAndOrdersDateBetween(Long sellerId,String ordersStatus,String startDate,String endDate);

    List<Orders> findByUserId(Long userId);

    @Query("select o from Orders o where o.userId=:userId and o.ordersStatus = 'deliveryCompleted'")
    List<Orders> findWithOrderAndOrdersStatus(@Param("userId") Long userId);
}
