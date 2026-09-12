package com.viettel.delivery.repository;

import com.viettel.delivery.entity.FunctionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface FunctionRepository extends JpaRepository<FunctionEntity, Long> {

    List<FunctionEntity> findAllByIsDeletedFalseOrderByModuleAscFunctionCodeAsc();

    Set<FunctionEntity> findAllByIdInAndIsDeletedFalse(Set<Long> ids);
}
