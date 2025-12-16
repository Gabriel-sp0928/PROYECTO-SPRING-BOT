package com.industriagafra.repository;

import com.industriagafra.entity.SaleDetail;
import com.industriagafra.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SaleDetailRepository extends JpaRepository<SaleDetail, Long> {
	List<SaleDetail> findBySale(Sale sale);

}
