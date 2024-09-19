package com.haifachagwey.springmvc.repository;

import com.haifachagwey.springmvc.models.MathGrade;
import com.haifachagwey.springmvc.models.ScienceGrade;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScienceGradeDao extends CrudRepository<ScienceGrade, Integer> {

    Iterable<ScienceGrade> findGradeByStudentId(int id);

    void deleteByStudentId(int id);
}
