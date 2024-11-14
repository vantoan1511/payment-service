package com.shopbee.paymentservice.repository;

import com.shopbee.paymentservice.entity.Transaction;
import com.shopbee.paymentservice.enums.TransactionStatus;
import com.shopbee.paymentservice.shared.page.PageRequest;
import com.shopbee.paymentservice.shared.sort.SortCriteria;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class TransactionRepository implements PanacheRepository<Transaction> {

    public List<Transaction> findByCriteria(PageRequest pageRequest, SortCriteria sortCriteria) {
        Sort.Direction direction = sortCriteria.isAscending() ? Sort.Direction.Ascending : Sort.Direction.Descending;
        Sort sort = Sort.by(sortCriteria.getSortBy().getColumn(), direction);
        return findAll(sort).page(pageRequest.getPage() - 1, pageRequest.getSize()).list();
    }

    public Optional<Transaction> findByOrderId(Long orderId) {
        return find("orderId", orderId).stream().findFirst();
    }

    public Optional<Transaction> findByReferenceId(String referenceId) {
        return find("referenceId", referenceId).stream().findFirst();
    }

    public List<Transaction> findSuccessByYear(int year) {
        return find("status = ?1 and extract(year from createdAt) = ?2", TransactionStatus.SUCCESS, year).list();
    }

}
