package com.haifachagwey.springmvc.repository;

import com.haifachagwey.springmvc.models.HistoryGrade;
import com.haifachagwey.springmvc.models.MathGrade;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryGradeDao extends CrudRepository<HistoryGrade, Integer> {

    Iterable<HistoryGrade> findGradeByStudentId(int id);

    void deleteByStudentId(int id);
}
