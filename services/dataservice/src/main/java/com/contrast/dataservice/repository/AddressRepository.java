package com.contrast.dataservice.repository;

import com.contrast.dataservice.entity.Address;
import com.contrast.dataservice.entity.User;
import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "address", path = "address")
public interface AddressRepository extends PagingAndSortingRepository<Address, Long>, CrudRepository<Address, Long> {

  List<Address> findByFname(@Param("fname") String fname);
  List<Address> findByName(@Param("name") String name);
  List<Address> findByObjOwner(@Param("objOwner") User objOwner);

}
