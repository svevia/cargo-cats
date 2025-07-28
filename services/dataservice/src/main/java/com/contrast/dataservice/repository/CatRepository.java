package com.contrast.dataservice.repository;

import com.contrast.dataservice.entity.Cat;
import com.contrast.dataservice.entity.User;
import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "cats", path = "cats")
public interface CatRepository extends PagingAndSortingRepository<Cat, Long>, CrudRepository<Cat, Long> {

  List<Cat> findByName(@Param("name") String name);
  List<Cat> findByType(@Param("type") String type);
  List<Cat> findByObjOwner(@Param("objOwner") User objOwner);

}
