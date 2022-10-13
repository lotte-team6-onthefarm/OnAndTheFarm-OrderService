package com.team6.onandthefarmorderservice.repository;

import com.team6.onandthefarmorderservice.entity.Refund;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface RefundRepository extends CrudRepository<Refund,Long> {
    Refund findByOrderProductId(Long orderProductId);

    List<Refund> findByUserId(Long userId);
}
