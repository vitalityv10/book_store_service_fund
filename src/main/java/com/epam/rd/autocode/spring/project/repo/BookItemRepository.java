package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.BookItem;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookItemRepository extends JpaRepository<BookItem, UUID> {
    boolean existsByBookIdAndOrder_OrderStatusNotIn(UUID bookId, List<OrderStatus> orderStatuses);

    boolean getBookItemByBook_Id(UUID bookId);

    void deleteAllByBook_Id(UUID bookId);
}
