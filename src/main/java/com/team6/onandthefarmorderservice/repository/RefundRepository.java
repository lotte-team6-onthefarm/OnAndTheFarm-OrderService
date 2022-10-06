package com.team6.onandthefarmorderservice.repository;

import com.team6.onandthefarmorderservice.entity.Refund;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RefundRepository extends CrudRepository<Refund,Long> {
    Refund findByOrderProductId(Long orderProductId);

    List<Refund> findByUserId(Long userId);
}
