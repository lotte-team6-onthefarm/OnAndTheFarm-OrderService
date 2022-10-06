package com.team6.onandthefarmorderservice.repository;

import com.team6.onandthefarmorderservice.entity.Payment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends CrudRepository<Payment,Long> {
}
