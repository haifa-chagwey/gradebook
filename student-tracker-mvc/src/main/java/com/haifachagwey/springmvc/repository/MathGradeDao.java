package com.haifachagwey.springmvc.repository;

import com.haifachagwey.springmvc.models.MathGrade;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MathGradeDao extends CrudRepository<MathGrade, Integer> {

    Iterable<MathGrade> findGradeByStudentId(int id);

    void deleteByStudentId(int id);
}
