package com.example.ecom_project.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ecom_project.model.CustomerOrder;

@Repository
public interface OrderRepo extends JpaRepository<CustomerOrder, Long> {
}
